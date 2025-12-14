package org.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO prestamos (id_usuario, id_libro, fecha_devolucion) VALUES (?, ?, CURRENT_DATE + INTERVAL '7 days')")) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idLibro);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE libros SET disponible = FALSE WHERE id_libro = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Préstamo registrado. Fecha de devolución en 7 días");

        } catch (SQLException e) {
            throw new RuntimeException("Error al prestar libro: " + e.getMessage(), e);
        }
    }

    public void devolverLibro() {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);

            Integer idPrestamo = seleccionarPrestamoActivo(con);
            if (idPrestamo == null) {
                con.rollback();
                return;
            }

            int idLibro;

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_libro FROM prestamos WHERE id_prestamo = ?")) {
                ps.setInt(1, idPrestamo);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    idLibro = rs.getInt("id_libro");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE prestamos SET fecha_devolucion = CURRENT_DATE WHERE id_prestamo = ?")) {
                ps.setInt(1, idPrestamo);
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

    public void librosPrestadosAUsuario() {
        try (Connection con = Database.getConnection()) {

            Integer idUsuario = seleccionarUsuario(con);
            if (idUsuario == null) return;

            String sql =
                    "SELECT p.id_prestamo, l.titulo, l.autor, p.fecha_prestamo, p.fecha_devolucion " +
                            "FROM prestamos p " +
                            "JOIN libros l ON l.id_libro = p.id_libro " +
                            "WHERE p.id_usuario = ? " +
                            "ORDER BY p.fecha_prestamo DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean any = false;

                    while (rs.next()) {
                        any = true;
                        String fechaDev = rs.getDate("fecha_devolucion") == null
                                ? "No devuelto"
                                : rs.getDate("fecha_devolucion").toString();

                        System.out.println(
                                "Préstamo #" + rs.getInt("id_prestamo") +
                                        " | " + rs.getString("titulo") +
                                        " | " + rs.getString("autor") +
                                        " | Prestado: " + rs.getDate("fecha_prestamo") +
                                        " | Devolución: " + fechaDev
                        );
                    }

                    if (!any) {
                        System.out.println("Este usuario no tiene préstamos");
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar préstamos: " + e.getMessage(), e);
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

    private Integer seleccionarPrestamoActivo(Connection con) throws SQLException {
        String sql =
                "SELECT p.id_prestamo, u.nombre, l.titulo " +
                        "FROM prestamos p " +
                        "JOIN usuarios u ON u.id_usuario = p.id_usuario " +
                        "JOIN libros l ON l.id_libro = p.id_libro " +
                        "WHERE l.disponible = FALSE " +
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

            if (ids.isEmpty()) return null;

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
