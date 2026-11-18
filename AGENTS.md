# AGENTS.md

Instructions for AI coding assistants working in this repository.
Read this file fully before every task. If a task conflicts with this file, stop and ask.

## 1. Project

Worker Attendance PWA for a contractor or supervisor who manages many workers.
Supervisors and owners mark daily attendance. The app calculates wages with overtime and advances,
and emails a PDF attendance report every month end. Workers do not log in.

Source-of-truth documents (read the relevant sections before coding):

- `docs/PRD.md`: what to build
- `docs/TRD.md`: how to build it (architecture, data model, API, sync, security)
- `docs/TASKS.md`: the task list. Work on one task at a time.

If code and the TRD disagree, the TRD wins unless the task says otherwise.
If something is unclear or missing, ask. Do not guess.

## 2. Stack (do not change without asking)

- Frontend: React, TypeScript (strict), Vite, vite-plugin-pwa (Workbox), Dexie (IndexedDB), TanStack Query,
  React Router, React Hook Form + Zod, react-i18next, Tailwind CSS
- Backend: Java 21, Spring Boot 3.x (Web, Data JPA, Security, Validation), PostgreSQL, Flyway,
  springdoc-openapi, Bucket4j
- Auth: Google Sign-In. The server verifies the Google ID token, then issues its own short-lived JWT and a rotating refresh token.
- Tests: JUnit 5 + Testcontainers, Vitest + Testing Library, Playwright
- Hosting: Vercel or Netlify (web), Render or Railway (api), Neon (database)

Do not add a library, framework or service without asking first and explaining why.
Pin dependency versions. Do not upgrade major versions unprompted.

## 3. Repository layout

```text
attendance/
├─ web/                  React + Vite + TypeScript PWA
│  ├─ src/{features,db,sync,i18n,components,lib}
│  └─ public/            manifest, icons
├─ api/                  Spring Boot
│  ├─ src/main/java/.../{auth,org,site,worker,attendance,payroll,sync,notification,report,email,audit}
│  └─ src/main/resources/db/migration     Flyway migrations
├─ shared/fixtures/      payroll test vectors used by both web and api
├─ docs/                 PRD.md, TRD.md, TASKS.md
├─ docker-compose.yml    local PostgreSQL
└─ .github/workflows/
```

Backend code is packaged by feature. Each feature uses controller, service, repository.
Keep DTOs separate from entities.

## 4. Commands

Confirm these match the repo and fix this section if they do not.

```text
docker compose up -d              start local PostgreSQL
cd api && ./mvnw spring-boot:run  run the API
cd api && ./mvnw verify           backend lint, tests (Testcontainers)
cd web && npm install
cd web && npm run dev             run the PWA
cd web && npm run lint
cd web && npm run typecheck
cd web && npm run test            Vitest
cd web && npm run e2e             Playwright
```

Before saying a task is done, run lint, typecheck and tests for every part you touched.

## 5. Hard rules (never break these)

### Money and calculations

- Money is NUMERIC(12,2) in the database and BigDecimal in Java. In TypeScript use integer paise or a decimal library. Never floating point.
- Round HALF_UP to 2 decimals once, at the end of each worker's period total.
- Payroll logic exists twice: Java (authoritative) and TypeScript (offline preview). Both must pass the same cases in
  `shared/fixtures/payroll.json`. If you change one, change the other and the fixtures.

### Tenancy and security

- Every table has `organization_id`. Every query is scoped by the tenant taken from the authenticated user.
  Never take the tenant, role or user id from a request parameter or body.
- Verify the Google ID token fully: signature, issuer, audience, expiry, `email_verified`. Only invited emails may sign in.
- Never log tokens, secrets or personal data (phone numbers, emails). Log ids only.
- Secrets live in environment variables only. Never commit `.env`. Keep `.env.example` up to date.
- Validate all input: Bean Validation on the API, Zod on the client. Use parameterized queries only.
- Every new endpoint needs authorization checks and a test proving another tenant cannot access its data.

### Data

- IDs are UUIDs, generated on the client for records created offline.
- Synced tables use soft delete (`deleted_at`). Never hard delete synced data.
- Timestamps are UTC (`timestamptz`). `work_date` is a plain DATE in the organization's time zone (default Asia/Kolkata).
- Advances and payments are append-only. Corrections are new reversing rows.
- Never edit an applied Flyway migration. Add a new one named `V<number>__description.sql`.
- Attendance is unique on `(worker_id, site_id, work_date)`.

### Offline-first and sync

- The UI reads from IndexedDB only. Writes go to IndexedDB and the outbox first, never straight to the API.
- Sync is automatic: about 2 seconds after a save, on app open, on focus, when the browser comes online, and every 60 seconds.
  There is no manual sync button. A Retry button appears only for items that Need attention.
- Every mutation carries a `mutationId`. The server must handle a repeated `mutationId` safely (idempotent).
- Attendance conflicts are never overwritten silently. A stale `baseVersion` returns a conflict and the supervisor chooses.
- Sync status colors, always shown with an icon and a text label:
  - Synced: green, check icon
  - Syncing: blue, spinning arrows
  - Not synced: amber, clock icon, dashed outline
  - Needs attention: red, warning icon
- Attendance status colors (Present, Absent, Half-day, Leave) and sync colors must never share a visual channel.
  Sync colors live only in the badge, the outline and the top status bar.

### UI

- Mobile first, one-handed use, tap targets at least 48 px, high contrast for sunlight.
- Attendance entry is one tap per worker, plus a "Mark all present" action.
- All user-facing text goes through react-i18next keys. Only `en.json` for now; Hindi and Odia come later.
  No hard-coded strings and no building sentences by concatenation.

### Monthly PDF report and email

- The report job runs on the 1st of each month (default 06:00 organization time) for the previous month.
- It must be idempotent: unique per organization, month and version. A second run or a second instance must not send duplicates.
- It must retry failures, mark the report FAILED after the last retry, and catch up any missed month on startup and hourly.
- Embed Noto Sans fonts (Latin, Devanagari, Odia) in the PDF. Test with English, Hindi and Odia worker names.
- The wage summary page is off by default.
- Send email through the provider's HTTPS API. API keys come from environment variables.

## 6. Out of scope (do not build)

Selfie photo check-in, GPS geofencing, QR check-in, worker login, online payments,
translated content beyond the i18n structure, native mobile apps.

## 7. How to work

1. Read the task in `docs/TASKS.md` and the TRD sections it references.
2. Plan first. Write a short plan (files to change, approach, risks, questions) and wait for approval before coding anything larger than a small change.
3. Make the smallest change that meets the acceptance criteria. Do not refactor or reformat unrelated code.
4. Write or update tests in the same change. For payroll and sync logic, write the tests first.
5. Run lint, typecheck and tests, and fix failures. Never disable tests, lint rules or type checks to get green.
6. Finish with a report: what changed, how to verify it, and anything undone or uncertain.
7. If a requirement is ambiguous, ask a question instead of inventing behavior. List any assumption in the pull request description.

## 8. Code style

- TypeScript: `strict` on, no `any` without a comment explaining why, functional React components with hooks,
  one feature per folder under `src/features`.
- Java: constructor injection, no business logic in controllers, no entities returned from controllers,
  errors returned as RFC 7807 problem+json.
- Small functions, clear names, comments only where the reason is not obvious.
- Run the formatter before committing.

## 9. Testing requirements

- Payroll and overtime: unit tests against `shared/fixtures/payroll.json`.
- Sync: tests for duplicate mutations, out-of-order batches, conflicts, tombstones and retries.
- Backend: integration tests with Testcontainers PostgreSQL, including a cross-tenant access test for every endpoint.
- Frontend: Vitest and Testing Library for components. Playwright for end-to-end flows, including offline mode.
- A bug fix includes a test that fails without the fix.

## 10. Git

- One branch per task: `task/T2-05-mark-attendance`.
- Small, focused commits with clear messages.
- The pull request description lists the task id, the acceptance criteria as a checklist, assumptions, and how to test.
- Never force-push `main`. Never commit secrets, build output or large binaries.

## 11. Definition of done

- Every acceptance criterion in the task is met and demonstrated.
- Tests added and passing. Lint and typecheck clean.
- No new dependency unless approved.
- Documentation updated if behavior, setup or the API changed (README, `.env.example`, OpenAPI).
- No TODOs left without a task id.

## 12. Known constraints

- Free-tier hosting means the API may be asleep. Never assume the server is awake. Scheduled jobs need an external trigger
  or an always-on instance.
- iOS Safari has no Background Sync, and web push works only for PWAs installed to the home screen.
  Sync must not depend on Background Sync.
- Browser storage can be cleared. Keep the pending-change count visible and request persistent storage.
