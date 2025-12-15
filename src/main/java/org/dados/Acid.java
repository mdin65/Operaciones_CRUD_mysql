package org.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Acid {
    private final Scanner sc;

    public Acid(Scanner sc) {
        this.sc = sc;
    }

    public void prestarLibro() {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            Integer idUsuario = seleccionarUsuario(con);
            if (idUsuario == null) {
                con.rollback();
                return;
            }

            Integer idLibro = seleccionarLibroDisponible(con);
            if (idLibro == null) {
                con.rollback();
                return;
            }

            try (PreparedStatement lockLibro = con.prepareStatement(
                    "SELECT disponible FROM libros WHERE id_libro = ? FOR UPDATE")) {
                lockLibro.setInt(1, idLibro);

                try (ResultSet rs = lockLibro.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("El libro no existe");
                        return;
                    }
                    if (!rs.getBoolean("disponible")) {
                        con.rollback();
                        System.out.println("El libro no está disponible");
                        return;
                    }
                }
            }

            LocalDate fechaActual = LocalDate.now(ZoneId.systemDefault());

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO prestamos (id_usuario, id_libro, fecha_prestamo) VALUES (?, ?, ?)")) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idLibro);
                ps.setDate(3, java.sql.Date.valueOf(fechaActual));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE libros SET disponible = FALSE WHERE id_libro = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Préstamo registrado correctamente");

        } catch (SQLException e) {
            throw new RuntimeException("Error al prestar libro: " + e.getMessage(), e);
        }
    }

    public void devolverLibro() {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            Integer idPrestamo = seleccionarPrestamo(con);
            if (idPrestamo == null) {
                con.rollback();
                return;
            }

            int idLibro;

            try (PreparedStatement lockPrestamo = con.prepareStatement(
                    "SELECT id_libro FROM prestamos WHERE id_prestamo = ? AND fecha_devolucion IS NULL FOR UPDATE")) {
                lockPrestamo.setInt(1, idPrestamo);

                try (ResultSet rs = lockPrestamo.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("El préstamo no existe o ya fue devuelto");
                        return;
                    }
                    idLibro = rs.getInt("id_libro");
                }
            }

            LocalDate fechaActual = LocalDate.now(ZoneId.systemDefault());

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE prestamos SET fecha_devolucion = ? WHERE id_prestamo = ?")) {
                ps.setDate(1, java.sql.Date.valueOf(fechaActual));
                ps.setInt(2, idPrestamo);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE libros SET disponible = TRUE WHERE id_libro = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Libro devuelto correctamente");

        } catch (SQLException e) {
            throw new RuntimeException("Error al devolver libro: " + e.getMessage(), e);
        }
    }

    private Integer seleccionarUsuario(Connection con) throws SQLException {
        String sql = "SELECT id_usuario, nombre FROM usuarios ORDER BY id_usuario";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int index = 1;
            List<Integer> ids = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("id_usuario"));
                System.out.println(index + "- " + rs.getString("nombre"));
                index++;
            }

            if (ids.isEmpty()) return null;

            System.out.println("0- Volver");

            int opcion = readInt("Seleccione usuario: ");
            if (opcion == 0 || opcion > ids.size()) return null;

            return ids.get(opcion - 1);
        }
    }

    private Integer seleccionarLibroDisponible(Connection con) throws SQLException {
        String sql = "SELECT id_libro, titulo, autor FROM libros WHERE disponible = TRUE ORDER BY id_libro";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int index = 1;
            List<Integer> ids = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("id_libro"));
                System.out.println(index + "- " + rs.getString("titulo") + " (" + rs.getString("autor") + ")");
                index++;
            }

            if (ids.isEmpty()) return null;

            System.out.println("0- Volver");

            int opcion = readInt("Seleccione libro: ");
            if (opcion == 0 || opcion > ids.size()) return null;

            return ids.get(opcion - 1);
        }
    }

    private Integer seleccionarPrestamo(Connection con) throws SQLException {
        String sql =
                "SELECT p.id_prestamo, u.nombre, l.titulo " +
                        "FROM prestamos p " +
                        "JOIN usuarios u ON u.id_usuario = p.id_usuario " +
                        "JOIN libros l ON l.id_libro = p.id_libro " +
                        "WHERE p.fecha_devolucion IS NULL " +
                        "ORDER BY p.fecha_prestamo";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int index = 1;
            List<Integer> ids = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("id_prestamo"));
                System.out.println(index + "- " + rs.getString("nombre") + " | " + rs.getString("titulo"));
                index++;
            }

            if (ids.isEmpty()) {
                System.out.println("No hay préstamos pendientes de devolución");
                return null;
            }

            System.out.println("0- Volver");

            int opcion = readInt("Seleccione préstamo: ");
            if (opcion == 0 || opcion > ids.size()) return null;

            return ids.get(opcion - 1);
        }
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine();
        while (!isInt(s)) {
            System.out.print(prompt);
            s = sc.nextLine();
        }
        return Integer.parseInt(s.trim());
    }

    private boolean isInt(String number) {
        try {
            Integer.parseInt(number.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
