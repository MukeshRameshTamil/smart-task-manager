package com.smarttaskmanager.dto;

import com.smarttaskmanager.model.Priority;
import com.smarttaskmanager.model.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 80) String title,
        @Size(max = 40) String category,
        @NotNull Priority priority,
        @NotNull TaskStatus status,
        @NotNull LocalDate dueDate,
        @NotNull @Min(15) @Max(720) Integer estimatedMinutes,
        @Size(max = 220) String notes
) {
}

