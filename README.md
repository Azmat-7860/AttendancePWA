# Worker Attendance PWA

Worker Attendance PWA for contractors and supervisors to manage workers, record daily attendance, calculate wages with overtime and advances, and generate monthly attendance PDF reports.

Source of truth:
- `docs/TRD.md` — Technical Requirements Document
- `docs/TASKS.md` — Task Backlog and Status
- `AGENTS.md` — Agent Guidelines and Hard Constraints

---

## Prerequisites

- **Java**: 21 LTS (`java -version`)
- **Node.js**: 20 LTS (`node -v`)
- **npm**: 10+ (`npm -v`)
- **Docker**: For running local PostgreSQL (`docker compose up -d`)

---

## Quickstart

### 1. Database
Start local PostgreSQL:
```bash
docker compose up -d
```

### 2. Backend API
```bash
cd api
./mvnw spring-boot:run
```
The API starts on `http://localhost:8080`.
Verify health:
```bash
curl http://localhost:8080/health
```

Run backend verification (lint/tests):
```bash
cd api
./mvnw verify
```

### 3. Frontend Web App
```bash
cd web
npm install
npm run dev
```
The PWA runs on `http://localhost:5173`.

Run frontend quality checks:
```bash
cd web
npm run lint         # ESLint
npm run typecheck    # TypeScript compiler check
npm run test         # Vitest unit tests
npm run e2e          # Playwright E2E tests
```

