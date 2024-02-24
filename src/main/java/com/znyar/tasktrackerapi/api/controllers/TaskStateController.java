package com.znyar.tasktrackerapi.api.controllers;

import com.znyar.tasktrackerapi.api.controllers.helpers.ControllerHelper;
import com.znyar.tasktrackerapi.api.dto.AckDto;
import com.znyar.tasktrackerapi.api.dto.TaskStateDto;
import com.znyar.tasktrackerapi.api.exceptions.BadRequestException;
import com.znyar.tasktrackerapi.api.exceptions.NotFoundException;
import com.znyar.tasktrackerapi.api.factories.TaskStateDtoFactory;
import com.znyar.tasktrackerapi.store.entities.ProjectEntity;
import com.znyar.tasktrackerapi.store.entities.TaskStateEntity;
import com.znyar.tasktrackerapi.store.repositories.TaskStateRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskStateController {

    TaskStateRepository taskStateRepository;

    TaskStateDtoFactory taskStateDtoFactory;

    ControllerHelper controllerHelper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task_states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task_states";
    public static final String UPDATE_TASK_STATE = "/api/task_states/{task_state_id}";
    public static final String CHANGE_TASK_STATE_POSITION = "/api/task_states/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/task_states/{task_state_id}";


    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable("project_id") Long projectId){
        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(
            @PathVariable("project_id") Long projectId,
            @RequestParam("task_state_name") String taskStateName) {

        if (taskStateName.trim().isEmpty()) throw new BadRequestException("Task state name can`t be empty");

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();

        for (TaskStateEntity taskState : project.getTaskStates()) {

            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException(String.format("Task state \"%s\" already exists", taskStateName));
            }

            if (taskState.getRightTaskState().isEmpty()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }

        }

        TaskStateEntity taskState = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                    .name(taskStateName)
                    .project(project)
                    .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam("task_state_name") String taskStateName) {

        if (taskStateName.trim().isEmpty()) throw new BadRequestException("Task state name can`t be empty");

        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);

        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName
                )
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                            throw new BadRequestException(String.format("Task state \"%s\" already exists", taskStateName));
                });

        taskState.setName(taskStateName);

        taskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(taskState);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public TaskStateDto changeTaskStatePosition(
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam(value = "left_task_state_id", required = false) Optional<Long> optionalLeftTaskStateId) {

        TaskStateEntity changingTaskState = getTaskStateOrThrowException(taskStateId);

        ProjectEntity project = changingTaskState.getProject();

        Optional<Long> optionalOldLeftTaskStateId = changingTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if (optionalOldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return taskStateDtoFactory.makeTaskStateDto(changingTaskState);
        }

        Optional<TaskStateEntity> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {

                    if (taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left task state id equals changing task state id");
                    }

                    TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

                    if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        throw new BadRequestException("Task state position can be changed within the same project");
                    }
                    return leftTaskStateEntity;
                });

        Optional<TaskStateEntity> optionalNewRightTaskState;
        if (optionalNewLeftTaskState.isEmpty()) {

            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> anotherTaskState.getLeftTaskState().isEmpty())
                    .findAny();

        } else {
            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStatePosition(changingTaskState);

        if (optionalNewLeftTaskState.isPresent()) {
            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();
            newLeftTaskState.setRightTaskState(changingTaskState);
            changingTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changingTaskState.setLeftTaskState(null);
        }

        if (optionalNewRightTaskState.isPresent()) {
            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();
            newRightTaskState.setLeftTaskState(changingTaskState);
            changingTaskState.setRightTaskState(newRightTaskState);
        } else {
            changingTaskState.setRightTaskState(null);
        }

        changingTaskState = taskStateRepository.saveAndFlush(changingTaskState);

        optionalNewLeftTaskState.ifPresent(taskStateRepository::saveAndFlush);
        optionalNewRightTaskState.ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDtoFactory.makeTaskStateDto(changingTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AckDto deleteTaskState(@PathVariable("task_state_id") Long taskStateId) {

        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);
        replaceOldTaskStatePosition(taskState);
        taskStateRepository.delete(taskState);

        return AckDto.makeDefault(true);
    }

    private void replaceOldTaskStatePosition(TaskStateEntity changingTaskState) {

        Optional<TaskStateEntity> optionalOldLeftTaskState = changingTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changingTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it -> {
                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));
                    taskStateRepository.saveAndFlush(it);
                });
        optionalOldRightTaskState
                .ifPresent(it -> {
                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));
                    taskStateRepository.saveAndFlush(it);
                });
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException(String.format("Task state with \"%s\" id doesn`t exist", taskStateId))
                );
    }
}
