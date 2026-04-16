package com.smarttaskmanager.dto;

import com.smarttaskmanager.model.Task;
import com.smarttaskmanager.model.TaskStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record TaskResponse(
        Long id,
        String title,
        String category,
        String priority,
        String status,
        LocalDate dueDate,
        Integer estimatedMinutes,
        String notes,
        boolean overdue,
        boolean dueToday,
        long daysLeft
) {
    public static TaskResponse from(Task task, LocalDate today) {
        long daysLeft = ChronoUnit.DAYS.between(today, task.getDueDate());
        boolean overdue = task.getStatus() == TaskStatus.DONE ? false : task.getDueDate().isBefore(today);
        boolean dueToday = task.getStatus() == TaskStatus.DONE ? false : task.getDueDate().isEqual(today);

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getCategory(),
                task.getPriority().name(),
                task.getStatus().name(),
                task.getDueDate(),
                task.getEstimatedMinutes(),
                task.getNotes(),
                overdue,
                dueToday,
                daysLeft
        );
    }
}
