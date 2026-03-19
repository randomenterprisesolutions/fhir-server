/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.repository;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

/**
 * CDI producer that selects the appropriate {@link TenantRepository} at startup.
 *
 * <p><b>Selection logic:</b>
 * <ol>
 *   <li>If the environment variable {@code FHIR_ADMIN_DB_URL} is set, a
 *       {@link JdbcTenantRepository} is produced using a Derby embedded
 *       {@link DataSource}.  The URL format is
 *       {@code jdbc:derby:<path>[;create=true]} or
 *       {@code jdbc:derby:memory:<name>[;create=true]} for an in-memory DB.</li>
 *   <li>Otherwise an {@link InMemoryTenantRepository} is produced.  This is
 *       suitable for local development and tests but loses all data on restart.</li>
 * </ol>
 *
 * <p>For production deployments that use PostgreSQL or connect via Liberty
 * JNDI, replace the {@code buildDerbyDataSource} helper with a JNDI lookup:
 * {@code new InitialContext().lookup("jdbc/fhirAdmin")}.
 */
@Dependent
public class TenantRepositoryProducer {

    private static final Logger log = Logger.getLogger(TenantRepositoryProducer.class.getName());

    /**
     * Environment variable name.  When set, JDBC persistence is active.
     * Example values:
     * <ul>
     *   <li>{@code jdbc:derby:memory:fhir-admin;create=true}  (in-memory Derby)</li>
     *   <li>{@code jdbc:derby:/var/data/fhir-admin;create=true}  (file-backed Derby)</li>
     * </ul>
     */
    public static final String FHIR_ADMIN_DB_URL = "FHIR_ADMIN_DB_URL";

    /**
     * Produces the singleton {@link TenantRepository} based on the
     * {@value #FHIR_ADMIN_DB_URL} environment variable.
     *
     * @return {@link JdbcTenantRepository} when the env var is set,
     *         {@link InMemoryTenantRepository} otherwise
     */
    @Produces
    @ApplicationScoped
    public TenantRepository produce() {
        String dbUrl = System.getenv(FHIR_ADMIN_DB_URL);
        if (dbUrl != null && !dbUrl.isEmpty()) {
            log.info("FHIR_ADMIN_DB_URL found — wiring JdbcTenantRepository: " + dbUrl);
            DataSource ds = buildDataSource(dbUrl);
            return new JdbcTenantRepository(ds);
        }
        log.warning("FHIR_ADMIN_DB_URL not set — using InMemoryTenantRepository."
                + " All tenant data will be lost on restart."
                + " Set FHIR_ADMIN_DB_URL to enable JDBC persistence.");
        return new InMemoryTenantRepository();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a driver-agnostic {@link DataSource} from a plain JDBC URL string.
     * The JDBC driver must be on the runtime classpath; this method makes no
     * compile-time reference to any vendor-specific driver class, so the
     * producer remains portable across Derby, PostgreSQL, and other databases.
     *
     * <p>On Java 9+, drivers that provide a {@code META-INF/services} descriptor
     * (Derby, PostgreSQL, etc.) are discovered automatically via {@link java.util.ServiceLoader}.
     */
    private DataSource buildDataSource(final String url) {
        return new DataSource() {
            @Override public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(url);
            }
            @Override public Connection getConnection(String user, String pw) throws SQLException {
                return DriverManager.getConnection(url, user, pw);
            }
            @Override public PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(PrintWriter pw) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException();
            }
            @Override public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new SQLException("Not a wrapper for " + iface);
            }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        };
    }
}
