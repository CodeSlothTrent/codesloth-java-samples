# Mapper Tests Changes Specification

## Files: 
- `src/test/java/com/cursor/automation/mapper/TaskDTOMapperTest.java`
- `src/test/java/com/cursor/automation/mapper/TaskEntityMapperTest.java`

## Changes Required

Update the mapper test classes to include tests for mapping the new `dueDate` field.

### Implementation Details

1. Update existing tests to verify the dueDate is correctly mapped
2. Add new tests specifically for the dueDate field mapping in both directions

## TaskDTOMapperTest Changes

### Code Example

```java
// Add import
import java.time.LocalDate;

public class TaskDTOMapperTest {
    
    @Test
    public void testToServiceModel_ValidDTO_ReturnsServiceModel() {
        // Given
        LocalDate dueDate = LocalDate.now().plusDays(1);
        TaskDTO dto = new TaskDTO("test-id", "Test Task");
        dto.setDueDate(dueDate);
        
        // When
        TaskServiceModel serviceModel = TaskDTOMapper.toServiceModel(dto);
        
        // Then
        assertNotNull(serviceModel);
        assertEquals(dto.getId(), serviceModel.getId());
        assertEquals(dto.getTitle(), serviceModel.getTitle());
        assertEquals(dueDate, serviceModel.getDueDate()); // Verify dueDate mapping
    }
    
    @Test
    public void testToServiceModel_NullDueDate_MapsCorrectly() {
        // Given
        TaskDTO dto = new TaskDTO("test-id", "Test Task");
        // dueDate is null by default
        
        // When
        TaskServiceModel serviceModel = TaskDTOMapper.toServiceModel(dto);
        
        // Then
        assertNotNull(serviceModel);
        assertNull(serviceModel.getDueDate());
    }
    
    @Test
    public void testToDTO_ValidServiceModel_ReturnsDTO() {
        // Given
        LocalDate dueDate = LocalDate.now().plusDays(1);
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task", dueDate);
        
        // When
        TaskDTO dto = TaskDTOMapper.toDTO(serviceModel);
        
        // Then
        assertNotNull(dto);
        assertEquals(serviceModel.getId(), dto.getId());
        assertEquals(serviceModel.getTitle(), dto.getTitle());
        assertEquals(dueDate, dto.getDueDate()); // Verify dueDate mapping
    }
    
    @Test
    public void testToDTO_NullDueDate_MapsCorrectly() {
        // Given
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task");
        // dueDate is null by default
        
        // When
        TaskDTO dto = TaskDTOMapper.toDTO(serviceModel);
        
        // Then
        assertNotNull(dto);
        assertNull(dto.getDueDate());
    }
    
    // Update list mapping tests as well
    @Test
    public void testToDTOList_ValidServiceModels_ReturnsDTOList() {
        // Given
        LocalDate date1 = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        LocalDate date3 = LocalDate.now().plusDays(3);
        
        List<TaskServiceModel> serviceModels = Arrays.asList(
            new TaskServiceModel("id-1", "Task 1", date1),
            new TaskServiceModel("id-2", "Task 2", date2),
            new TaskServiceModel("id-3", "Task 3", date3)
        );
        
        // When
        List<TaskDTO> dtos = TaskDTOMapper.toDTOList(serviceModels);
        
        // Then
        assertNotNull(dtos);
        assertEquals(3, dtos.size());
        
        assertEquals("id-1", dtos.get(0).getId());
        assertEquals("Task 1", dtos.get(0).getTitle());
        assertEquals(date1, dtos.get(0).getDueDate());
        
        assertEquals("id-2", dtos.get(1).getId());
        assertEquals("Task 2", dtos.get(1).getTitle());
        assertEquals(date2, dtos.get(1).getDueDate());
        
        assertEquals("id-3", dtos.get(2).getId());
        assertEquals("Task 3", dtos.get(2).getTitle());
        assertEquals(date3, dtos.get(2).getDueDate());
    }
}
```

## TaskEntityMapperTest Changes

### Code Example

```java
// Add import
import java.time.LocalDate;

public class TaskEntityMapperTest {
    
    @Test
    public void testToEntity_ValidServiceModel_ReturnsEntity() {
        // Given
        LocalDate dueDate = LocalDate.now().plusDays(1);
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task", dueDate);
        
        // When
        TaskEntity entity = TaskEntityMapper.toEntity(serviceModel);
        
        // Then
        assertNotNull(entity);
        assertEquals(serviceModel.getId(), entity.getId());
        assertEquals(serviceModel.getTitle(), entity.getTitle());
        assertEquals(dueDate, entity.getDueDate()); // Verify dueDate mapping
    }
    
    @Test
    public void testToEntity_NullDueDate_MapsCorrectly() {
        // Given
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task");
        // dueDate is null by default
        
        // When
        TaskEntity entity = TaskEntityMapper.toEntity(serviceModel);
        
        // Then
        assertNotNull(entity);
        assertNull(entity.getDueDate());
    }
    
    @Test
    public void testToServiceModel_ValidEntity_ReturnsServiceModel() {
        // Given
        LocalDate dueDate = LocalDate.now().plusDays(1);
        TaskEntity entity = new TaskEntity("test-id", "Test Task", dueDate);
        
        // When
        TaskServiceModel serviceModel = TaskEntityMapper.toServiceModel(entity);
        
        // Then
        assertNotNull(serviceModel);
        assertEquals(entity.getId(), serviceModel.getId());
        assertEquals(entity.getTitle(), serviceModel.getTitle());
        assertEquals(dueDate, serviceModel.getDueDate()); // Verify dueDate mapping
    }
    
    @Test
    public void testToServiceModel_NullDueDate_MapsCorrectly() {
        // Given
        TaskEntity entity = new TaskEntity("test-id", "Test Task");
        // dueDate is null by default
        
        // When
        TaskServiceModel serviceModel = TaskEntityMapper.toServiceModel(entity);
        
        // Then
        assertNotNull(serviceModel);
        assertNull(serviceModel.getDueDate());
    }
    
    // Update list mapping tests as well
    @Test
    public void testToServiceModelList_ValidEntities_ReturnsServiceModelList() {
        // Given
        LocalDate date1 = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        LocalDate date3 = LocalDate.now().plusDays(3);
        
        List<TaskEntity> entities = Arrays.asList(
            new TaskEntity("id-1", "Task 1", date1),
            new TaskEntity("id-2", "Task 2", date2),
            new TaskEntity("id-3", "Task 3", date3)
        );
        
        // When
        List<TaskServiceModel> serviceModels = TaskEntityMapper.toServiceModelList(entities);
        
        // Then
        assertNotNull(serviceModels);
        assertEquals(3, serviceModels.size());
        
        assertEquals("id-1", serviceModels.get(0).getId());
        assertEquals("Task 1", serviceModels.get(0).getTitle());
        assertEquals(date1, serviceModels.get(0).getDueDate());
        
        assertEquals("id-2", serviceModels.get(1).getId());
        assertEquals("Task 2", serviceModels.get(1).getTitle());
        assertEquals(date2, serviceModels.get(1).getDueDate());
        
        assertEquals("id-3", serviceModels.get(2).getId());
        assertEquals("Task 3", serviceModels.get(2).getTitle());
        assertEquals(date3, serviceModels.get(2).getDueDate());
    }
}
```

### Testing Considerations

1. Test that dueDate is correctly mapped in all mapper methods
2. Test with null dueDate values to ensure backward compatibility
3. Test mapper behavior with lists containing mixed date values 