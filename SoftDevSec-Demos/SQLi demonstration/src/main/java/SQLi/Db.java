package SQLi;

import java.sql.*;

public final class Db {
    
private static final String SERVER   = requireCfg("AZ_SQL_SERVER");
private static final String DATABASE = requireCfg("AZ_SQL_DB");
private static final String USER     = requireCfg("AZ_SQL_USER");
private static final String PASS     = requireCfg("AZ_SQL_PASS");

private static String requireCfg(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
        throw new IllegalStateException(
            "Missing required environment variable: " + key
        );
    }
    return value;
}

    // Azure-recommended flags
    private static final String URL = "jdbc:sqlserver://" + SERVER + ":1433;"
            + "database=" + DATABASE + ";"
            + "encrypt=true;"
            + "trustServerCertificate=false;"
            + "hostNameInCertificate=*.database.windows.net;"
            + "loginTimeout=30;";

    private Db() {}

    /** Get a live JDBC connection (caller closes). */
    public static Connection get() throws SQLException {
        DriverManager.setLoginTimeout(15);
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Quick connectivity check: SELECT 1 */
    public static boolean ping() {
        try (Connection c = get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean wake_ping() {
        try (Connection c = get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
    
    // Call this once at app startup (after warm-up): Db.resetFreshDatabase();
    public static void resetFreshDatabase() throws SQLException {
        try (Connection c = get()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                // 1) Drop views referencing tables (safer if SCHEMABINDING was used somewhere)
                st.execute(dropAllViewsSql("dbo"));

                // 2) Drop all foreign keys, then all tables in schema
                st.execute(dropAllForeignKeysSql("dbo"));
                st.execute(dropAllTablesSql("dbo"));

                // 3) Recreate + seed Users
                st.execute(createUsersSql());
                st.execute(seedUsersSql());
            }
            c.commit();
        } catch (SQLException e) {
            // Best-effort rollback
            try { get().rollback(); } catch (Exception ignore) {}
            throw e;
        } finally {
            try { get().setAutoCommit(true); } catch (Exception ignore) {}
        }
    }

    // ---------- dynamic SQL builders ----------

    private static String dropAllViewsSql(String schema) {
        return ("""
            DECLARE @schema sysname = N'%s';
            DECLARE @sql nvarchar(max);
            SELECT @sql = STRING_AGG('DROP VIEW ' + QUOTENAME(s.name) + '.' + QUOTENAME(v.name) + ';', CHAR(10))
            FROM sys.views v
            JOIN sys.schemas s ON v.schema_id = s.schema_id
            WHERE s.name = @schema;
            IF @sql IS NOT NULL EXEC sp_executesql @sql;
            """).formatted(schema);
    }

    private static String dropAllForeignKeysSql(String schema) {
        return ("""
            DECLARE @schema sysname = N'%s';
            DECLARE @sql nvarchar(max);
            SELECT @sql = STRING_AGG(
                'ALTER TABLE ' + QUOTENAME(s.name) + '.' + QUOTENAME(t.name) +
                ' DROP CONSTRAINT ' + QUOTENAME(fk.name) + ';', CHAR(10))
            FROM sys.foreign_keys fk
            JOIN sys.tables t  ON fk.parent_object_id = t.object_id
            JOIN sys.schemas s ON t.schema_id = s.schema_id
            WHERE s.name = @schema;
            IF @sql IS NOT NULL EXEC sp_executesql @sql;
            """).formatted(schema);
    }

    private static String dropAllTablesSql(String schema) {
        return ("""
            DECLARE @schema sysname = N'%s';
            DECLARE @sql nvarchar(max);
            SELECT @sql = STRING_AGG(
                'DROP TABLE ' + QUOTENAME(s.name) + '.' + QUOTENAME(t.name) + ';', CHAR(10))
            FROM sys.tables t
            JOIN sys.schemas s ON t.schema_id = s.schema_id
            WHERE s.name = @schema;
            IF @sql IS NOT NULL EXEC sp_executesql @sql;
            """).formatted(schema);
    }

    private static String createUsersSql() {
        return """
            CREATE TABLE dbo.Users (
            username VARCHAR(50)  NOT NULL PRIMARY KEY,
            passwd   VARCHAR(255) NOT NULL,
            secret   VARCHAR(100) NOT NULL
                CONSTRAINT DF_Users_secret DEFAULT ('SECRET-42')
            );
            """;
    }

    private static String seedUsersSql() {
        return """
            INSERT INTO dbo.Users (username, passwd, secret) VALUES
            ('admin','admin123','ADMIN-SECRET'),
            ('alice','password1','ALICE-SECRET'),
            ('bob','hunter2','BOB-SECRET');
            """;
    }

    /** For logging configs without leaking secrets. */
    public static String info() {
        return "sqlserver://" + SERVER + "/"+ DATABASE + " (user=" + mask(USER) + ")";
    }

    // ---- helpers ----
    private static String getCfg(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) v = System.getProperty(key, def);
        return v;
    }
    private static String mask(String s) {
        if (s == null) return "<null>";
        int at = s.indexOf('@');
        return (at > 0) ? s.substring(0, at) + "@***" : s;
    }

}
