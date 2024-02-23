package com.znyar.tasktrackerapi.store.repositories;

import com.znyar.tasktrackerapi.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStateEntityRepository extends JpaRepository<TaskStateEntity, Long> {
}
