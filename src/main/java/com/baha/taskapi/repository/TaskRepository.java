package com.baha.taskapi.repository;

import com.baha.taskapi.entity.Task;
import com.baha.taskapi.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status);
}
