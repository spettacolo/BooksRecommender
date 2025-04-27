package ONA.booksrecommender.server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        Thread serverThread = null;

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Avvia Server");
            System.out.println("2. Ferma Server");
            System.out.println("3. Esci");
            System.out.print("Scelta: ");

            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    if (serverThread == null || !serverThread.isAlive()) {
                        serverThread = new Thread(server);
                        serverThread.start();
                        System.out.println("Server avviato!");
                    } else {
                        System.out.println("Server già attivo.");
                    }
                    break;
                case "2":
                    if (serverThread != null && serverThread.isAlive()) {
                        server.stop();
                        try {
                            serverThread.join(); // aspetta che il server chiuda davvero
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Server fermato!");
                    } else {
                        System.out.println("Server non è attivo.");
                    }
                    break;
                case "3":
                    System.out.println("Uscita...");
                    if (serverThread != null && serverThread.isAlive()) {
                        server.stop();
                        try {
                            serverThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    exit = true;
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }

        scanner.close();
    }
}
