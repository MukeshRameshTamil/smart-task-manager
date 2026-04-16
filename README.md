# Smart Task Manager

Compact task management dashboard built with Java, Spring Boot, and PostgreSQL. The UI is designed to feel premium while keeping the main workflow inside a single screen on desktop with very little page scrolling.

## Stack

- Java 17
- Spring Boot
- PostgreSQL
- Vanilla HTML, CSS, and JavaScript served by Spring Boot
- Gradle Wrapper

## Features

- Compact dashboard with summary metrics and internal task list scrolling
- Create, update, and delete tasks
- Priority, status, due date, category, and effort tracking
- Backend recommendation for the next best task
- Seed demo data for the first launch
- PostgreSQL-ready local setup with Docker Compose

## Run locally

1. Install Java 17 or newer.
2. Start PostgreSQL:

```bash
docker compose up -d
```

3. Start the backend:

```bash
./gradlew bootRun
```

On Windows PowerShell use:

```powershell
.\gradlew.bat bootRun
```

Then open `http://localhost:8080`.

## PostgreSQL connection

The app uses these defaults:

- Database: `smart_tasks`
- Username: `smart_user`
- Password: `smart_pass`

You can override them with:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `SERVER_PORT`
- `SEED_DEMO_DATA`

## Good premium upgrades to add next

- User login and role-based workspaces
- Reminder notifications by email or WhatsApp
- Calendar sync and drag-drop planning
- Team comments and activity timeline
- Analytics for workload and completion trends

