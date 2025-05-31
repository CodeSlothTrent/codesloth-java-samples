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