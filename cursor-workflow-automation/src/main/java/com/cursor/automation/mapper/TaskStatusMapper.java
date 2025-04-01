package com.cursor.automation.mapper;

import com.cursor.automation.dal.model.TaskStatusEntity;
import com.cursor.automation.model.dto.TaskStatusDTO;
import com.cursor.automation.service.model.TaskStatusService;

/**
 * Mapper for converting between different TaskStatus enum types across application layers.
 */
public class TaskStatusMapper {

    /**
     * Converts from DTO layer enum to Service layer enum.
     *
     * @param statusDTO the status enum from the DTO layer
     * @return the corresponding status enum for the Service layer
     */
    public static TaskStatusService toServiceStatus(TaskStatusDTO statusDTO) {
        if (statusDTO == null) {
            return null;
        }

        switch (statusDTO) {
            case TODO:
                return TaskStatusService.TODO;
            case IN_PROGRESS:
                return TaskStatusService.IN_PROGRESS;
            case BLOCKED:
                return TaskStatusService.BLOCKED;
            case IN_REVIEW:
                return TaskStatusService.IN_REVIEW;
            case DONE:
                return TaskStatusService.DONE;
            default:
                throw new IllegalArgumentException("Unknown TaskStatusDTO: " + statusDTO);
        }
    }

    /**
     * Converts from Service layer enum to DTO layer enum.
     *
     * @param statusService the status enum from the Service layer
     * @return the corresponding status enum for the DTO layer
     */
    public static TaskStatusDTO toDTOStatus(TaskStatusService statusService) {
        if (statusService == null) {
            return null;
        }

        switch (statusService) {
            case TODO:
                return TaskStatusDTO.TODO;
            case IN_PROGRESS:
                return TaskStatusDTO.IN_PROGRESS;
            case BLOCKED:
                return TaskStatusDTO.BLOCKED;
            case IN_REVIEW:
                return TaskStatusDTO.IN_REVIEW;
            case DONE:
                return TaskStatusDTO.DONE;
            default:
                throw new IllegalArgumentException("Unknown TaskStatusService: " + statusService);
        }
    }

    /**
     * Converts from Entity layer enum to Service layer enum.
     *
     * @param statusEntity the status enum from the Entity layer
     * @return the corresponding status enum for the Service layer
     */
    public static TaskStatusService toServiceStatus(TaskStatusEntity statusEntity) {
        if (statusEntity == null) {
            return null;
        }

        switch (statusEntity) {
            case TODO:
                return TaskStatusService.TODO;
            case IN_PROGRESS:
                return TaskStatusService.IN_PROGRESS;
            case BLOCKED:
                return TaskStatusService.BLOCKED;
            case IN_REVIEW:
                return TaskStatusService.IN_REVIEW;
            case DONE:
                return TaskStatusService.DONE;
            default:
                throw new IllegalArgumentException("Unknown TaskStatusEntity: " + statusEntity);
        }
    }

    /**
     * Converts from Service layer enum to Entity layer enum.
     *
     * @param statusService the status enum from the Service layer
     * @return the corresponding status enum for the Entity layer
     */
    public static TaskStatusEntity toEntityStatus(TaskStatusService statusService) {
        if (statusService == null) {
            return null;
        }

        switch (statusService) {
            case TODO:
                return TaskStatusEntity.TODO;
            case IN_PROGRESS:
                return TaskStatusEntity.IN_PROGRESS;
            case BLOCKED:
                return TaskStatusEntity.BLOCKED;
            case IN_REVIEW:
                return TaskStatusEntity.IN_REVIEW;
            case DONE:
                return TaskStatusEntity.DONE;
            default:
                throw new IllegalArgumentException("Unknown TaskStatusService: " + statusService);
        }
    }
} 