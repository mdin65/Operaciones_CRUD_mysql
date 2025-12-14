package org.dados;

import java.util.Objects;
import java.util.Scanner;

public class Crudo {
    private final Scanner sc;

    public Crudo(Scanner sc) {
        this.sc = sc;
    }

    public void insert() {
        String[] tabla = tabla();
        sc.nextLine();

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        Object[] params = new Object[tabla.length - 1];

        for (int i = 1; i < tabla.length; i++) {
            columns.append(tabla[i]);
            placeholders.append("?");

            System.out.print("Enter " + tabla[i] + ": ");
            params[i - 1] = sc.nextLine();

            if (i < tabla.length - 1) {
                columns.append(", ");
                placeholders.append(", ");
            }
        }

        String sql = "INSERT INTO " + tabla[0] + " (" + columns + ") VALUES (" + placeholders + ")";
        int rows = Database.executeUpdate(sql, params);
        System.out.println("Inserted rows: " + rows);
    }

    public void select() {
        String[] tabla = tabla();
        sc.nextLine();

        System.out.println("1- Select ALL");
        System.out.println("2- Select by ID");
        String opt = sc.nextLine();

        if ("1".equals(opt)) {
            Database.executeQueryAndPrint("SELECT * FROM " + tabla[0]);
        } else if ("2".equals(opt)) {
            System.out.print("Enter id: ");
            String id = sc.nextLine();
            Database.executeQueryAndPrint("SELECT * FROM " + tabla[0] + " WHERE id = ?", id);
        } else {
            System.out.println("Invalid option");
        }
    }

    public void update() {
        String[] tabla = tabla();
        sc.nextLine();

        System.out.print("Enter id to update: ");
        String id = sc.nextLine();

        System.out.println("Choose column to update:");
        for (int i = 1; i < tabla.length; i++) {
            System.out.println(i + "- " + tabla[i]);
        }

        String colOpt = sc.nextLine();
        if (!isInt(colOpt)) {
            System.out.println("Invalid column");
            return;
        }

        int colIndex = Integer.parseInt(colOpt);
        if (colIndex < 1 || colIndex >= tabla.length) {
            System.out.println("Invalid column");
            return;
        }

        String col = tabla[colIndex];
        System.out.print("Enter new value for " + col + ": ");
        String newValue = sc.nextLine();

        String sql = "UPDATE " + tabla[0] + " SET " + col + " = ? WHERE id = ?";
        int rows = Database.executeUpdate(sql, newValue, id);
        System.out.println("Updated rows: " + rows);
    }

    public void delete() {
        String[] tabla = tabla();
        sc.nextLine();

        System.out.print("Enter id to delete: ");
        String id = sc.nextLine();

        String sql = "DELETE FROM " + tabla[0] + " WHERE id = ?";
        int rows = Database.executeUpdate(sql, id);
        System.out.println("Deleted rows: " + rows);
    }

    private String[] tabla() {
        System.out.println("Seleccione una tabla");
        System.out.println("1- Usuario");
        System.out.println("2- Libros");
        System.out.println("3- Prestamos");
        int n = sc.nextInt();

        return switch (n) {
            case 1 -> new String[]{"usuarios", "nombre", "email"};
            case 2 -> new String[]{"libros", "titulo", "autor"};
            case 3 -> new String[]{"prestamos", "fecha_prestamo"};
            default -> tabla();
        };
    }

    private boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
