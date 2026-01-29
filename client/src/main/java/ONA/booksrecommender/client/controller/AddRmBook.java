package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import java.util.ArrayList;
import java.util.List;

public class AddRmBook {

    /**
     * Recupera l'elenco delle librerie dell'utente che NON contengono un determinato libro.
     * Interroga il server per ottenere tutte le librerie dell'utente e, per ciascuna,
     * verifica se l'ID del libro specificato è presente.
     *
     * @param client   L'istanza del client per la comunicazione con il server.
     * @param username Il nome utente di cui controllare le librerie.
     * @param bookId   L'identificativo numerico del libro da cercare.
     * @return Una lista di oggetti {@link LibraryInfo} rappresentanti le librerie che non includono il libro.
     */
    public static List<LibraryInfo> getLibrariesWithoutBook(Client client, String username, int bookId) {
        String libsResponse = client.send("get_user_libraries;" + username);
        List<LibraryInfo> result = new ArrayList<>();
        if (libsResponse == null || libsResponse.isEmpty() || libsResponse.contains("ERROR")) return result;

        String[] libIds = libsResponse.split(",");

        for (String libId : libIds) {
            String libBooksResponse = client.send("get_user_library;id;" + libId.trim());
            if (libBooksResponse == null || libBooksResponse.isEmpty()) continue;

            String[] parts = libBooksResponse.split(";");
            boolean containsBook = false;

            if (parts.length > 3 && !parts[3].isBlank()) {
                String[] bookIds = parts[3].split(",");
                for (String bId : bookIds) {
                    try {
                        if (!bId.isBlank() && Integer.parseInt(bId.trim()) == bookId) {
                            containsBook = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Ignoriamo pezzi di stringa non numerici
                    }
                }
            }

            if (!containsBook) {
                String libName = parts.length > 1 ? parts[1] : "Libreria " + libId;
                result.add(new LibraryInfo(libId, libName));
            }
        }
        return result;
    }

    /**
     * Recupera l'elenco delle librerie dell'utente che contengono già un determinato libro.
     * Interroga il server per ottenere i dettagli di ogni libreria dell'utente e filtra
     * quelle in cui l'ID del libro specificato è incluso nella lista dei volumi.
     *
     * @param client   L'istanza del client per la comunicazione con il server.
     * @param username Il nome utente di cui controllare le librerie.
     * @param bookId   L'identificativo numerico del libro da cercare.
     * @return Una lista di oggetti {@link LibraryInfo} rappresentanti le librerie che includono il libro.
     */
    public static List<LibraryInfo> getLibrariesWithBook(Client client, String username, int bookId) {
        String libsResponse = client.send("get_user_libraries;" + username);
        List<LibraryInfo> result = new ArrayList<>();
        if (libsResponse == null || libsResponse.isEmpty() || libsResponse.contains("ERROR")) return result;

        String[] libIds = libsResponse.split(",");

        for (String libId : libIds) {
            String libBooksResponse = client.send("get_user_library;id;" + libId.trim());
            if (libBooksResponse == null || libBooksResponse.isEmpty()) continue;

            String[] parts = libBooksResponse.split(";");
            boolean containsBook = false;

            if (parts.length > 3 && !parts[3].isBlank()) {
                String[] bookIds = parts[3].split(",");
                for (String bId : bookIds) {
                    try {
                        if (!bId.isBlank() && Integer.parseInt(bId.trim()) == bookId) {
                            containsBook = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Ignoriamo ID non validi
                    }
                }
            }

            if (containsBook) {
                String libName = parts.length > 1 ? parts[1] : "Libreria " + libId;
                result.add(new LibraryInfo(libId, libName));
            }
        }
        return result;
    }

    /**
     * Classe interna di supporto per memorizzare informazioni sintetiche su una libreria.
     * Utilizzata per mappare l'ID e il nome della libreria durante le operazioni di aggiunta/rimozione.
     */
    public static class LibraryInfo {
        private final String id;
        private final String name;

        /**
         * Crea una nuova istanza di LibraryInfo.
         *
         * @param id   L'identificativo univoco della libreria.
         * @param name Il nome visualizzabile della libreria.
         */
        public LibraryInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        /**
         * Restituisce l'ID della libreria.
         *
         * @return L'identificativo della libreria come stringa.
         */
        public String getId() { return id; }

        /**
         * Restituisce il nome della libreria.
         *
         * @return Il nome della libreria.
         */
        public String getName() { return name; }

        /**
         * Restituisce una rappresentazione testuale della libreria nel formato "Nome (ID)".
         *
         * @return Stringa formattata con nome e ID.
         */
        @Override
        public String toString() { return name + " (" + id + ")"; }
    }
}