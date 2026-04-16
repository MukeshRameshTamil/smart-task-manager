package com.smarttaskmanager.dto;

import com.smarttaskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(@NotNull TaskStatus status) {
}

