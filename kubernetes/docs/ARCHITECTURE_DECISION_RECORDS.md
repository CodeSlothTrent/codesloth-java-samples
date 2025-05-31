# Architecture Decision Records (ADR)

## ADR-001: Use Kubernetes API for Cluster Storage Instead of In-Memory HashMap

**Status**: Accepted

**Context**: 
The controller needs persistent storage for OpenSearch cluster metadata across application restarts, with features like audit trails, RBAC, and high availability.

**Decision**: 
Use Kubernetes Custom Resource Definitions (CRDs) and the Kubernetes Java Client API for cluster storage instead of `HashMap<String, OpenSearchCluster>`.

**Consequences**:
- ✅ **Persistence**: Cluster state survives application restarts
- ✅ **High Availability**: Kubernetes manages data replication
- ✅ **Audit Trail**: Built-in audit logging of all changes
- ✅ **RBAC**: Fine-grained access control
- ✅ **Consistency**: ACID transactions via etcd
- ❌ **Complexity**: Requires Kubernetes API configuration
- ❌ **Dependencies**: Tight coupling to Kubernetes environment

---

## ADR-002: Remove CloudWatch Threshold Re-Analysis Since Alarms Handle This

**Status**: Accepted

**Context**: 
CloudWatch alarms already perform threshold monitoring, trend analysis, and anomaly detection before sending SQS messages.

**Decision**: 
Remove redundant threshold checking and trend analysis from the application. Trust CloudWatch alarm states.

**Rationale**: 
- CloudWatch alarms fire when thresholds are crossed
- Re-checking thresholds in application is redundant
- Alarm presence indicates the condition already occurred

**Implementation**: 
- Removed `isUpwardTrend()`, `extractMetricValue()` methods
- Modified `isUnderStress()` to check alarm states, not raw values
- Updated `calculateHealthScore()` to use alarm severity

---

## ADR-003: Process Single CloudWatch Alarms Per SQS Message

**Status**: Accepted

**Context**: 
CloudWatch sends one SQS notification per alarm state transition (OK → ALARM, ALARM → OK), not multiple alarms bundled together.

**Decision**: 
Redesign message processing to handle single alarms instead of assuming multiple alarms per message.

**Rationale**: 
- **AWS Reality**: Each alarm state change generates a separate SQS message
- **Incremental Processing**: One message = one state transition for one metric
- **Targeted Actions**: Take specific action based on the single alarm type/severity

**Changes Made**:
- `isUnderStress()`: Check single alarm severity instead of counting multiple alarms
- `calculateHealthScore()` → `getAlarmSeverity()`: Return severity string for single alarm
- `hasAnomalies()`: Check if the single alarm is anomaly-based
- `hasAlarmForMetric()`: Check if the single alarm matches metric type
- **Removed `isUnderCombinedStress()`**: Cannot correlate multiple metrics in single-alarm messages

**Impact**:
- ✅ **Correct Architecture**: Matches how CloudWatch actually works
- ✅ **Targeted Actions**: Specific remediation for specific alarms
- ✅ **Simpler Logic**: No complex multi-alarm correlation
- ✅ **Emergency Actions**: Based on individual alarm severity, not impossible multi-metric correlation
- ❌ **No Cross-Alarm Analysis**: Need separate state tracking for correlation 

---

## ADR-004: Eliminate Redundant Code Violating Single-Alarm Architecture

**Status**: Accepted

**Context**: 
Multiple methods in OpenSearchClusterController violated the single-alarm-per-message principle by:
- Re-checking thresholds that CloudWatch already validated
- Assuming multiple metrics in one message
- Complex custom rule engines duplicating CloudWatch functionality

**Decision**: 
Systematically remove all redundant threshold checking and multi-metric correlation logic.

**Removed Components**:
- **`isUnderCombinedStress()`**: Cannot correlate multiple metrics in single-alarm messages
- **Custom Rules Engine**: 5 methods (`applyCustomRules`, `evaluateRuleCondition`, `evaluateSimpleExpression`, `evaluateComparison`, `createActionFromRule`) - CloudWatch alarms provide sufficient triggering
- **Scale Calculation Methods**: `calculateScaleOutNodes()`, `calculateScaleInNodes()`, `canScaleIn()` - used redundant threshold math
- **Duplicate Helper Methods**: `hasAlarmForMetric()`, `getAlarmSeverity()` - simplified alarm processing
- **Multi-Metric Updates**: `updateClusterMetrics()` now stores alarm info instead of assuming multiple metrics

**Simplified Logic**:
- **Single alarm processing**: Check alarm name/severity, take targeted action
- **No threshold re-checking**: Trust CloudWatch alarm decisions  
- **Simple scaling**: Add 1 node for most alarms, emergency scale for critical
- **Alarm-based remediation**: CPU/Memory → Scale, Latency → Scale or New Cluster, Disk → Alert

**Impact**:
- ✅ **Architectural Consistency**: All code matches single-alarm reality
- ✅ **Reduced Complexity**: ~150 lines of redundant code removed
- ✅ **Clearer Logic**: Simple alarm type → action mapping
- ✅ **No Threshold Duplication**: CloudWatch is authoritative source
- ❌ **Less Customization**: Removed custom rule engine (but CloudWatch provides this) 

---

## ADR-005: Single Action Per Message Instead of Action Lists

**Status**: Accepted

**Context**: 
The architecture still assumed multiple actions per message by using `List<RemediationAction>` return types and complex action filtering/sorting logic, violating the single-alarm-per-message principle.

**Decision**: 
Convert all action processing from lists to single actions, matching the one-alarm-per-message reality.

**Rationale**:
- **One Alarm = One Action**: Each SQS message contains one alarm, which should trigger one specific action
- **No Action Lists**: Cannot have multiple actions from a single alarm state transition
- **Simplified Execution**: No need for priority sorting or filtering when there's only one action

**Changes Made**:
- `analyzeAndPlanRemediation()`: Returns `RemediationAction` instead of `List<RemediationAction>`
- **Removed**: `filterByCooldown()` method - cooldown check moved inline
- **Removed**: `executeRemediationActions()` method - replaced with `executeRemediationAction()`
- `updateClusterStatus()`: Takes single action instead of action list
- **Removed**: Duplicate emergency scaling logic (now integrated into CPU/Memory handling)

**Simplified Flow**:
```
SQS Message → Single Alarm → Single Action → Execute → Update Status
```

**Impact**:
- ✅ **Perfect Architectural Alignment**: Code exactly matches CloudWatch behavior
- ✅ **Eliminated Complexity**: No more list processing for inherently single operations
- ✅ **Cleaner Logic**: Direct alarm → action → execution flow
- ✅ **No Redundant Processing**: One decision path per message
- ❌ **No Batch Operations**: But these were impossible anyway with single-alarm messages 

---

## ADR-006: Intelligent Cooldown Handling for Persistent Alarms

**Status**: Accepted

**Context**: 
A critical flaw was discovered: if CloudWatch alarms stay in ALARM state and cooldown prevents action execution, no new SQS messages are sent, leading to persistent issues never being remediated.

**Problem Flow**:
```
CloudWatch Alarm Fires → SQS Message → Cooldown Blocks Action → 
Alarm Stays in ALARM State → No New Messages → Issue Persists Forever
```

**Decision**: 
Implement intelligent cooldown handling with severity-based bypassing and delayed retry scheduling.

**Solution Components**:

1. **Critical Alarm Bypass**: Critical alarms bypass cooldown entirely
2. **Delayed Retry**: Non-critical alarms schedule execution after cooldown expires  
3. **Smart Re-evaluation**: Check if action is still needed before delayed execution

**Implementation**:
- `handleActionWithCooldown()`: Intelligent cooldown decision logic
- `scheduleDelayedAction()`: Schedule future execution using `CompletableFuture.delayedExecutor`
- `isAlarmStillActive()`: Validate action is still needed before delayed execution
- `getRemainingCooldown()`: Calculate exact delay needed

**Cooldown Logic**:
```java
if (cooldown_active) {
    if (CRITICAL_alarm) {
        execute_immediately(); // Bypass cooldown
    } else {
        schedule_delayed_execution(remaining_cooldown);
    }
}
```

**Benefits**:
- ✅ **No Lost Actions**: Persistent alarms will eventually get remediated
- ✅ **Critical Priority**: Severe issues bypass cooldown for immediate action
- ✅ **Efficient Scheduling**: Delays align with actual cooldown expiration
- ✅ **Smart Validation**: Avoids unnecessary actions if alarm cleared

**Risk Mitigation**:
- ❌ **Potential Action Storms**: If many alarms persist, but limited by cooldown periods
- ❌ **Memory Usage**: Scheduled futures, but bounded by alarm frequency

This ensures CloudWatch alarm persistence doesn't create permanent blind spots in remediation. 

---

## ADR-007: State-Aware Cooldowns to Handle Oscillating Alarms

**Status**: Accepted

**Context**: 
The time-based cooldown system had a critical flaw: during slow scaling operations (5+ minutes), oscillating CloudWatch alarms would trigger multiple scaling attempts, ignoring that scaling was already in progress.

**Problem Scenario**:
```
1. CPU alarm fires → Start scaling (3→4 nodes)
2. During 5-minute scaling process, CPU oscillates:
   - CPU 85% → Alarm → Triggers ANOTHER scale attempt
   - CPU 70% → OK
   - CPU 82% → Alarm → Triggers ANOTHER scale attempt
3. Multiple overlapping scaling operations
```

**Decision**: 
Enhance CooldownManager to be **state-aware**, checking both time cooldowns AND cluster operational state.

**Implementation**:

**Two-Layer Cooldown System**:
1. **Time Cooldown**: Traditional "don't repeat this action for X minutes"  
2. **State Cooldown**: "don't repeat this action while cluster is busy"

**Enhanced Methods**:
- `canExecuteActionWithState()`: Checks both time AND state
- `isTimeCooldownExpired()`: Pure time-based check
- `isClusterReadyForAction()`: State-based availability check

**State Conflicts Detected**:
- **Scaling Actions**: Blocked if cluster phase is `SCALING` or `UPDATING`
- **Any Actions**: Blocked if cluster is `CREATING`, `ERROR`, or busy

**Intelligent Bypass Logic**:
```java
if (time_cooldown_active) {
    if (CRITICAL) bypass_time_cooldown(); // But still respect state
} else if (state_conflict) {
    if (CRITICAL) log_but_cannot_bypass_state();
    else ignore_until_state_ready();
}
```

**Flow Example**:
```
CPU Alarm → Check Time Cooldown (OK) → Check State (SCALING) → 
Block Action + Log "Already scaling, ignoring oscillating alarm"
```

**Benefits**:
- ✅ **No Duplicate Operations**: Prevents scaling while already scaling
- ✅ **Oscillation Immunity**: Ignores alarm noise during operations  
- ✅ **State Awareness**: Understands cluster lifecycle phases
- ✅ **Smart Critical Handling**: Bypasses time but respects operational reality

**Impact**:
- ✅ **Operational Stability**: No more conflicting simultaneous operations
- ✅ **Reduced Noise**: Filters out expected alarm oscillations
- ✅ **Better Resource Usage**: Avoids wasted scaling attempts
- ❌ **Increased Complexity**: More logic to maintain and test

This prevents the common anti-pattern of **"fighting yourself"** during operational transitions. 