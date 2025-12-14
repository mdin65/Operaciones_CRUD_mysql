package org.dados;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    public static int executeUpdate(String sql, Object... params) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB update failed: " + e.getMessage(), e);
        }
    }

    public static void executeQueryAndPrint(String sql, Object... params) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                while (rs.next()) {
                    for (int c = 1; c <= cols; c++) {
                        System.out.print(md.getColumnLabel(c) + "=" + rs.getObject(c));
                        if (c < cols) System.out.print(" | ");
                    }
                    System.out.println();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("DB query failed: " + e.getMessage(), e);
        }
    }
}
