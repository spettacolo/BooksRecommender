package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import java.util.ArrayList;
import java.util.List;

public class AddRmBook {

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

            // FIX: La lista dei libri inizia dall'indice 3.
            // parts[0]=ID, parts[1]=Nome, parts[2]=Username, parts[3]=LibriID
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

            // FIX: Iniziamo il controllo dall'indice 3
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

    public static class LibraryInfo {
        private final String id;
        private final String name;

        public LibraryInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name + " (" + id + ")"; }
    }
}