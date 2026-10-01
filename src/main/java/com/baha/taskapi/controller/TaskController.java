package com.baha.taskapi.controller;

import com.baha.taskapi.dto.CreateTaskRequest;
import com.baha.taskapi.entity.Task;
import com.baha.taskapi.enums.TaskStatus;
import com.baha.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping
    public List<Task> getTasks(
            @RequestParam(required = false) TaskStatus status
    ) {
        return taskService.getTasks(status);
    }

    @PatchMapping("/{id}/status")
    public Task updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request
    ) {
        return taskService.updateStatus(id, request.status());
    }

    public record UpdateStatusRequest(TaskStatus status) {
    }
}