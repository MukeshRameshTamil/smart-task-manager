const state = {
    filter: "ALL",
    dashboard: null
};

const form = document.getElementById("taskForm");
const taskList = document.getElementById("taskList");
const focusCard = document.getElementById("focusCard");
const feedback = document.getElementById("feedback");
const filters = document.getElementById("filters");

const statusCycle = {
    TODO: "IN_PROGRESS",
    IN_PROGRESS: "DONE",
    DONE: "TODO"
};

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("dueDate").value = getDefaultDueDate();
    loadDashboard().catch((error) => {
        feedback.textContent = error.message;
    });
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const fields = form.elements;

    const payload = {
        title: fields.namedItem("title").value.trim(),
        category: fields.namedItem("category").value.trim(),
        priority: fields.namedItem("priority").value,
        status: "TODO",
        dueDate: fields.namedItem("dueDate").value,
        estimatedMinutes: Number(fields.namedItem("estimatedMinutes").value),
        notes: fields.namedItem("notes").value.trim()
    };

    try {
        await fetchJson("/api/tasks", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        form.reset();
        fields.namedItem("priority").value = "MEDIUM";
        fields.namedItem("estimatedMinutes").value = 30;
        fields.namedItem("dueDate").value = getDefaultDueDate();
        feedback.textContent = "Task added to the board.";
        await loadDashboard();
    } catch (error) {
        feedback.textContent = error.message;
    }
});

filters.addEventListener("click", (event) => {
    const filterButton = event.target.closest("[data-filter]");

    if (!filterButton) {
        return;
    }

    state.filter = filterButton.dataset.filter;
    renderFilters();
    renderTasks();
});

taskList.addEventListener("click", async (event) => {
    const actionButton = event.target.closest("[data-action]");

    if (!actionButton) {
        return;
    }

    const taskId = actionButton.dataset.id;

    try {
        if (actionButton.dataset.action === "advance") {
            const nextStatus = actionButton.dataset.nextStatus;

            await fetchJson(`/api/tasks/${taskId}/status`, {
                method: "PATCH",
                body: JSON.stringify({ status: nextStatus })
            });

            feedback.textContent = "Task status updated.";
        }

        if (actionButton.dataset.action === "delete") {
            await fetchJson(`/api/tasks/${taskId}`, {
                method: "DELETE"
            });

            feedback.textContent = "Task removed from the board.";
        }

        await loadDashboard();
    } catch (error) {
        feedback.textContent = error.message;
    }
});

async function loadDashboard() {
    const dashboard = await fetchJson("/api/dashboard");
    state.dashboard = dashboard;
    render();
}

function render() {
    renderFilters();
    renderStats();
    renderTasks();
    renderFocus();
}

function renderFilters() {
    [...filters.querySelectorAll(".filter")].forEach((button) => {
        button.classList.toggle("active", button.dataset.filter === state.filter);
    });
}

function renderStats() {
    const dashboard = state.dashboard;
    const activeCount = dashboard.todoCount + dashboard.inProgressCount;

    document.getElementById("activeCount").textContent = activeCount;
    document.getElementById("dueTodayCount").textContent = dashboard.dueTodayCount;
    document.getElementById("overdueCount").textContent = dashboard.overdueCount;
    document.getElementById("completionRate").textContent = `${dashboard.completionRate}%`;
}

function renderTasks() {
    const tasks = getFilteredTasks();
    document.getElementById("taskCountLabel").textContent = `${tasks.length} task${tasks.length === 1 ? "" : "s"}`;

    if (!tasks.length) {
        taskList.innerHTML = `
            <article class="empty-state">
                <h3>No tasks in this lane</h3>
                <p>Add a new task or switch filters to see the full board.</p>
            </article>
        `;
        return;
    }

    taskList.innerHTML = tasks.map((task) => {
        const dueTone = task.overdue ? "overdue" : task.dueToday ? "today" : "";
        const absoluteDaysLeft = Math.abs(task.daysLeft);
        const dueLabel = task.overdue
            ? `${absoluteDaysLeft} day${absoluteDaysLeft === 1 ? "" : "s"} overdue`
            : task.dueToday
                ? "Due today"
                : task.daysLeft === 0
                    ? "Due today"
                    : `${task.daysLeft} day${task.daysLeft === 1 ? "" : "s"} left`;

        return `
            <article class="task-card">
                <div class="task-main">
                    <div class="task-title-row">
                        <span class="task-title">${escapeHtml(task.title)}</span>
                        <span class="task-chip ${task.priority.toLowerCase()}">${formatPriority(task.priority)}</span>
                        <span class="task-chip">${formatStatus(task.status)}</span>
                    </div>

                    <div class="task-tags task-meta">
                        <span class="task-chip">${escapeHtml(task.category)}</span>
                        <span class="task-chip ${dueTone}">${dueLabel}</span>
                        <span class="task-chip">${task.estimatedMinutes} min</span>
                    </div>

                    ${task.notes ? `<p class="task-notes">${escapeHtml(task.notes)}</p>` : ""}
                </div>

                <div class="task-actions">
                    <button
                        class="status-button"
                        type="button"
                        data-action="advance"
                        data-id="${task.id}"
                        data-next-status="${statusCycle[task.status]}"
                    >
                        ${task.status === "DONE" ? "Reopen" : "Move to " + formatStatus(statusCycle[task.status])}
                    </button>
                    <button class="delete-button" type="button" data-action="delete" data-id="${task.id}">
                        Delete
                    </button>
                </div>
            </article>
        `;
    }).join("");
}

function renderFocus() {
    const dashboard = state.dashboard;
    const task = dashboard.recommendedTask;

    if (!task) {
        focusCard.innerHTML = `
            <div class="focus-copy">
                <h3>Board cleared</h3>
                <p>${escapeHtml(dashboard.insight)}</p>
            </div>
            <div class="focus-line">
                <span>Next step</span>
                <strong>Add a fresh task</strong>
            </div>
        `;
        return;
    }

    focusCard.innerHTML = `
        <div class="focus-copy">
            <h3>${escapeHtml(task.title)}</h3>
            <p>${escapeHtml(dashboard.insight)}</p>
        </div>
        <div class="task-tags">
            <span class="task-chip">${escapeHtml(task.category)}</span>
            <span class="task-chip ${task.priority.toLowerCase()}">${formatPriority(task.priority)}</span>
            <span class="task-chip">${task.estimatedMinutes} min</span>
        </div>
        <div class="focus-line">
            <span>Status</span>
            <strong>${formatStatus(task.status)}</strong>
        </div>
        <div class="focus-line">
            <span>Due date</span>
            <strong>${formatDate(task.dueDate)}</strong>
        </div>
    `;
}

function getFilteredTasks() {
    const tasks = state.dashboard?.tasks ?? [];

    if (state.filter === "ALL") {
        return tasks;
    }

    return tasks.filter((task) => task.status === state.filter);
}

function formatPriority(priority) {
    return priority.charAt(0) + priority.slice(1).toLowerCase();
}

function formatStatus(status) {
    return status.replace("_", " ").toLowerCase().replace(/(^|\s)\S/g, (char) => char.toUpperCase());
}

function formatDate(date) {
    const [year, month, day] = date.split("-").map(Number);

    return new Intl.DateTimeFormat("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric"
    }).format(new Date(year, month - 1, day));
}

function getDefaultDueDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return toDateInputValue(tomorrow);
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json"
        },
        ...options
    });

    if (response.status === 204) {
        return null;
    }

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        if (Array.isArray(data.details)) {
            throw new Error(data.details.join(" | "));
        }
        throw new Error(data.error || "Something went wrong.");
    }

    return data;
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function toDateInputValue(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}
