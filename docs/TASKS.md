# TASKS.md

Task list for the Worker Attendance PWA. Work on one task at a time, in order, and do not start a task before the tasks it depends on are done.
Tick the box only when every acceptance criterion (AC) is met.


---

## T0: Setup and early spikes

- [x] **T0-01 Repo scaffold and tooling** (TRD 20). Depends: none
  - AC: folders `web`, `api`, `shared/fixtures`, `docs` exist; `docker compose up -d` starts PostgreSQL.
  - AC: `web` and `api` both start locally; `GET /health` on the API returns OK.
  - AC: lint, typecheck and test commands from AGENTS.md section 4 all run (even with placeholder tests).
  - AC: `README.md` has exact setup steps; `.env.example` lists every variable with no real secrets.
- [x] **T0-02 CI pipeline** (TRD 16). Depends: T0-01
  - AC: GitHub Actions runs web lint, typecheck and tests, and api `verify` with Testcontainers on every pull request.
  - AC: a failing test blocks the check.
- [x] **T0-03 Payroll fixtures** (TRD 12). Depends: T0-01
  - AC: `shared/fixtures/payroll.json` holds the cases below with inputs and expected outputs.
  - AC: a short README in that folder explains the JSON format.
  - Case A: daily wage 600, standard hours 8, multiplier 1.5, 24 present days, 2 half-days, 6 overtime hours in the period, advances recovered 3,000.
    Expected: day pay 14,400.00; half-day pay 600.00; overtime pay 675.00; gross 15,675.00; net 12,675.00.
  - Case B: daily wage 500, 26 present days, no half-days, no overtime, advances recovered 2,000.
    Expected: gross 13,000.00; net 11,000.00.
  - Case C (overtime rounding): daily wage 600, standard hours 8, multiplier 1.5, one day worked 10 h 20 min, no advances.
    Overtime is 2 h 20 min, rounded down to 15-minute steps = 2 h 15 min = 2.25 h.
    Expected: overtime pay 253.13 (2.25 × 75 × 1.5 = 253.125, rounded HALF_UP); gross 853.13; net 853.13.
- [x] **T0-04 Spike: offline sync** (TRD 11). Depends: T0-01
  - Throwaway code in a `spikes/` folder, not in `web` or `api`.
  - AC: with the network off, mark attendance, close and reopen the app, then go online: the data reaches the server on its own within about 2 seconds.
  - AC: sending the same mutation twice creates one record.
  - AC: a short written result says what worked, what broke, and what to change in the TRD.
- [x] **T0-05 Spike: PDF with Hindi and Odia names** (TRD 13). Depends: T0-01
  - AC: a PDF with a 100-worker by 31-day grid on A4 landscape renders with English, Hindi and Odia names, with no empty boxes.
  - AC: generation time and file size are measured and written down.
  - AC: the result confirms the library choice (OpenPDF or Apache PDFBox) or recommends a change.

## T1: Foundation (weeks 1 and 2)

- [x] **T1-01 Database schema** (TRD 9). Depends: T0-01
  - AC: Flyway `V1` creates every table in TRD section 9 with `organization_id`, `created_at`, `updated_at`, `deleted_at` and `server_version` where specified.
  - AC: unique and index rules from the TRD exist, including attendance unique on `(worker_id, site_id, work_date)`.
  - AC: a Testcontainers test applies all migrations to an empty database.
  - AC: I review the schema before further work builds on it.
- [x] **T1-02 Tenant context** (TRD 7). Depends: T1-01
  - AC: the tenant comes only from the authenticated user; repositories filter by it.
  - AC: a test shows that organization B cannot read or change organization A's rows.
- [x] **T1-03 Google sign-in API** (TRD 8). Depends: T1-02
  - AC: `POST /auth/google` verifies signature, issuer, audience, expiry and `email_verified`.
  - AC: only invited emails get tokens; others get 403.
  - AC: the access JWT lasts about 15 minutes; the refresh token is stored hashed, rotates on use, and is sent as an HttpOnly Secure cookie.
  - AC: `POST /auth/refresh`, `POST /auth/logout` and `GET /me` work. Reusing an old refresh token revokes the session.
  - AC: tests use mocked Google keys and cover bad audience, expired token and unknown email.
- [x] **T1-04 Roles and permissions** (TRD 8). Depends: T1-03
  - AC: Owner and Supervisor permissions follow the TRD table; checks use method-level security.
  - AC: tests prove a Supervisor cannot do Owner-only actions.
- [x] **T1-05 User invitations** (TRD 8). Depends: T1-04
  - AC: the Owner can invite a Supervisor by Google email, list users, and deactivate a user.
  - AC: a deactivated user loses access immediately.
- [x] **T1-06 Sites API** (TRD 10). Depends: T1-04
  - AC: create, edit, deactivate and list sites, scoped to the tenant, with validation and tests.
- [x] **T1-07 Workers API** (TRD 9, 10). Depends: T1-06
  - AC: create, edit, deactivate and list workers with name, phone, skill, pay type (DAILY, HOURLY, MONTHLY) and rate.
  - AC: workers can be assigned to sites with a from-date and to-date.
  - AC: deactivated workers keep history and drop out of the daily list.
- [ ] **T1-08 PWA shell** (TRD 6). Depends: T0-01
  - AC: valid manifest, service worker precaching the app shell, installable on Android Chrome, opens with no network after the first visit.
  - AC: when a new version is available, the app asks the user to reload and never reloads mid-edit.
  - AC: Tailwind, routing, the base layout and i18n with `en.json` are set up.
- [ ] **T1-09 Login screen** (TRD 8). Depends: T1-03, T1-08
  - AC: "Sign in with Google" works end to end; the session survives app restarts; protected routes redirect to login.
  - AC: a supervisor stays signed in while offline for several days using the cached session.
- [ ] **T1-10 Staging deployment** (TRD 16). Depends: T1-09, T0-02
  - AC: web, API and database run on the chosen hosts with environment variables set.
  - AC: web and API share a parent domain so the refresh cookie works.
  - AC: the deploy runs from CI.

## T2: Attendance and sync (weeks 3 and 4)

- [ ] **T2-01 Local database layer** (TRD 6.2). Depends: T1-08
  - AC: Dexie stores for workers, sites, attendance, advances, payments, outbox, syncMeta.
  - AC: every record has a client UUID, `updatedAt` and `syncStatus` (synced, syncing, pending, conflict).
  - AC: screens read through live queries; unit tests cover the data access functions.
- [ ] **T2-02 Sync API** (TRD 11.3 to 11.5). Depends: T1-07
  - AC: `POST /sync/push` takes batches of up to 50 mutations, is idempotent through `processed_mutations`, and returns applied, conflict or rejected for each.
  - AC: `GET /sync/pull?cursor=` returns changes after the cursor in pages, including tombstones.
  - AC: tests cover duplicates, out-of-order batches, stale `baseVersion` conflicts, deletes and a locked period.
- [ ] **T2-03 Client sync engine** (TRD 11.1 to 11.6). Depends: T2-01, T2-02
  - AC: saving writes to IndexedDB and the outbox, then sync starts about 2 seconds later; saves within that window go together.
  - AC: sync also runs on app open, on focus, when the browser goes online, and every 60 seconds.
  - AC: failures retry with exponential backoff and jitter (2 seconds up to 5 minutes); the outbox survives restarts.
- [ ] **T2-04 Initial load** (TRD 11.4). Depends: T2-03
  - AC: first sign-in pulls sites and workers into IndexedDB and shows them without further network calls.
- [ ] **T2-05 Mark Attendance screen** (PRD 6.3, TRD 6). Depends: T2-04
  - AC: choose a site and date; see the active workers for that site; one tap sets a status (Present, Absent, Half-day, Leave).
  - AC: "Mark all present" works and exceptions can be changed after it.
  - AC: overtime hours and a note can be added; past dates can be viewed and backfilled.
  - AC: marking 20 workers takes under 30 seconds on a mid-range phone.
- [ ] **T2-06 Sync status UI** (TRD 11.7). Depends: T2-05
  - AC: each attendance row and worker card shows a sync badge; Not synced rows have an amber dashed outline.
  - AC: the top bar shows green "All data synced", amber "N changes not synced" or red "N need attention".
  - AC: every state has an icon and a text label as well as a color.
  - AC: an item that stays Not synced for more than 10 minutes while the device is online becomes Needs attention.
  - AC: there is no manual sync button; Retry appears only for Needs attention items.
- [ ] **T2-07 Conflict resolution** (TRD 11.5). Depends: T2-06
  - AC: on a conflict the app shows both versions and the supervisor picks one; the choice is sent as a new mutation.
- [ ] **T2-08 Worker and site screens** (PRD 6.2). Depends: T2-04
  - AC: list, search, add, edit, deactivate and assign workers; list, add and edit sites; all through the outbox and sync.
- [ ] **T2-09 Attendance history** (PRD 6.4). Depends: T2-05
  - AC: a per-worker calendar with color-coded days and a monthly summary (present, half-days, absent, leave, overtime hours).
- [ ] **T2-10 End-to-end offline test** (TRD 18). Depends: T2-06
  - AC: a Playwright test marks attendance with the network off, reconnects, and checks the server state matches and the badge turns green.

## T3: Payroll and reports (weeks 5 and 6)

- [ ] **T3-01 Payroll calculation, Java** (TRD 12). Depends: T0-03, T1-07
  - AC: all cases in `shared/fixtures/payroll.json` pass; amounts use BigDecimal and round HALF_UP at the end of the worker's total.
- [ ] **T3-02 Payroll calculation, TypeScript** (TRD 12). Depends: T0-03
  - AC: the same fixtures pass in Vitest. A change to the fixtures fails one side if the other is not updated.
- [ ] **T3-03 Advances** (PRD 6.5). Depends: T2-08
  - AC: add and list advances per worker; recovered amount is tracked; records are append-only and corrected by a reversing entry.
- [ ] **T3-04 Payroll screen** (PRD 6.5). Depends: T3-02, T3-03, T2-09
  - AC: pick a period and see per worker: days, overtime, gross, advances recovered and net, plus totals.
  - AC: figures match the server calculation for the same data.
- [ ] **T3-05 Payment records** (PRD 6.5). Depends: T3-04
  - AC: record partial or full payments with date and mode; show the pending balance and payment history.
- [ ] **T3-06 Period locking** (TRD 9, 11.5). Depends: T3-04
  - AC: the Owner can lock a period; edits are rejected with `PERIOD_LOCKED` except by the Owner.
- [ ] **T3-07 On-device reports and export** (PRD 6.7). Depends: T2-09
  - AC: daily, weekly and monthly attendance, per-worker and per-site summaries work offline.
  - AC: export to CSV and PDF, and share through the phone's share sheet.
- [ ] **T3-08 Audit log** (TRD 9, 15). Depends: T2-02
  - AC: attendance edits and payroll changes record who, when, and old and new values; a record's history can be viewed.

## T4: Monthly report, hardening and pilot (weeks 7 and 8)

- [ ] **T4-01 Monthly PDF generator** (TRD 13.2). Depends: T0-05, T2-02
  - AC: one section per site with a worker by day grid (P, A, H, L), weekly offs shaded, per-worker totals, site and organization totals, a data-as-of line, page numbers and report version.
  - AC: A4 landscape with the header repeated on every page; Hindi and Odia names render correctly; generation is done one site at a time to keep memory low.
  - AC: the wage summary page is optional and off by default.
- [ ] **T4-02 Report data and recipients** (TRD 9, 13.3). Depends: T4-01
  - AC: `monthly_reports` and `report_recipients` tables are in use; the Owner manages recipients in Settings.
- [ ] **T4-03 Email sending** (TRD 13.3). Depends: T4-02
  - AC: the PDF is sent as an attachment through the provider's HTTPS API with a summary in the body.
  - AC: failures retry 3 times with increasing delays, then the report becomes FAILED and an in-app banner shows for the Owner.
  - AC: the sender domain is verified (SPF and DKIM) and a real test email reaches an inbox, not spam.
- [ ] **T4-04 Month-end scheduler** (TRD 13.1). Depends: T4-03
  - AC: the job runs on the 1st at 06:00 in the organization's time zone for the previous month.
  - AC: running it twice, or on two instances, sends one email (unique constraint and ShedLock).
  - AC: on startup and hourly it finds any past month without a SENT report and runs it.
  - AC: a protected endpoint lets an external cron service trigger the job.
- [ ] **T4-05 Monthly reports screen** (TRD 13.1, 13.4). Depends: T4-04
  - AC: the Owner sees the list of months with status and version, can download any PDF, and can regenerate and resend; a new version keeps the old one.
- [ ] **T4-06 Security hardening** (TRD 15). Depends: T3-08
  - AC: rate limiting on auth and sync endpoints, a CORS allow-list, security headers and HSTS.
  - AC: a test suite proves cross-tenant access fails on every endpoint.
  - AC: dependency scanning is enabled and an OWASP Top 10 review is done and written down.
- [ ] **T4-07 Backups and restore test** (TRD 15). Depends: T1-10
  - AC: automated backups run (point-in-time restore if the plan allows, plus a weekly `pg_dump`).
  - AC: a restore into a fresh database is performed once and the steps are written in a runbook.
- [ ] **T4-08 Monitoring** (TRD 17). Depends: T1-10
  - AC: Sentry on web and API, structured logs with a request id, secured Actuator health, an uptime check, and an in-app sync diagnostics screen.
- [ ] **T4-09 Always-on API and scheduler check** (TRD 16, 22). Depends: T4-04
  - AC: the API runs on an always-on instance, or an external cron trigger is set up.
  - AC: a test run of the month-end job on staging sends the email on time.
- [ ] **T4-10 Real-device testing** (TRD 18). Depends: T2-10
  - AC: tested on a low-end Android phone and an iPhone; install, offline use, sync colors and reports all work.
  - AC: a Lighthouse PWA and performance audit runs in CI and meets the TRD targets.
- [ ] **T4-11 Pilot** (PRD 12). Depends: T4-01 to T4-10
  - AC: one real crew uses the app for 2 to 4 weeks, including one month-end report received by the accountant.
  - AC: feedback is collected into a prioritized fix list.

## T5: Phase 2 features (weeks 9 and 10)

- [ ] **T5-01 Check-in and check-out times** (TRD 12). Depends: T4-11
  - AC: in and out times are recorded; overtime is computed in 15-minute steps rounded down (configurable) and passes fixture Case C.
- [ ] **T5-02 Push notifications** (TRD 14). Depends: T4-11
  - AC: VAPID keys, subscribe and unsubscribe, expired subscriptions removed on 404 or 410.
  - AC: a daily reminder if no attendance is marked by the set time, a last-day-of-month reminder to confirm sync, and an alert to the Owner when the monthly report fails.
  - AC: behavior on iOS (installed PWA only) is tested and documented.
- [ ] **T5-03 Accountant role** (TRD 8). Depends: T4-11
  - AC: a read-only role that can view and download reports and cannot edit anything; tests prove it.
- [ ] **T5-04 Hindi and Odia** (TRD 6.4). Depends: T4-11
  - AC: `hi.json` and `or.json` added with a language setting; no layout breaks with longer text.
