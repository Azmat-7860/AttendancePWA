# Worker Attendance PWA — Technical Requirements Document

Version 0.1 · Draft · November 18, 2026 · Companion to the PRD

## 1. Purpose and Scope

This TRD defines how the Worker Attendance PWA described in the PRD will be built: stack, architecture, data model, API, offline sync, security and deployment.

In scope: the MVP (including automatic sync with visible sync status and a monthly attendance PDF emailed to the owner and accountant) plus two Phase 2 features chosen for design now: check-in/out timestamps with automatic overtime and push notifications.

Out of scope for this version: GPS geofencing, QR check-in, selfie photo at check-in, worker login, translated content (the app is i18n-ready only) and online payments.

## 2. Decisions Summary

| Area | Decision |
| --- | --- |
| Frontend | React + Vite + TypeScript, installable PWA |
| Backend | Spring Boot (Java), REST API |
| Database | PostgreSQL (Neon, free tier to start) |
| Offline | Offline-first storage (IndexedDB + outbox) with automatic background sync; internet is assumed and the outbox is a safety net for outages |
| Auth | Google Sign-In (OAuth 2.0 / OpenID Connect); the API issues its own short-lived tokens |
| Users | Supervisor and owner only; workers do not log in |
| Tenancy | Single contractor now; every table carries organization\_id so multi-tenant is a configuration change, not a rewrite |
| Hosting | Free/low-cost tiers: Vercel or Netlify (frontend), Render or Railway (API), Neon (database) |
| Languages | English first; i18n structure in place for Hindi and Odia |
| Phase 2 in design | Check-in/out timestamps and overtime, push notifications |
| Sync | Automatic after every save, with color-coded status: green synced, blue syncing, amber not synced, red needs attention (always with an icon) |
| Monthly report | The server generates an attendance PDF at each month end and emails it to the owner and the accountant; a copy is kept in the database as backup |

## 3. Assumptions to Confirm

- The app is normally used with an internet connection. Offline support is a safety net for outages, not the main mode.
- Push notifications go to supervisors and owners only (attendance reminders, sync and payroll alerts). A wage-paid push to workers is not possible without worker devices; a payslip shared through the phone's share sheet (WhatsApp) replaces it.
- Monthly report PDFs are stored in PostgreSQL and emailed as attachments to the owner and the accountant.
- The first deployment serves one contractor with roughly 100 to 200 workers and a handful of supervisors.

## 4. Architecture

```text
[React PWA]  ──HTTPS──►  [Spring Boot API]  ──►  [PostgreSQL (Neon)]
 ├ Service worker          ├ Auth (Google ID token → app JWT)
 ├ IndexedDB (Dexie)       ├ Sync endpoints (push / pull)
 ├ Outbox queue            ├ Payroll + overtime service
 ├ Auto-sync engine        ├ Scheduler: reminders + month-end report
 └ Web Push subscription   ├ PDF generator (monthly attendance report)
                           ├ Web Push sender (VAPID)
                           └ Email sender ──► [Email provider] ──► owner / accountant inbox

[Google Identity] ◄── ID token verified against Google public keys ── API
```

The app always reads and writes local IndexedDB first. A sync engine moves changes between IndexedDB and the API automatically in the background, normally within a couple of seconds of each change. Once data has synced, the server is the source of truth.

## 5. Technology Stack

| Layer | Choice | Notes |
| --- | --- | --- |
| UI framework | React + TypeScript + Vite | Fast builds, small bundles |
| PWA | vite-plugin-pwa (Workbox) | Precaches the app shell, runtime caching |
| Local database | IndexedDB via Dexie | Live queries drive the UI |
| Server calls | TanStack Query | Used for sync and online-only calls |
| Forms | React Hook Form + Zod | Shared validation rules |
| i18n | react-i18next | JSON locale files |
| Styling | Tailwind CSS | Large touch targets, high contrast |
| Backend | Java 21, Spring Boot 3.x | Web, Data JPA, Security, Validation |
| Migrations | Flyway | Versioned SQL |
| Database | PostgreSQL | UUID keys, timestamptz |
| Push | Web Push with VAPID keys | Java web-push library |
| Scheduling | Spring @Scheduled (+ ShedLock if more than one instance) | Reminder job |
| PDF generation | OpenPDF (or Apache PDFBox) | Server-side monthly report, embedded Noto fonts |
| Email | Provider HTTPS API (Brevo, Resend or Amazon SES) | Sends the monthly PDF; needs a verified sender domain |
| API docs | springdoc-openapi | Generated from controllers |
| Rate limiting | Bucket4j | Auth and sync endpoints |
| Tests | JUnit 5, Testcontainers, Vitest, Playwright | See section 18 |
| CI/CD | GitHub Actions | Build, test, deploy |
| Errors | Sentry (free tier) | Frontend and backend |

## 6. Frontend Design

### 6.1 PWA shell

- Web app manifest with standalone display, icons and theme color.
- The service worker precaches the app shell so the app opens with no network.
- When a new version is available the app asks the user to reload; it never swaps code in the middle of an edit.

### 6.2 Local data (IndexedDB)

Object stores: workers, sites, attendance, advances, payments, outbox, syncMeta. Every record has a client-generated UUID, an updatedAt time and a syncStatus (synced, syncing, pending or conflict). Screens read only from IndexedDB, so the network never blocks a screen.

### 6.3 State

Dexie live queries feed the UI. TanStack Query handles calls to the API. A small Zustand store holds UI-only state such as the selected site and date.

### 6.4 Internationalization

All strings use react-i18next keys. Ship en.json now; hi.json and or.json are added later without code changes. Dates and numbers use Intl formatting, and sentences are never built by concatenation.

### 6.5 Usability constraints

Tap targets of at least 48 px, one-handed layout, high contrast for sunlight, one tap per worker on the attendance screen, and a Mark all present action.

## 7. Backend Design

- Package by feature: auth, org, site, worker, attendance, payroll, advance, sync, notification, report, email, audit. Each feature follows controller, service, repository.
- DTOs are separate from entities. Errors use RFC 7807 problem+json.
- **Tenant scoping:** every entity has organization\_id. A request-scoped tenant context is derived from the authenticated user, and every query is filtered by it (Hibernate filter or explicit predicates). Tests prove cross-tenant access is impossible.
- **Money:** NUMERIC(12,2) in the database and BigDecimal in Java. Never floating point.
- **Time:** timestamps are stored in UTC (timestamptz). The attendance work\_date is a plain DATE in the organization's time zone (Asia/Kolkata by default) to avoid midnight edge cases.
- **Idempotency:** every mutation carries a client-generated UUID, so replays are safe.

## 8. Authentication and Authorization

1. The user taps Sign in with Google (Google Identity Services in the browser) and receives a Google ID token.
2. The app sends the ID token to POST /api/v1/auth/google.
3. The API verifies signature, issuer, audience (our client ID), expiry and email\_verified using Google's public keys.
4. The API looks up the user by Google subject or email. Only invited emails can sign in: the owner invites supervisors by email, and unknown emails receive 403.
5. The API returns a short-lived access JWT (about 15 minutes) and a rotating refresh token. The refresh token is stored hashed on the server and sent as an HttpOnly Secure cookie.
6. Serve the app and API under one parent domain (for example app.example.com and api.example.com) so the cookie works with SameSite=Lax.
7. Offline use: the app keeps working with its cached session. The refresh token lives long enough (about 30 days) that a supervisor offline for several days is not logged out, and sync resumes after the next refresh.

### Roles and permissions (defaults, adjustable)

| Action | Owner | Supervisor | Accountant (Phase 2) |
| --- | --- | --- | --- |
| Invite users, change roles | Yes | No | No |
| Manage sites | Yes | No | No |
| Manage workers | Yes | Assigned sites | No |
| Mark and edit attendance | Yes | Assigned sites | No |
| Edit a locked period | Yes | No | No |
| Record advances and payments | Yes | View only | View only |
| Reports and exports | Yes | Assigned sites | Yes |

Authorization is enforced with method-level checks (@PreAuthorize) in addition to tenant scoping.

## 9. Data Model

PostgreSQL, snake\_case. Every table has id (UUID, client-generated where data originates offline), organization\_id, created\_at, updated\_at, deleted\_at (soft delete) and server\_version (a bigint from a database sequence, used by sync pull).

| Table | Key columns | Notes |
| --- | --- | --- |
| organizations | name, timezone, currency | One row for now |
| organization\_settings | organization\_id, standard\_hours, ot\_multiplier, week\_off\_day, reminder\_time, report\_run\_time | Defaults 8 hours and 1.5 |
| users | organization\_id, google\_sub, email, name, role, active | Email unique per organization |
| sites | organization\_id, name, address, active |  |
| workers | organization\_id, name, phone, photo\_key, skill, pay\_type, rate, active | pay\_type: DAILY, HOURLY or MONTHLY |
| worker\_sites | worker\_id, site\_id, from\_date, to\_date | Assignment history |
| attendance | worker\_id, site\_id, work\_date, status, in\_time, out\_time, ot\_minutes, note, marked\_by | Unique on worker\_id, site\_id, work\_date |
| advances | worker\_id, amount, advance\_date, note, recovered\_amount | Append-only |
| payments | worker\_id, period\_start, period\_end, gross, deductions, net, paid\_amount, paid\_on, mode | Append-only |
| payroll\_periods | organization\_id, start\_date, end\_date, status, locked\_by | status: OPEN or LOCKED |
| push\_subscriptions | user\_id, endpoint, p256dh, auth, user\_agent | One row per user device |
| refresh\_tokens | user\_id, token\_hash, expires\_at, revoked\_at | Hashed, rotated |
| processed\_mutations | mutation\_id, organization\_id, result | Idempotency record for sync |
| audit\_log | organization\_id, entity, entity\_id, action, user\_id, old\_value, new\_value | JSONB values |
| report\_recipients | organization\_id, email, name, active | Owner and accountant addresses |
| monthly\_reports | organization\_id, period\_month, version, status, pdf\_data, checksum, generated\_at, sent\_at, attempts, last\_error | Unique on organization\_id, period\_month, version; status: PENDING, SENT or FAILED; PDF stored as bytea |

Indexes: (organization\_id, server\_version) on every synced table, (organization\_id, work\_date) and (worker\_id, work\_date) on attendance.

## 10. API Design

Base path /api/v1, JSON, Bearer access token.

| Endpoint | Purpose |
| --- | --- |
| POST /auth/google | Exchange a Google ID token for app tokens |
| POST /auth/refresh | Rotate the refresh token, return a new access token |
| POST /auth/logout | Revoke the refresh token |
| GET /me | Current user, role and organization |
| POST /sync/push | Upload a batch of queued mutations |
| GET /sync/pull?cursor= | Fetch changes after a cursor |
| GET /reports/payroll?from=&to= | Authoritative payroll figures for a period |
| POST /payroll/periods/{id}/lock | Lock a period (owner) |
| GET /reports/monthly | List monthly reports with status (SENT or FAILED) and version |
| POST /push/subscribe | Register a push subscription |
| DELETE /push/subscribe | Remove a push subscription |
| GET /health | Liveness and readiness |
| GET /reports/monthly/{yyyy-mm}/pdf | Download a stored monthly PDF (Owner or Accountant) |
| POST /reports/monthly/{yyyy-mm}/send | Regenerate and resend a monthly report (Owner) |
| GET and PUT /settings/report-recipients | View and manage who receives the monthly report (Owner) |

Attendance, workers, sites and advances travel through sync endpoints so that offline and online paths are the same. On-screen reports and ad-hoc CSV or PDF exports are generated on the device from local data. The month-end PDF is generated by the server (section 13).

## 11. Offline-First Sync Design

### 11.1 Principles

- The UI reads and writes only IndexedDB.
- Every change is also appended to a persistent outbox.
- Mutations are idempotent, and the server is authoritative.

### 11.2 Mutation shape

```json
{
  "mutationId": "uuid",
  "entity": "attendance",
  "entityId": "uuid",
  "op": "upsert",
  "baseVersion": 41,
  "payload": { },
  "clientTime": "2026-11-18T09:12:00Z"
}
```

### 11.3 Push

The client sends mutations in order, in batches of up to 50. The server processes each in a transaction and skips any mutationId it has already seen. The response lists a result per mutation: applied, conflict or rejected, with the current server record.

### 11.4 Pull

The client sends the last server\_version it has seen as a cursor. The server returns records with a higher server\_version, including deleted records as tombstones, one page at a time. The client applies them and stores the new cursor. Pull runs automatically after each push, on app open, on regaining connectivity and every 60 seconds while the app is open (see 11.7).

### 11.5 Conflict rules

| Data | Rule |
| --- | --- |
| Attendance (same worker and date edited on two devices) | If baseVersion is stale, the server keeps its record and returns a conflict. The app shows both values and the supervisor chooses. No silent overwrites. |
| Workers and sites | Field-level last write wins |
| Advances and payments | Append-only. Corrections are new reversing entries |
| Locked period | Server rejects with PERIOD\_LOCKED unless the user is the owner |

### 11.6 Failure handling

- Retry with exponential backoff and jitter (2 seconds up to 5 minutes).
- The outbox is persistent and survives app restarts.
- Sync is fully automatic; there is no manual sync step. A Retry button appears only when an item needs attention (see 11.7).
- Background Sync API is used where supported (Chromium on Android). iOS Safari does not support it, so sync runs on app open, focus and online events.

### 11.7 Automatic sync and sync status colors

Syncing is automatic. Users never press a sync button. The sync engine runs:

- About 2 seconds after every save (changes made within that window are sent together).
- When the app opens, when the tab regains focus, and when the browser reports it is online again.
- Every 60 seconds while the app is open, to pull changes made on other devices and to retry anything that failed.

The app is designed for normal internet use. The outbox is a safety net: if the connection drops, entries stay on the device marked Not synced and upload by themselves when the connection returns.

| State | Color | Marker (never color alone) | Meaning |
| --- | --- | --- | --- |
| Synced | Green | Check icon | Saved on the server |
| Syncing | Blue | Spinning arrows | Upload in progress |
| Not synced | Amber | Clock icon and dashed outline | Saved on this device only; uploads automatically |
| Needs attention | Red | Warning icon | Conflict or server rejection; the supervisor must review |

Where the colors appear:

- Every attendance row and worker card carries a small sync badge in its corner. Not synced rows also get a dashed amber outline so they stand out at a glance.
- A bar at the top of the app summarizes everything: green All data synced, amber N changes not synced, red N need attention.
- A change that stays Not synced for more than 10 minutes while the device reports it is online is promoted to Needs attention (proposed threshold).
- Attendance status colors (Present, Absent, Half-day, Leave) live in the status chip. Sync colors live only in the badge, the outline and the top bar, so the two meanings are never confused.
- Every color is paired with an icon and a text label, so it stays readable in sunlight and for color-blind users.

### 11.8 Data volume

100 workers over a year is 100 × 365 = 36,500 attendance rows, which is small. The device keeps the last 12 months locally and loads older months on demand.

## 12. Payroll and Overtime Calculation

### Rules

- Day value: Present = 1, Half-day = 0.5, Absent = 0, Leave = 0 (paid leave can be added later).
- Hourly rate for overtime = daily wage ÷ standard hours (default 8).
- Overtime pay = overtime hours × hourly rate × overtime multiplier (default 1.5).
- Overtime hours come from check-out time minus check-in time minus standard hours, rounded down to 15-minute steps (configurable). In the MVP, the supervisor enters overtime hours directly.
- Hourly workers: worked hours × rate, plus overtime beyond standard hours.
- Monthly workers: salary ÷ calendar days in the month × payable days (to confirm).
- Gross = day pay + overtime pay. Net = gross − advance recoveries.
- All amounts use BigDecimal and are rounded HALF\_UP to 2 decimals at the end of each worker's period total.

The same logic is implemented twice: in TypeScript for the offline preview and in Java as the authoritative calculation. Both run against the same JSON test fixtures so results cannot drift.

### Worked example

Inputs: daily wage ₹600, standard 8 hours, multiplier 1.5, 24 present days, 2 half-days, 6 overtime hours in the period, advances recovered ₹3,000.

| Step | Calculation | Result |
| --- | --- | --- |
| Present days | 24 × 600 | ₹14,400 |
| Half-days | 2 × 0.5 × 600 | ₹600 |
| Hourly rate | 600 ÷ 8 | ₹75 |
| Overtime pay | 6 × 75 × 1.5 | ₹675 |
| Gross | 14,400 + 600 + 675 | ₹15,675 |
| Net | 15,675 − 3,000 | ₹12,675 |

## 13. Monthly Attendance PDF Report

Purpose: a month-end PDF of attendance that is emailed automatically. It works as a backup of the month's records and as the document the accountant uses.

### 13.1 Schedule and flow

1. A scheduled job runs on the first day of each month (default 06:00 in the organization's time zone) and reports on the previous month. Running after the month closes captures attendance entered late on the last day.
2. The job creates a monthly\_reports row, unique per organization and month. A restart or a second instance therefore cannot send duplicates.
3. The server generates the PDF from synced attendance data and stores it in PostgreSQL (bytea) with a checksum.
4. The server emails the PDF as an attachment to all active recipients.
5. On success the status becomes SENT. On failure it retries 3 times with increasing delays; if it still fails, the status becomes FAILED and the owner gets a push notification and an in-app banner.
6. Catch-up: on startup and every hour, the job also looks for any past month with no SENT report and runs it. This covers a sleeping or restarted free-tier instance.
7. The owner can open Reports, then Monthly, to download any past PDF, regenerate a month, or resend it.

### 13.2 PDF contents

- Header: organization name, month, site name, generated-at time, and a data-as-of line.
- Attendance grid per site: one row per worker and one column per day, with P (present), A (absent), H (half-day) and L (leave). Weekly off days are shaded.
- Per-worker totals: days present, half-days, absences, leave, overtime hours and payable days.
- Site totals and organization totals.
- Optional wage summary page (gross, advances recovered, net per worker), controlled by a setting. It is off by default because the email may reach people who should not see wages.
- Footer: page numbers, report version, and a note that changes made after generation are not reflected.
- Layout: A4 landscape, one section per site, header row repeated on every page.
- Fonts: worker names may be in English, Hindi or Odia, so the PDF embeds Noto Sans fonts for those scripts. Names must not turn into empty boxes.
- Generation runs one site at a time to keep memory use low on a small instance.

### 13.3 Email

- Recipients are stored per organization (the owner, the accountant, any number of addresses) and managed by the owner in Settings.
- Subject: Attendance report, month and year, organization name. Body: a short summary (number of workers, total present days, total overtime hours) with the PDF attached. A corrected report is marked Corrected in the subject.
- Sending uses a transactional email provider (for example Brevo, Resend or Amazon SES) through its HTTPS API. Some free hosts restrict outbound SMTP ports, so the HTTPS API is the safer default.
- Before launch, verify the sender domain (SPF and DKIM) and check the provider's current free-tier limits. Mail from an unverified domain often lands in spam.
- The PDF is also kept in the database, so the report stays available in the app even if the email fails or is lost.

### 13.4 Data completeness

- The report reads only data that has reached the server. Because sync is automatic and the app is used with internet, this is normally everything.
- Two safeguards: supervisors get a reminder on the last day of the month to confirm the sync bar is green, and the PDF shows its data-as-of time.
- If an entry changes after the report is sent, the owner regenerates it. The new file is saved as the next version (v2, v3), and older versions are kept.

### 13.5 Size and retention

- A monthly PDF for about 100 workers is expected to be a few hundred KB (estimate; check with real data). Twelve reports a year is only a few MB, so storing them in PostgreSQL is fine.
- Retention: keep all report PDFs unless the owner deletes one (to confirm).

## 14. Push Notifications

- Web Push API with VAPID keys. Subscriptions are stored per user device.
- The service worker handles the push and notificationclick events.
- A scheduled job checks each organization's reminder time (default 11:00 local). If no attendance is recorded for an active site that day, it sends a push to that site's supervisors.
- Expired subscriptions (HTTP 404 or 410 from the push service) are removed automatically.
- Platform limits: on iOS, web push works only for PWAs installed to the home screen (iOS 16.4 and later). Delivery is best effort, so important states also appear inside the app.

Two more push notifications: on the last day of each month, a reminder to open the app and confirm everything is synced before the monthly report runs, and an alert to the owner if the monthly report fails to send.

## 15. Security

- HTTPS everywhere with HSTS.
- Tenant isolation in every query, with automated tests.
- Google ID token checks: signature, issuer, audience, expiry and email\_verified.
- Access JWTs are short-lived. Refresh tokens are hashed, rotated on use, and reuse is detected and revoked.
- CORS allow-list limited to the frontend origin. The access token is sent in the Authorization header; the refresh cookie is HttpOnly, Secure and SameSite.
- Bean Validation on all inputs, parameterized queries through JPA.
- Rate limiting on auth and sync endpoints.
- Secrets live in platform environment variables, with separate keys per environment.
- Local data: IndexedDB holds business data unencrypted. Signing out clears local data after pending changes have synced or the user confirms discarding them, which matters on shared phones.
- Backups: use the point-in-time restore of the chosen database plan (check its history window) plus a weekly pg\_dump to object storage. Test a restore once before launch.
- Audit log for attendance edits and payroll changes.
- Dependabot for dependency scanning and an OWASP Top 10 review before launch.

Monthly report PDFs contain personal data and are sent by email. Recipients are limited to the list the owner maintains, the PDF download endpoints require the Owner or Accountant role, the wage summary page is off by default, and email provider keys are kept in environment variables.

## 16. Deployment and Environments

| Component | Host | Notes |
| --- | --- | --- |
| Frontend | Vercel or Netlify | Static build, HTTPS, custom domain |
| API | Render or Railway (Docker, Spring Boot) | Free instances sleep when idle and a JVM can start slowly. Offline-first hides this, but move to a small paid instance once real users depend on it |
| Database | Neon PostgreSQL | Keep the Hikari pool small (for example max 5) to respect connection limits |
| Email provider | Brevo, Resend or Amazon SES | HTTPS API; verified sender domain (SPF and DKIM) |
| Scheduled jobs | External cron trigger, or an always-on instance | Free instances sleep, so month-end and reminder jobs need something to wake them. The report job also catches up missed months on startup |

- Environments: local (Docker Compose with PostgreSQL), staging and production.
- JVM tuning for small memory: cap heap with -XX:MaxRAMPercentage and keep dependencies lean. A native image is a later option.
- CI/CD: GitHub Actions builds, tests (with Testcontainers), lints and deploys from the main branch. Flyway migrations run on startup under an advisory lock.

## 17. Observability

Structured JSON logs with a request ID, Spring Boot Actuator health and metrics (secured), Sentry on both frontend and backend, an in-app sync diagnostics screen (pending count, last error), and an uptime check on /health.

### 17.1 Environment-Aware Logging Strategy
- **Zero code changes**: Switching between development and production log verbosity is managed strictly via configuration profiles (`SPRING_PROFILES_ACTIVE=dev` vs `SPRING_PROFILES_ACTIVE=prod`).
- **Log Levels by Profile**:
  - `dev`: Root logger at `INFO`, application package (`com.attendance`) at `DEBUG`, Hibernate SQL queries logged at `DEBUG` with parameter descriptors at `TRACE`.
  - `prod`: Root logger at `WARN`, application package (`com.attendance`) at `INFO`, Hibernate SQL logging turned `OFF`. All `log.debug(...)` calls are automatically and completely suppressed with zero runtime performance cost.
- **Security & PII Rules**: Never log credentials, JWTs, Google tokens, phone numbers, worker names, or email addresses. Log only internal IDs (`worker_id`, `site_id`, `organization_id`, `mutation_id`).
- **Frontend logging**: Web client uses environment guards (`import.meta.env.DEV`) so debug console outputs are eliminated in production builds.

## 18. Testing Strategy

- **Unit:** payroll and overtime calculations against shared JSON fixtures in Java and TypeScript.
- **Backend integration:** JUnit 5 with Testcontainers PostgreSQL, tenant isolation tests, auth tests with mocked Google keys.
- **Sync:** duplicate mutations, out-of-order batches, conflicts, tombstones and retries.
- **Frontend:** Vitest and Testing Library for components.
- **End to end:** Playwright with the network toggled offline (mark attendance offline, go online, verify server state).
- **PWA quality:** Lighthouse audit in CI, plus manual runs on a low-end Android phone and an iPhone.

Report and sync status tests: a golden-file PDF test with English, Hindi and Odia worker names, a scheduler test that runs the job twice and sends only one email, a catch-up test for a missed month, and email provider calls stubbed in CI. A UI test checks that a record shows Not synced after a failed push and Synced after the retry.

## 19. Performance Targets

| Metric | Target |
| --- | --- |
| First load after the first visit, mid-range Android on 3G | Under 3 seconds |
| Attendance screen interactive from cache | Under 1 second |
| Mark attendance for 20 workers | Under 30 seconds |
| Sync success rate for offline entries | Above 99% |
| API response time, warm instance | p95 under 500 ms (proposed) |

## 20. Repository Structure

```text
attendance/
├─ web/                    # React + Vite + TypeScript PWA
│  ├─ src/{features,db,sync,i18n,components,lib}
│  └─ public/              # manifest, icons
├─ api/                    # Spring Boot
│  ├─ src/main/java/.../{auth,org,site,worker,attendance,payroll,sync,notification,report,email,audit}
│  └─ src/main/resources/db/migration   # Flyway
├─ shared/fixtures/        # payroll test vectors used by both sides
├─ docker-compose.yml      # local PostgreSQL
└─ .github/workflows/
```

## 21. Build Plan

| Milestone | Technical scope | Target |
| --- | --- | --- |
| T1 | Repository, CI, Flyway schema, Google auth and roles, worker and site CRUD, PWA shell | Weeks 1–2 |
| T2 | IndexedDB layer, attendance screen, outbox, automatic sync push and pull, sync status colors, conflict handling | Weeks 3–4 |
| T3 | History, payroll and advances, shared calculation fixtures, on-device reports with CSV and PDF export, monthly PDF job and email | Weeks 5–6 |
| T4 | Hardening, deployment, pilot with a real crew | Weeks 7–8 |
| T5 | Check-in/out with overtime, push notifications | Weeks 9–11 |

## 22. Technical Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| JVM cold starts on free hosting | Slow first sync after idle | Offline-first design, retry with backoff, small paid instance later |
| iOS PWA limits (no background sync, push only when installed) | Missed syncs or reminders | Sync on open and focus, in-app indicators, install guide |
| Attendance sync conflicts | Wrong wages | Stale-version conflict prompt, audit log, period locking |
| Browser storage cleared before sync | Lost entries | Frequent sync, visible pending count, request persistent storage with navigator.storage.persist() |
| Monthly report email fails or lands in spam | Accountant does not get the backup copy | Verified sender domain (SPF and DKIM), retries, a FAILED alert, and the PDF is always stored in the app |
| Neon free-tier limits | Outages or throttling | Monitor usage, upgrade path |
| Client and server payroll logic diverge | Disputes | Shared fixtures, server is authoritative |
| Google-only login | A user without a Google account cannot sign in | Owner invites by Google email; add another sign-in method later if needed |
| Scheduled jobs do not run while a free instance sleeps | Month-end report or reminders missed | External cron trigger or an always-on instance; the report job also catches up missed months on startup |

## 23. Open Questions

- Overtime rounding: 15-minute steps assumed.
- Monthly-salary workers: proration rule (calendar days assumed).
- Who may record advances and payments: owner only assumed.
- Monthly report: sent on the 1st at 06:00 for the previous month (assumed), who receives it, and whether to include a wage summary page (off by default).
- Time zone and week-off defaults per site.
- Domain name for the app and API, needed for the cookie setup and for verified email sending.
