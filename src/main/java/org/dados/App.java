package org.dados;

import java.util.Scanner;

public class App {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Database.init();
        Crud crud = new Crud(sc);
        Acid acid = new Acid(sc);
        menuPrincipal(crud, acid);
    }

    public static void menuPrincipal(Crud crud, Acid acid) {
        while (true) {
            System.out.println("Menu Principal");
            System.out.println("1- Crud");
            System.out.println("2- Acid");
            System.out.println("0- Salir");
            System.out.print("Seleccione una opcion: ");

            String opt = sc.next();
            sc.nextLine();

            if (isInt(opt)) {
                System.out.println("Opcion invalida");
                continue;
            }

            switch (Integer.parseInt(opt)) {
                case 1 -> menuCrud(crud);
                case 2 -> menuAcid(acid);
                case 0 -> {
                    System.out.println("Saliendo...");
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    public static void menuCrud(Crud crud) {
        while (true) {
            System.out.println("Menu CRUD");
            System.out.println("1- Insert");
            System.out.println("2- Select");
            System.out.println("3- Update");
            System.out.println("4- Delete");
            System.out.println("0- Volver");
            System.out.print("Seleccione una opcion: ");

            String opt = sc.next();
            sc.nextLine();

            if (isInt(opt)) {
                System.out.println("Opcion invalida");
                continue;
            }

            switch (Integer.parseInt(opt)) {
                case 1 -> crud.insert();
                case 2 -> crud.select();
                case 3 -> crud.update();
                case 4 -> crud.delete();
                case 0 -> { return; }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    public static void menuAcid(Acid acid) {
        while (true) {
            System.out.println("Menu ACID");
            System.out.println("1- Prestar libro");
            System.out.println("2- Devolver libro");
            System.out.println("3- Libros prestados a usuario");
            System.out.println("0- Volver");
            System.out.print("Seleccione una opcion: ");

            String opt = sc.next();
            sc.nextLine();

            if (isInt(opt)) {
                System.out.println("Opcion invalida");
                continue;
            }

            switch (Integer.parseInt(opt)) {
                case 1 -> acid.prestarLibro();
                case 2 -> acid.devolverLibro();
                case 3 -> acid.librosPrestadosAUsuario();
                case 0 -> { return; }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    public static boolean isInt(String number) {
        try {
            Integer.parseInt(number.trim());
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
