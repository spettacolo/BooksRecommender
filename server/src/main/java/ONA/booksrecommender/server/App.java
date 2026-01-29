package ONA.booksrecommender.server;

import java.util.Scanner;

public class App {
    /**
     * Punto di ingresso principale dell'applicazione Server.
     * Gestisce un'interfaccia a riga di comando (CLI) che permette all'amministratore di:
     * <ul>
     * <li>Avviare il server in un thread dedicato.</li>
     * <li>Fermare il server in modo pulito (graceful shutdown).</li>
     * <li>Uscire dall'applicazione garantendo la chiusura delle risorse pendenti.</li>
     * </ul>
     * * Il metodo utilizza un ciclo di controllo basato su {@link Scanner} per interpretare
     * i comandi dell'utente e monitora lo stato del thread del server per evitare
     * avvii multipli o tentativi di arresto di un servizio non attivo.
     *
     * @param args Argomenti passati da riga di comando (non utilizzati).
     */
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
