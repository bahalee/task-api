package com.baha.taskapi.service;

import com.baha.taskapi.dto.CreateTaskRequest;
import com.baha.taskapi.entity.Task;
import com.baha.taskapi.enums.TaskStatus;
import com.baha.taskapi.exception.InvalidTransitionException;
import com.baha.taskapi.exception.TaskNotFoundException;
import com.baha.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(CreateTaskRequest request) {
        String title = request.getTitle().trim();

        if (title.isEmpty() || title.length() > 120) {
            throw new IllegalArgumentException(
                    "Title must contain between 1 and 120 characters after trim"
            );
        }

        Task task = new Task(title, request.getDescription());

        return taskRepository.save(task);
    }

    public List<Task> getTasks(TaskStatus status) {
        if (status == null) {
            return taskRepository.findAll();
        }

        return taskRepository.findByStatus(status);
    }

    public Task updateStatus(Long id, TaskStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus currentStatus = task.getStatus();

        boolean validTransition =
                (currentStatus == TaskStatus.TODO && newStatus == TaskStatus.IN_PROGRESS)
                        || (currentStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.DONE);

        if (!validTransition) {
            throw new InvalidTransitionException();
        }

        task.setStatus(newStatus);

        return taskRepository.save(task);
    }
}