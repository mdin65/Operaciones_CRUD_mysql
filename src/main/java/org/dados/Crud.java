package org.dados;

import java.util.Scanner;

public class Crud {
    private final Scanner sc;

    public Crud(Scanner sc) {
        this.sc = sc;
    }

    public void insert() {
        String[][] tabla = tabla();
        if (tabla == null) return;
        String table = tabla[0][0];
        sc.nextLine();

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        Object[] params = new Object[tabla[0].length - 2];

        int p = 0;
        for (int i = 2; i < tabla[0].length; i++) {
            String col = tabla[0][i];
            columns.append(col);
            placeholders.append("?");

            System.out.print("Ingrese " + col + ": ");
            String input = sc.nextLine();

            if ("prestamos".equals(table) && ("id_usuario".equals(col) || "id_libro".equals(col))) {
                while (isInt(input)) {
                    System.out.print("Ingrese un número válido para " + col + ": ");
                    input = sc.nextLine();
                }
                params[p++] = Integer.parseInt(input);
            } else if ("libros".equals(table) && "disponible".equals(col)) {
                while (!input.equals("0") && !input.equals("1")) {
                    System.out.print("Ingrese 1 (verdadero) o 0 (falso) para disponible: ");
                    input = sc.nextLine();
                }
                params[p++] = input.equals("1");
            } else {
                params[p++] = input;
            }

            if (i < tabla[0].length - 1) {
                columns.append(", ");
                placeholders.append(", ");
            }
        }

        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";
        int rows = Database.executeUpdate(sql, params);
        System.out.println("Filas insertadas: " + rows);
    }

    public void select() {
        String[][] tabla = tabla();
        if (tabla == null) return;
        String table = tabla[0][0];
        String pk = tabla[0][1];
        sc.nextLine();

        System.out.println("1- Mostrar todos");
        System.out.println("2- Buscar por ID");
        String opt = sc.nextLine();

        if ("1".equals(opt)) {
            Database.executeQueryAndPrint("SELECT * FROM " + table);
            return;
        }

        if ("2".equals(opt)) {
            int id = readInt("Ingrese " + pk + ": ");
            Database.executeQueryAndPrint("SELECT * FROM " + table + " WHERE " + pk + " = ?", id);
            return;
        }

        System.out.println("Opción inválida");
    }

    public void update() {
        String[][] tabla = tabla();
        if (tabla == null) return;
        String table = tabla[0][0];
        String pk = tabla[0][1];
        sc.nextLine();

        int id = readInt("Ingrese " + pk + " a modificar: ");

        System.out.println("Seleccione la columna a modificar:");
        int optionNumber = 1;

        for (int i = 2; i < tabla[0].length; i++) {
            System.out.println(optionNumber + "- " + tabla[0][i]);
            optionNumber++;
        }

        boolean isPrestamos = "prestamos".equals(table);
        if (isPrestamos) {
            System.out.println(optionNumber + "- fecha_devolucion");
        }

        String opt = sc.nextLine();
        if (isInt(opt)) {
            System.out.println("Opción inválida");
            return;
        }

        int chosen = Integer.parseInt(opt);

        if (isPrestamos && chosen == optionNumber) {
            System.out.print("Ingrese fecha de devolución (YYYY-MM-DD) o deje vacío para NULL: ");
            String fecha = sc.nextLine();
            Object value = (fecha == null || fecha.isBlank()) ? null : fecha;

            String sql = "UPDATE " + table + " SET fecha_devolucion = ? WHERE " + pk + " = ?";
            int rows = Database.executeUpdate(sql, value, id);
            System.out.println("Filas actualizadas: " + rows);
            return;
        }

        int normalCount = tabla[0].length - 2;
        if (chosen < 1 || chosen > normalCount) {
            System.out.println("Opción inválida");
            return;
        }

        String col = tabla[0][chosen + 1];

        if ("disponible".equals(col)) {
            System.out.print("Ingrese 1 (verdadero) o 0 (falso) para disponible: ");
        } else {
            System.out.print("Ingrese nuevo valor para " + col + ": ");
        }

        String newValue = sc.nextLine();

        Object newValueObj;
        if ("prestamos".equals(table) && ("id_usuario".equals(col) || "id_libro".equals(col))) {
            while (isInt(newValue)) {
                System.out.print("Ingrese un número válido para " + col + ": ");
                newValue = sc.nextLine();
            }
            newValueObj = Integer.parseInt(newValue);
        } else if ("libros".equals(table) && "disponible".equals(col)) {
            while (!newValue.equals("0") && !newValue.equals("1")) {
                System.out.print("Ingrese 1 (verdadero) o 0 (falso) para disponible: ");
                newValue = sc.nextLine();
            }
            newValueObj = newValue.equals("1");
        } else {
            newValueObj = newValue;
        }

        String sql = "UPDATE " + table + " SET " + col + " = ? WHERE " + pk + " = ?";
        int rows = Database.executeUpdate(sql, newValueObj, id);
        System.out.println("Filas actualizadas: " + rows);
    }

    public void delete() {
        String[][] tabla = tabla();
        if (tabla == null) return;
        String table = tabla[0][0];
        String pk = tabla[0][1];
        sc.nextLine();

        int id = readInt("Ingrese " + pk + " a eliminar: ");
        String sql = "DELETE FROM " + table + " WHERE " + pk + " = ?";
        int rows = Database.executeUpdate(sql, id);
        System.out.println("Filas eliminadas: " + rows);
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine();
        while (isInt(s)) {
            System.out.print(prompt);
            s = sc.nextLine();
        }
        return Integer.parseInt(s.trim());
    }

    private String[][] tabla() {
        System.out.println("Seleccione una tabla");
        System.out.println("1- Usuarios");
        System.out.println("2- Libros");
        System.out.println("3- Préstamos");
        System.out.println("4- Salir");
        int n = sc.nextInt();

        return switch (n) {
            case 1 -> new String[][]{{"usuarios", "id_usuario", "nombre", "email"}};
            case 2 -> new String[][]{{"libros", "id_libro", "titulo", "autor", "disponible"}};
            case 3 -> new String[][]{{"prestamos", "id_prestamo", "id_usuario", "id_libro"}};
            case 4 -> { yield null; }
            default -> tabla();
        };
    }

    private boolean isInt(String number) {
        try {
            Integer.parseInt(number.trim());
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
