let tasks = JSON.parse(localStorage.getItem("tasks")) || [];
let filter = "all";

function saveTasks() {
    localStorage.setItem("tasks", JSON.stringify(tasks));
}

function addTask() {
    let name = document.getElementById("taskName").value;
    let days = parseInt(document.getElementById("days").value);
    let priority = parseInt(document.getElementById("priority").value);

    if (name === "" || isNaN(days)) {
        alert("Enter valid data!");
        return;
    }

    tasks.push({
        name,
        daysLeft: days,
        priority,
        completed: false
    });

    saveTasks();
    displayTasks();
}

function displayTasks() {
    let container = document.getElementById("taskContainer");
    container.innerHTML = "";

    let filtered = tasks.filter(task => {
        if (filter === "completed") return task.completed;
        if (filter === "pending") return !task.completed;
        return true;
    });

    filtered.forEach((task, index) => {
        let div = document.createElement("div");
        div.className = "task " + (task.completed ? "completed" : "");

        div.innerHTML = `
            <span>
                ${task.name} | Days: ${task.daysLeft} | Priority: ${task.priority}
            </span>

            <div class="task-buttons">
                <button class="complete" onclick="toggleComplete(${index})">✔</button>
                <button onclick="deleteTask(${index})">❌</button>
            </div>
        `;

        container.appendChild(div);
    });
}

function deleteTask(index) {
    tasks.splice(index, 1);
    saveTasks();
    displayTasks();
}

function toggleComplete(index) {
    tasks[index].completed = !tasks[index].completed;
    saveTasks();
    displayTasks();
}

function filterTasks(type) {
    filter = type;
    displayTasks();
}

function suggestTask() {
    let pendingTasks = tasks.filter(t => !t.completed);

    if (pendingTasks.length === 0) {
        document.getElementById("result").innerText = "🎉 All tasks completed!";
        return;
    }

    let best = pendingTasks[0];

    pendingTasks.forEach(task => {
        if (task.daysLeft < best.daysLeft ||
           (task.daysLeft === best.daysLeft && task.priority < best.priority)) {
            best = task;
        }
    });

    let result = document.getElementById("result");

    if (best.daysLeft <= 1) {
        result.innerText = `⚠️ Do "${best.name}" NOW!`;
    } else {
        result.innerText = `✅ Start "${best.name}" soon`;
    }
}

// Load on start
displayTasks();