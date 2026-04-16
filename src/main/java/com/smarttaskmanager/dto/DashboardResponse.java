package com.smarttaskmanager.dto;

import java.util.List;

public record DashboardResponse(
        List<TaskResponse> tasks,
        long totalTasks,
        long todoCount,
        long inProgressCount,
        long doneCount,
        long dueTodayCount,
        long overdueCount,
        int completionRate,
        TaskResponse recommendedTask,
        String insight
) {
}

