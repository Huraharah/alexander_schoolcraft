package SQLi;

import java.sql.*;

public class LoginService {

    public enum Screen {
        ATTACK, INSECURE_PASS, INSECURE_FAIL, SECURE_PASS, SECURE_FAIL
    }

    public static final class Result {
        public final Screen screen;
        public final String displayQuery;
        public final String data;                 

        public Result(Screen s, String q, String d) {
            this.screen = s; this.displayQuery = q; this.data = d;
        }
        // Backward-compat (optional)
        public Result(Screen s, String q) { this(s, q, ""); }
    }
    // --- Insecure: builds SQL via concatenation (vulnerable) ---
    public Result loginInsecure(String username, String password) {
        // Vulnerable: concat, and SELECT columns so we can show them
        String vulnerableSql =
            "SELECT username, passwd, secret " +
            "FROM dbo.Users " +
            "WHERE username='" + username + "' AND passwd='" + password + "';";

        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(vulnerableSql)) {

            Table t = buildTable(rs, "username", "passwd", "secret");

            // If multiple rows came back, it’s effectively a dump (tautology/UNION), treat as attack
            if (t.rows() > 1 || looksLikeInjection(username, password)) {
                return new Result(Screen.ATTACK, vulnerableSql, t.text());
            }

            // Single row = legit insecure success
            if (t.rows() == 1) {
                return new Result(Screen.INSECURE_PASS, vulnerableSql, t.text());
            }

            // 0 rows
            if (looksLikeInjection(username, password)) {
                // Make clear the attempt didn’t yield rows (e.g., comment without match)
                return new Result(Screen.ATTACK, vulnerableSql, "(no rows returned)");
            }
            return new Result(Screen.INSECURE_FAIL, vulnerableSql, "(no rows)");
        } catch (SQLException ex) {
            // If it *looks* like injection (broken quotes, etc.), do an explicit leak to illustrate impact
            if (looksLikeInjection(username, password)) {
                String leak = "SELECT username, passwd, secret FROM dbo.Users ORDER BY username;";
                try (Connection c2 = Db.get();
                     Statement st2 = c2.createStatement();
                     ResultSet rs2 = st2.executeQuery(leak)) {
                    Table t2 = buildTable(rs2, "username", "passwd", "secret");
                    return new Result(Screen.ATTACK, leak, t2.text());
                } catch (SQLException ex2) {
                    return new Result(Screen.ATTACK, leak, "(leak failed: " + ex2.getMessage() + ")");
                }
            }
            return new Result(Screen.INSECURE_FAIL, vulnerableSql, "(error: " + ex.getMessage() + ")");
        }
    }

    // --- Secure: PreparedStatement (no concatenation) ---
    public Result loginSecure(String username, String password) {
        // Return actual columns; ISNULL keeps display tidy if secret is NULL
        String sql =
            "SELECT username, ISNULL(CAST(secret AS VARCHAR(100)),'(null)') AS secret " +
            "FROM dbo.Users WHERE username=? AND passwd=?";
        String display = "[Prepared] " + sql + " | params: [username='" + username +
                         "', passwd='" + password + "']";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String u  = rs.getString("username");
                    String s  = rs.getString("secret");

                    // simple, monospace-friendly block (works great in a Label with wrap)
                    String data = """
                                  username | secret
                                  -----------------
                                  %s | %s
                                  """.formatted(u, s);

                    return new Result(Screen.SECURE_PASS, display, data);
                }
            }
        } catch (SQLException e) {
            return new Result(Screen.SECURE_FAIL,
                    display + "  // ERROR: " + e.getMessage(),
                    "(error)");
        }

        // No row matched (bad creds). If it *looks* like injection, call it out.
        if (looksLikeInjection(username, password)) {
            return new Result(Screen.SECURE_FAIL,
                    display + "  // Injection blocked by parameters",
                    "(no rows)");
        }
        return new Result(Screen.SECURE_FAIL, display, "(no rows)");
    }


    // Basic patterns for the demo
    private boolean looksLikeInjection(String u, String p) {
        String s = (u + " " + p).toLowerCase();
        return s.contains("' or '1'='1")
            || s.contains("' or 1=1")
            || s.contains("--")
            || s.contains(" union all ")
            || s.contains(" union ");
    }

    // Simple, monospace-friendly table for the #data label
    private static final int MAX_DISPLAY_ROWS = 500; // guardrail for huge tables (tweak or remove)

    private static record Table(String text, int rows, boolean truncated) {}

    private Table buildTable(ResultSet rs, String... cols) throws SQLException {
        String header = String.join(" | ", cols);

        StringBuilder sb = new StringBuilder(Math.max(128, header.length() * 2));
        sb.append(header).append('\n');
        sb.append("-".repeat(header.length())).append('\n');  // <-- FIX: constant length underline

        int count = 0;
        boolean truncated = false;

        while (rs.next()) {
            // row
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) sb.append(" | ");
                sb.append(String.valueOf(rs.getString(cols[i])));
            }
            sb.append('\n');
            count++;

            if (count >= MAX_DISPLAY_ROWS) {
                truncated = true;
                break;
            }
        }

        if (truncated) {
            sb.append("\n... (display truncated at ").append(MAX_DISPLAY_ROWS).append(" rows)");
        }

        return new Table(sb.toString().trim(), count, truncated);
    }

}


