package com.smarttaskmanager.service;

import com.smarttaskmanager.dto.DashboardResponse;
import com.smarttaskmanager.dto.TaskRequest;
import com.smarttaskmanager.dto.TaskResponse;
import com.smarttaskmanager.dto.TaskStatusUpdateRequest;
import com.smarttaskmanager.exception.TaskNotFoundException;
import com.smarttaskmanager.model.Priority;
import com.smarttaskmanager.model.Task;
import com.smarttaskmanager.model.TaskStatus;
import com.smarttaskmanager.repository.TaskRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class TaskService implements CommandLineRunner {

    private final TaskRepository taskRepository;
    private final boolean seedDemoData;

    public TaskService(TaskRepository taskRepository, @Value("${app.seed-demo-data:true}") boolean seedDemoData) {
        this.taskRepository = taskRepository;
        this.seedDemoData = seedDemoData;
    }

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        List<Task> tasks = taskRepository.findAll()
                .stream()
                .sorted(taskComparator(today))
                .toList();

        List<TaskResponse> taskResponses = tasks.stream()
                .map(task -> TaskResponse.from(task, today))
                .toList();

        long todoCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.TODO).count();
        long inProgressCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS).count();
        long doneCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        long dueTodayCount = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE && task.getDueDate().isEqual(today))
                .count();
        long overdueCount = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE && task.getDueDate().isBefore(today))
                .count();
        int completionRate = tasks.isEmpty() ? 0 : (int) Math.round((doneCount * 100.0) / tasks.size());

        TaskResponse recommendedTask = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .sorted(taskComparator(today))
                .map(task -> TaskResponse.from(task, today))
                .findFirst()
                .orElse(null);

        return new DashboardResponse(
                taskResponses,
                tasks.size(),
                todoCount,
                inProgressCount,
                doneCount,
                dueTodayCount,
                overdueCount,
                completionRate,
                recommendedTask,
                buildInsight(overdueCount, dueTodayCount, recommendedTask)
        );
    }

    public List<TaskResponse> getTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll()
                .stream()
                .sorted(taskComparator(today))
                .map(task -> TaskResponse.from(task, today))
                .toList();
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        return TaskResponse.from(taskRepository.save(task), LocalDate.now());
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        Task task = findTask(taskId);
        applyRequest(task, request);
        return TaskResponse.from(taskRepository.save(task), LocalDate.now());
    }

    public TaskResponse updateTaskStatus(Long taskId, TaskStatusUpdateRequest request) {
        Task task = findTask(taskId);
        task.setStatus(request.status());
        return TaskResponse.from(taskRepository.save(task), LocalDate.now());
    }

    public void deleteTask(Long taskId) {
        taskRepository.delete(findTask(taskId));
    }

    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.title().trim());
        task.setCategory(sanitizeCategory(request.category()));
        task.setPriority(request.priority());
        task.setStatus(request.status());
        task.setDueDate(request.dueDate());
        task.setEstimatedMinutes(request.estimatedMinutes());
        task.setNotes(sanitizeNotes(request.notes()));
    }

    private String sanitizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "General";
        }
        return category.trim();
    }

    private String sanitizeNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return "";
        }
        return notes.trim();
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private Comparator<Task> taskComparator(LocalDate today) {
        return Comparator
                .comparing((Task task) -> task.getStatus() == TaskStatus.DONE)
                .thenComparing((Task task) -> task.getDueDate().isAfter(today.plusDays(1)))
                .thenComparing(Task::getDueDate)
                .thenComparing((Task task) -> -task.getPriority().weight())
                .thenComparing(Task::getEstimatedMinutes)
                .thenComparing(Task::getCreatedAt);
    }

    private String buildInsight(long overdueCount, long dueTodayCount, TaskResponse recommendedTask) {
        if (recommendedTask == null) {
            return "Everything is complete. This board is ready for the next sprint.";
        }
        if (overdueCount > 0) {
            return "You have " + overdueCount + " overdue task" + pluralize(overdueCount) + ". Clear the oldest pressure point first.";
        }
        if (dueTodayCount > 0) {
            return "There are " + dueTodayCount + " task" + pluralize(dueTodayCount) + " due today. Keep the board tight and finish the urgent work.";
        }
        return "\"" + recommendedTask.title() + "\" is the cleanest next move based on due date, priority, and effort.";
    }

    private String pluralize(long count) {
        return count == 1 ? "" : "s";
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData || taskRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        taskRepository.saveAll(List.of(
                demoTask("Review launch copy", "Marketing", today, 45, "Make the premium hero section tighter.", TaskStatus.IN_PROGRESS, 3),
                demoTask("Database backup policy", "Ops", today.plusDays(1), 30, "Add retention notes for PostgreSQL.", TaskStatus.TODO, 2),
                demoTask("Client invoice", "Finance", today.minusDays(1), 20, "Send before end of day.", TaskStatus.TODO, 3),
                demoTask("Sprint retro notes", "Team", today.plusDays(3), 25, "Summarize blockers and wins.", TaskStatus.DONE, 1),
                demoTask("Mobile spacing pass", "Design", today.plusDays(2), 60, "Reduce vertical padding below 768px.", TaskStatus.TODO, 2)
        ));
    }

    private Task demoTask(
            String title,
            String category,
            LocalDate dueDate,
            int estimatedMinutes,
            String notes,
            TaskStatus status,
            int priorityWeight
    ) {
        Task task = new Task();
        task.setTitle(title);
        task.setCategory(category);
        task.setDueDate(dueDate);
        task.setEstimatedMinutes(estimatedMinutes);
        task.setNotes(notes);
        task.setStatus(status);
        task.setPriority(switch (priorityWeight) {
            case 3 -> Priority.HIGH;
            case 2 -> Priority.MEDIUM;
            default -> Priority.LOW;
        });
        return task;
    }
}
