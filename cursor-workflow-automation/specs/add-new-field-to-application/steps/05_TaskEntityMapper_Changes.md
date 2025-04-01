# TaskEntityMapper Changes Specification

## File: `src/main/java/com/cursor/automation/mapper/TaskEntityMapper.java`

## Changes Required

Update the TaskEntityMapper to handle the new field when mapping between TaskServiceModel and TaskEntity.

### Implementation Details

1. Modify the `toEntity` method to include mapping of the new field from ServiceModel to Entity
2. Modify the `toServiceModel` method to include mapping of the new field from Entity to ServiceModel
3. Update any batch mapping methods if present

### Code Example

```java
public class TaskEntityMapper {

    // Convert ServiceModel to Entity
    public TaskEntity toEntity(TaskServiceModel serviceModel) {
        if (serviceModel == null) {
            return null;
        }
        
        // Updated to include new field
        TaskEntity entity = new TaskEntity(
            serviceModel.getId(), 
            serviceModel.getTitle(),
            serviceModel.getNewField()  // Add the new field
        );
        
        return entity;
    }
    
    // Convert Entity to ServiceModel
    public TaskServiceModel toServiceModel(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Updated to include new field
        TaskServiceModel serviceModel = new TaskServiceModel(
            entity.getId(), 
            entity.getTitle(),
            entity.getNewField()  // Add the new field
        );
        
        return serviceModel;
    }
    
    // If batch mapping methods exist, also update them
    public List<TaskEntity> toEntityList(List<TaskServiceModel> serviceModelList) {
        if (serviceModelList == null) {
            return Collections.emptyList();
        }
        
        return serviceModelList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    public List<TaskServiceModel> toServiceModelList(List<TaskEntity> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        
        return entityList.stream()
                .map(this::toServiceModel)
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