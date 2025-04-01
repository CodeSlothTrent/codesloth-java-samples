# TaskDTOMapper Changes Specification

## File: `src/main/java/com/cursor/automation/mapper/TaskDTOMapper.java`

## Changes Required

Update the TaskDTOMapper to handle the new `dueDate` field when mapping between TaskDTO and TaskServiceModel.

### Implementation Details

1. Modify the `toServiceModel` method to include mapping of the dueDate field from DTO to ServiceModel
2. Modify the `toDTO` method to include mapping of the dueDate field from ServiceModel to DTO
3. Update any batch mapping methods if present

### Code Example

```java
public class TaskDTOMapper {

    // Convert DTO to Service Model
    public TaskServiceModel toServiceModel(TaskDTO dto) {
        if (dto == null) {
            return null;
        }
        
        // Updated to include dueDate field
        TaskServiceModel serviceModel = new TaskServiceModel(
            dto.getId(), 
            dto.getTitle(),
            dto.getDueDate()  // Add the new field
        );
        
        return serviceModel;
    }
    
    // Convert Service Model to DTO
    public TaskDTO toDTO(TaskServiceModel serviceModel) {
        if (serviceModel == null) {
            return null;
        }
        
        // Updated to include dueDate field
        TaskDTO dto = new TaskDTO(
            serviceModel.getId(), 
            serviceModel.getTitle(),
            serviceModel.getDueDate()  // Add the new field
        );
        
        return dto;
    }
    
    // If a batch mapping method exists, also update it
    public List<TaskServiceModel> toServiceModelList(List<TaskDTO> dtoList) {
        if (dtoList == null) {
            return Collections.emptyList();
        }
        
        return dtoList.stream()
                .map(this::toServiceModel)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> toDTOList(List<TaskServiceModel> serviceModelList) {
        if (serviceModelList == null) {
            return Collections.emptyList();
        }
        
        return serviceModelList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
```

### Testing Considerations

1. Test that the dueDate field is properly mapped in both directions
2. Verify null handling for the dueDate field
3. Test batch mapping operations with mixed datasets (some with dueDate, some without)
4. Ensure that existing tests for the mapper still pass 