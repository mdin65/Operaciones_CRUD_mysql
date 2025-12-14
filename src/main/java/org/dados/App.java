package org.dados;

import java.util.Scanner;

public class App {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Database.init();
        Crudo crud = new Crudo(sc);
        menu(crud);
    }

    public static void menu(Crudo crud) {
        mostrarMenu();
        String option = sc.next();

        if (isInt(option)) {
            ejecutarOpcion(Integer.parseInt(option), crud);
        } else {
            System.out.println("Numero no valido");
        }
        menu(crud);
    }

    public static void mostrarMenu() {
        System.out.println("Menu Principal");
        System.out.println("1- Insert");
        System.out.println("2- Select");
        System.out.println("3- Update");
        System.out.println("4- Delete");
        System.out.println("Selecciona una opcion:");
    }

    public static void ejecutarOpcion(int opcion, Crudo crud) {
        switch (opcion) {
            case 1 -> crud.insert();
            case 2 -> crud.select();
            case 3 -> crud.update();
            case 4 -> crud.delete();
            default -> System.out.println("Opcion invalida");
        }
    }

    public static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
