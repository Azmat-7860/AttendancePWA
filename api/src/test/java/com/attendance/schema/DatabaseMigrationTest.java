package com.attendance.schema;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class DatabaseMigrationTest {

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("attendance_test")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void checkDocker() {
        Assumptions.assumeTrue(isDockerAvailable(), "Docker is not available in the current environment; skipping Testcontainers test.");
    }

    @Test
    @DisplayName("V1 Flyway migration should successfully apply baseline schema to an empty PostgreSQL database")
    void shouldApplyBaselineMigrationCleanly() throws Exception {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");

        // Execute Flyway migration against the empty test database
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        var migrateResult = flyway.migrate();
        assertTrue(migrateResult.success, "Flyway migration should succeed");
        assertEquals(1, migrateResult.migrationsExecuted, "Exactly one baseline migration (V1) should be executed");

        MigrationInfo current = flyway.info().current();
        assertNotNull(current, "Current migration info should not be null");
        assertEquals("1", current.getVersion().getVersion());
        assertEquals("initial schema", current.getDescription());

        // Validate table creation and schema constraints directly via JDBC
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {

            // 1. Verify all 16 tables from TRD Section 9 exist
            Set<String> expectedTables = Set.of(
                    "organizations",
                    "organization_settings",
                    "users",
                    "refresh_tokens",
                    "push_subscriptions",
                    "sites",
                    "workers",
                    "worker_sites",
                    "attendance",
                    "advances",
                    "payments",
                    "payroll_periods",
                    "processed_mutations",
                    "audit_log",
                    "report_recipients",
                    "monthly_reports"
            );

            Set<String> actualTables = new HashSet<>();
            try (ResultSet rs = statement.executeQuery(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")) {
                while (rs.next()) {
                    actualTables.add(rs.getString("table_name"));
                }
            }

            for (String table : expectedTables) {
                assertTrue(actualTables.contains(table), "Missing expected table in schema: " + table);
            }

            // 2. Verify server_version_seq sequence exists
            try (ResultSet rs = statement.executeQuery(
                    "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public' AND sequence_name = 'server_version_seq'")) {
                assertTrue(rs.next(), "Sequence server_version_seq must exist");
            }

            // 3. Verify attendance unique constraint on (worker_id, site_id, work_date)
            try (ResultSet rs = statement.executeQuery("""
                SELECT tc.constraint_name
                FROM information_schema.table_constraints tc
                WHERE tc.table_name = 'attendance'
                  AND tc.constraint_type = 'UNIQUE'
                  AND tc.constraint_name = 'uq_attendance_worker_site_date'
            """)) {
                assertTrue(rs.next(), "Attendance table must have unique constraint on (worker_id, site_id, work_date)");
            }

            // 4. Verify composite indexes exist
            Set<String> expectedIndexes = Set.of(
                    "idx_attendance_org_work_date",
                    "idx_attendance_worker_work_date",
                    "idx_attendance_org_version",
                    "idx_workers_org_version",
                    "idx_sites_org_version"
            );

            Set<String> actualIndexes = new HashSet<>();
            try (ResultSet rs = statement.executeQuery(
                    "SELECT indexname FROM pg_indexes WHERE schemaname = 'public'")) {
                while (rs.next()) {
                    actualIndexes.add(rs.getString("indexname"));
                }
            }

            for (String indexName : expectedIndexes) {
                assertTrue(actualIndexes.contains(indexName), "Missing expected index: " + indexName);
            }
        }
    }
}
