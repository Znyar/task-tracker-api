package com.znyar.tasktrackerapi.api.controllers.helpers;

import com.znyar.tasktrackerapi.api.exceptions.NotFoundException;
import com.znyar.tasktrackerapi.store.entities.ProjectEntity;
import com.znyar.tasktrackerapi.store.repositories.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {

    ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn`t exist",
                                        projectId
                                )
                        )
                );
    }
}
