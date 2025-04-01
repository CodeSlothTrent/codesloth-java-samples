# TaskDTOMapper Changes Specification

## File: `src/main/java/com/cursor/automation/mapper/TaskDTOMapper.java`

## Changes Required

Update the TaskDTOMapper to handle the new field when mapping between TaskDTO and TaskServiceModel.

### Implementation Details

1. Modify the `toServiceModel` method to include mapping of the new field from DTO to ServiceModel
2. Modify the `toDTO` method to include mapping of the new field from ServiceModel to DTO
3. Update any batch mapping methods if present

### Code Example

```java
public class TaskDTOMapper {

    // Convert DTO to Service Model
    public TaskServiceModel toServiceModel(TaskDTO dto) {
        if (dto == null) {
            return null;
        }
        
        // Updated to include new field
        TaskServiceModel serviceModel = new TaskServiceModel(
            dto.getId(), 
            dto.getTitle(),
            dto.getNewField()  // Add the new field
        );
        
        return serviceModel;
    }
    
    // Convert Service Model to DTO
    public TaskDTO toDTO(TaskServiceModel serviceModel) {
        if (serviceModel == null) {
            return null;
        }
        
        // Updated to include new field
        TaskDTO dto = new TaskDTO(
            serviceModel.getId(), 
            serviceModel.getTitle(),
            serviceModel.getNewField()  // Add the new field
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

1. Test that the new field is properly mapped in both directions
2. Verify null handling for the new field
3. Test batch mapping operations with mixed datasets (some with new field, some without)
4. Ensure that existing tests for the mapper still pass
5. For primitive types, verify default value handling 