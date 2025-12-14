package org.dados;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;

public class Database {
    private static String url;
    private static String user;
    private static String password;

    public static void init() {
        Properties props = new Properties();

        try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("db.properties not found in src/main/resources");
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load db.properties: " + e.getMessage(), e);
        }

        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");

        if (url == null || url.isBlank()) throw new RuntimeException("Missing db.url in db.properties");
        if (user == null) user = "";
        if (password == null) password = "";
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    public static void testConnection() {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("DB OK (SELECT 1 = " + rs.getInt(1) + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB test failed: " + e.getMessage(), e);
        }
    }

    public static int executeUpdate(String sql, Object... params) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bindParams(ps, params);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB update failed: " + e.getMessage() + " | SQL: " + sql, e);
        }
    }

    // For INSERT ... RETURNING id_xxx
    public static Integer executeUpdateReturnId(String sql, Object... params) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB insert RETURNING failed: " + e.getMessage() + " | SQL: " + sql, e);
        }
    }

    public static void executeQueryAndPrint(String sql, Object... params) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                boolean any = false;
                while (rs.next()) {
                    any = true;
                    for (int c = 1; c <= cols; c++) {
                        System.out.print(md.getColumnLabel(c) + "=" + rs.getObject(c));
                        if (c < cols) System.out.print(" | ");
                    }
                    System.out.println();
                }
                if (!any) System.out.println("(no rows)");
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB query failed: " + e.getMessage() + " | SQL: " + sql, e);
        }
    }

    private static void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;

        for (int i = 0; i < params.length; i++) {
            Object p = params[i];

            switch (p) {
                case null -> {
                    ps.setNull(i + 1, Types.NULL);
                    continue;
                }


                // Allow passing dates as "YYYY-MM-DD" strings
                case String s -> {
                    String trimmed = s.trim();
                    if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        ps.setDate(i + 1, Date.valueOf(LocalDate.parse(trimmed)));
                    } else {
                        ps.setString(i + 1, s);
                    }
                    continue;
                }


                // Allow passing LocalDate directly
                case LocalDate ld -> {
                    ps.setDate(i + 1, Date.valueOf(ld));
                    continue;
                }
                default -> {
                }
            }

            ps.setObject(i + 1, p);
        }
    }
}
