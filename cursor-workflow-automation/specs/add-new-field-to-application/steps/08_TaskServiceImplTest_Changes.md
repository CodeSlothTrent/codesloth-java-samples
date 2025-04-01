# TaskServiceImplTest Changes Specification

## File: `src/test/java/com/cursor/automation/service/TaskServiceImplTest.java`

## Changes Required

Update the TaskServiceImplTest class to include test cases for the new `dueDate` field in service operations.

### Implementation Details

1. Update existing test methods to set and verify the dueDate field
2. Add new test cases specifically for dueDate handling
3. Test edge cases like null due dates

### Code Example

```java
// Add import
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskEntityMapper taskEntityMapper;
    
    @InjectMocks
    private TaskServiceImpl taskService;
    
    @Test
    public void createTask_WithDueDate_ReturnsTaskWithDueDate() {
        // Arrange
        LocalDate dueDate = LocalDate.now().plusDays(7);
        TaskServiceModel inputTask = new TaskServiceModel();
        inputTask.setTitle("Test Task");
        inputTask.setDueDate(dueDate);
        
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("test-id");
        taskEntity.setTitle("Test Task");
        taskEntity.setDueDate(dueDate);
        
        TaskEntity savedEntity = new TaskEntity();
        savedEntity.setId("test-id");
        savedEntity.setTitle("Test Task");
        savedEntity.setDueDate(dueDate);
        
        TaskServiceModel expectedResult = new TaskServiceModel();
        expectedResult.setId("test-id");
        expectedResult.setTitle("Test Task");
        expectedResult.setDueDate(dueDate);
        
        when(taskEntityMapper.toEntity(inputTask)).thenReturn(taskEntity);
        when(taskRepository.save(taskEntity)).thenReturn(savedEntity);
        when(taskEntityMapper.toServiceModel(savedEntity)).thenReturn(expectedResult);
        
        // Act
        TaskServiceModel result = taskService.createTask(inputTask);
        
        // Assert
        assertEquals("test-id", result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals(dueDate, result.getDueDate());
        
        verify(taskEntityMapper).toEntity(inputTask);
        verify(taskRepository).save(taskEntity);
        verify(taskEntityMapper).toServiceModel(savedEntity);
    }
    
    @Test
    public void createTask_WithNullDueDate_ReturnsTaskWithNullDueDate() {
        // Arrange
        TaskServiceModel inputTask = new TaskServiceModel();
        inputTask.setTitle("Test Task");
        inputTask.setDueDate(null);
        
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("test-id");
        taskEntity.setTitle("Test Task");
        taskEntity.setDueDate(null);
        
        TaskEntity savedEntity = new TaskEntity();
        savedEntity.setId("test-id");
        savedEntity.setTitle("Test Task");
        savedEntity.setDueDate(null);
        
        TaskServiceModel expectedResult = new TaskServiceModel();
        expectedResult.setId("test-id");
        expectedResult.setTitle("Test Task");
        expectedResult.setDueDate(null);
        
        when(taskEntityMapper.toEntity(inputTask)).thenReturn(taskEntity);
        when(taskRepository.save(taskEntity)).thenReturn(savedEntity);
        when(taskEntityMapper.toServiceModel(savedEntity)).thenReturn(expectedResult);
        
        // Act
        TaskServiceModel result = taskService.createTask(inputTask);
        
        // Assert
        assertEquals("test-id", result.getId());
        assertEquals("Test Task", result.getTitle());
        assertNull(result.getDueDate());
        
        verify(taskEntityMapper).toEntity(inputTask);
        verify(taskRepository).save(taskEntity);
        verify(taskEntityMapper).toServiceModel(savedEntity);
    }
    
    @Test
    public void getTaskById_TaskHasDueDate_ReturnsDueDate() {
        // Arrange
        String taskId = "test-id";
        LocalDate dueDate = LocalDate.now().plusDays(7);
        
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(taskId);
        taskEntity.setTitle("Test Task");
        taskEntity.setDueDate(dueDate);
        
        TaskServiceModel expectedResult = new TaskServiceModel();
        expectedResult.setId(taskId);
        expectedResult.setTitle("Test Task");
        expectedResult.setDueDate(dueDate);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskEntity));
        when(taskEntityMapper.toServiceModel(taskEntity)).thenReturn(expectedResult);
        
        // Act
        TaskServiceModel result = taskService.getTaskById(taskId);
        
        // Assert
        assertEquals(taskId, result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals(dueDate, result.getDueDate());
        
        verify(taskRepository).findById(taskId);
        verify(taskEntityMapper).toServiceModel(taskEntity);
    }
    
    // Other test methods...
}
```

### Testing Considerations

1. Test task creation with and without due dates
2. Test retrieving tasks with due dates from the repository
3. Test updating a task's due date
4. Test edge cases (null values, invalid dates)
5. Test for correct behavior when working with multiple tasks with different due dates 