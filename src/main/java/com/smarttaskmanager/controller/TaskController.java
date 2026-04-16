package com.smarttaskmanager.controller;

import com.smarttaskmanager.dto.TaskRequest;
import com.smarttaskmanager.dto.TaskResponse;
import com.smarttaskmanager.dto.TaskStatusUpdateRequest;
import com.smarttaskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getTasks() {
        return taskService.getTasks();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateTaskStatus(@PathVariable Long taskId, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return taskService.updateTaskStatus(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }
}

