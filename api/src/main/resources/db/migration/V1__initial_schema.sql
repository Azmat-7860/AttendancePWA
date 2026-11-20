-- =============================================================================
-- V1__initial_schema.sql
-- Worker Attendance PWA - Baseline schema definition (TRD Section 9)
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Monotonic sequence for change tracking across all synced tables
CREATE SEQUENCE IF NOT EXISTS server_version_seq START WITH 1 INCREMENT BY 1;

-- -----------------------------------------------------------------------------
-- 1. Organizations
-- -----------------------------------------------------------------------------
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID,
    name VARCHAR(255) NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- Ensure organization_id self-references for consistency if populated
ALTER TABLE organizations
    ADD CONSTRAINT fk_organizations_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE;

-- -----------------------------------------------------------------------------
-- 2. Organization Settings
-- -----------------------------------------------------------------------------
CREATE TABLE organization_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    standard_hours NUMERIC(4,2) NOT NULL DEFAULT 8.00,
    ot_multiplier NUMERIC(3,2) NOT NULL DEFAULT 1.50,
    week_off_day VARCHAR(16) NOT NULL DEFAULT 'SUNDAY',
    reminder_time TIME NOT NULL DEFAULT '18:00:00',
    report_run_time TIME NOT NULL DEFAULT '06:00:00',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_organization_settings_org UNIQUE (organization_id)
);

-- -----------------------------------------------------------------------------
-- 3. Users (Supervisors and Owners)
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    google_sub VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_users_org_email UNIQUE (organization_id, email)
);

-- -----------------------------------------------------------------------------
-- 4. Refresh Tokens (Hashed, rotated sessions)
-- -----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash)
);

-- -----------------------------------------------------------------------------
-- 5. Push Subscriptions (Web Push / VAPID)
-- -----------------------------------------------------------------------------
CREATE TABLE push_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint TEXT NOT NULL,
    p256dh TEXT NOT NULL,
    auth TEXT NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_push_subscriptions_endpoint UNIQUE (endpoint)
);

-- -----------------------------------------------------------------------------
-- 6. Sites
-- -----------------------------------------------------------------------------
CREATE TABLE sites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- -----------------------------------------------------------------------------
-- 7. Workers
-- -----------------------------------------------------------------------------
CREATE TABLE workers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    photo_key VARCHAR(255),
    skill VARCHAR(64),
    pay_type VARCHAR(16) NOT NULL,
    rate NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- -----------------------------------------------------------------------------
-- 8. Worker Sites (Site assignment history)
-- -----------------------------------------------------------------------------
CREATE TABLE worker_sites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
    site_id UUID NOT NULL REFERENCES sites(id) ON DELETE CASCADE,
    from_date DATE NOT NULL,
    to_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- -----------------------------------------------------------------------------
-- 9. Attendance
-- -----------------------------------------------------------------------------
CREATE TABLE attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
    site_id UUID NOT NULL REFERENCES sites(id) ON DELETE CASCADE,
    work_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    in_time TIMESTAMPTZ,
    out_time TIMESTAMPTZ,
    ot_minutes INTEGER NOT NULL DEFAULT 0,
    note TEXT,
    marked_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_attendance_worker_site_date UNIQUE (worker_id, site_id, work_date)
);

-- -----------------------------------------------------------------------------
-- 10. Advances (Append-only)
-- -----------------------------------------------------------------------------
CREATE TABLE advances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
    amount NUMERIC(12,2) NOT NULL,
    advance_date DATE NOT NULL,
    note TEXT,
    recovered_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- -----------------------------------------------------------------------------
-- 11. Payments (Append-only)
-- -----------------------------------------------------------------------------
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    gross NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    deductions NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    net NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    paid_on TIMESTAMPTZ,
    mode VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- -----------------------------------------------------------------------------
-- 12. Payroll Periods
-- -----------------------------------------------------------------------------
CREATE TABLE payroll_periods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    locked_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_payroll_periods_org_range UNIQUE (organization_id, start_date, end_date)
);

-- -----------------------------------------------------------------------------
-- 13. Processed Mutations (Sync idempotency record)
-- -----------------------------------------------------------------------------
CREATE TABLE processed_mutations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mutation_id UUID NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    result VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_processed_mutations_org_mutation UNIQUE (organization_id, mutation_id)
);

-- -----------------------------------------------------------------------------
-- 14. Audit Log (JSONB change tracking)
-- -----------------------------------------------------------------------------
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    entity VARCHAR(64) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(32) NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    old_value JSONB,
    new_value JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq')
);

-- -----------------------------------------------------------------------------
-- 15. Report Recipients (Monthly report distribution list)
-- -----------------------------------------------------------------------------
CREATE TABLE report_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_report_recipients_org_email UNIQUE (organization_id, email)
);

-- -----------------------------------------------------------------------------
-- 16. Monthly Reports
-- -----------------------------------------------------------------------------
CREATE TABLE monthly_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    period_month VARCHAR(7) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    pdf_data BYTEA,
    checksum VARCHAR(64),
    generated_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    server_version BIGINT NOT NULL DEFAULT nextval('server_version_seq'),
    CONSTRAINT uq_monthly_reports_org_period_version UNIQUE (organization_id, period_month, version)
);

-- =============================================================================
-- Indexes
-- =============================================================================

-- Composite index (organization_id, server_version) on every synced entity
CREATE INDEX idx_organizations_org_version ON organizations (organization_id, server_version);
CREATE INDEX idx_organization_settings_org_version ON organization_settings (organization_id, server_version);
CREATE INDEX idx_users_org_version ON users (organization_id, server_version);
CREATE INDEX idx_refresh_tokens_org_version ON refresh_tokens (organization_id, server_version);
CREATE INDEX idx_push_subscriptions_org_version ON push_subscriptions (organization_id, server_version);
CREATE INDEX idx_sites_org_version ON sites (organization_id, server_version);
CREATE INDEX idx_workers_org_version ON workers (organization_id, server_version);
CREATE INDEX idx_worker_sites_org_version ON worker_sites (organization_id, server_version);
CREATE INDEX idx_attendance_org_version ON attendance (organization_id, server_version);
CREATE INDEX idx_advances_org_version ON advances (organization_id, server_version);
CREATE INDEX idx_payments_org_version ON payments (organization_id, server_version);
CREATE INDEX idx_payroll_periods_org_version ON payroll_periods (organization_id, server_version);
CREATE INDEX idx_processed_mutations_org_version ON processed_mutations (organization_id, server_version);
CREATE INDEX idx_audit_log_org_version ON audit_log (organization_id, server_version);
CREATE INDEX idx_report_recipients_org_version ON report_recipients (organization_id, server_version);
CREATE INDEX idx_monthly_reports_org_version ON monthly_reports (organization_id, server_version);

-- Attendance query optimization indexes (TRD 9: attendance lookups by date and worker)
CREATE INDEX idx_attendance_org_work_date ON attendance (organization_id, work_date);
CREATE INDEX idx_attendance_worker_work_date ON attendance (worker_id, work_date);
