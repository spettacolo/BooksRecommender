package ONA.booksrecommender.client;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {
    public static void main(String[] args) {
        String host = "localhost"; // oppure l'IP del server, tipo "192.168.1.100"
        int porta = 1234;

        try (Socket socket = new Socket(host, porta)) {
            System.out.println("Connesso al server su " + host + ":" + porta);

            // 1. Esegui la funzione di popolamento
            populateLibraries(socket);

            String risposta = getString(socket, "get_book;top;business & economics;10");
            //String risposta = getString(socket, "get_book;title;harry potter");
            System.out.println("Server response: " + risposta);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getString(Socket socket, String richiesta) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            /*
            // Scrive un messaggio al server
            out.println("Ciao dal client!");

            // Riceve la risposta
            String risposta = in.readLine();
            System.out.println("Risposta del server: " + risposta);
            */

        //out.println("get_user;luigi");
        //out.println("get_book;title;1 is one");
        //out.println("test_get_book_image");
        //out.println("add_library;test;luigi");
        out.println(richiesta);
        return in.readLine();
    }

    /**
     * Crea 100 librerie (Library_1 a Library_100) e aggiunge 200 libri
     * con ID casuali (da 1 a 50000) a ciascuna.
     *
     * @param socket La connessione socket al server.
     */
    private static void populateLibraries(Socket socket) {
        final int NUM_LIBRARIES = 100;
        final int BOOKS_PER_LIBRARY = 200;
        final int MAX_BOOK_ID = 50000;
        final String USERNAME = "luigi"; // Username fisso per semplicità

        Random random = new Random();

        System.out.println("--- INIZIO POPOLAMENTO DATI ---");

        // 1. Creazione delle 100 librerie
        for (int i = 1; i <= NUM_LIBRARIES; i++) {
            String libraryName = "Library_" + i;
            String createLibraryRequest = "add_library;" + libraryName + ";" + USERNAME;
            try {
                // Non è strettamente necessario leggere la risposta qui
                getString(socket, createLibraryRequest);
                if (i % 10 == 0) {
                    System.out.println("Creata " + libraryName);
                }
            } catch (IOException e) {
                System.err.println("Errore durante la creazione di " + libraryName + ": " + e.getMessage());
                return; // Interrompi in caso di errore di I/O
            }
        }
        System.out.println("Completata la creazione di " + NUM_LIBRARIES + " librerie.");

        // 2. Aggiunta dei 200 libri casuali per ogni libreria
        for (int libraryId = 1; libraryId <= NUM_LIBRARIES; libraryId++) {
            // Per il protocollo, usiamo il library_name come identificativo univoco (se non si vuole usare il library_id)
            String libraryName = "Library_" + libraryId;

            for (int j = 0; j < BOOKS_PER_LIBRARY; j++) {
                // Genera un ID casuale da 1 a MAX_BOOK_ID
                int randomBookId = random.nextInt(MAX_BOOK_ID) + 1;

                // Usiamo il formato: add_book_to_library;<library_name>;<user_username>;<book_id>
                String addBookRequest = "add_book_to_library;" + libraryName + ";" + USERNAME + ";" + randomBookId;

                try {
                    // Non è strettamente necessario leggere la risposta qui
                    getString(socket, addBookRequest);
                } catch (IOException e) {
                    System.err.println("Errore durante l'aggiunta del libro " + randomBookId + " a " + libraryName + ": " + e.getMessage());
                    // Continua con la prossima richiesta
                }
            }
            if (libraryId % 10 == 0) {
                System.out.println("Aggiunti " + BOOKS_PER_LIBRARY + " libri a " + libraryName);
            }
        }

        System.out.println("--- FINE POPOLAMENTO DATI ---");
    }
}
