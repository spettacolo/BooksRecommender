package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.objects.Book;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gestisce la barra di ricerca e le operazioni di ricerca dei libri.
 */
public class SearchHandler {

    private Socket socket;
    public SearchHandler() {
        String host = "localhost"; // oppure l'IP del server, tipo "192.168.1.100"
        int porta = 1234;

        try {
            socket = new Socket(host, porta);
            System.out.println("Connesso al server su " + host + ":" + porta);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restituisce la barra di ricerca completa, pronta per essere aggiunta in HomeView.
     *
     * @param mainContent VBox principale dove inserire i risultati.
     */
    public HBox createSearchBar(VBox mainContent) {
        HBox searchBar = new HBox(10);
        searchBar.setPadding(new Insets(10, 40, 10, 40));
        searchBar.setVisible(false);
        searchBar.setManaged(false);

        TextField searchField = new TextField();
        searchField.setPromptText("Cerca un libro...");

        Button searchButton = new Button("Invia");

        // Azione del pulsante
        searchButton.setOnAction(event -> performSearch(mainContent, searchField.getText().trim()));

        // Permette la ricerca anche con Invio
        searchField.setOnAction(event -> searchButton.fire());

        searchBar.getChildren().addAll(searchField, searchButton);
        return searchBar;
    }

    /**
     * Esegue la ricerca e aggiorna il mainContent con i risultati.
     */
    private void performSearch(VBox mainContent, String query) {
        mainContent.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("resultsBox"));

        VBox resultsBox = searchBooks(query);
        resultsBox.setId("resultsBox");

        // Inserisce subito dopo la barra di ricerca (se presente)
        int indexAfterSearchBar = 1;
        mainContent.getChildren().add(indexAfterSearchBar, resultsBox);
    }

    /**
     * Esegue la ricerca dei libri in base al titolo e restituisce una VBox con i risultati.
     */
    private VBox searchBooks(String query) {
        VBox resultsBox = new VBox(10);
        resultsBox.setPadding(new Insets(20, 40, 20, 40));

        if (query == null || query.trim().isEmpty()) {
            Label empty = new Label("Inserisci un termine di ricerca.");
            resultsBox.getChildren().add(empty);
            return resultsBox;
        }

        List<Book> results = new ArrayList<>();

        try {
            String risposta = getString(socket, "get_book;title;" + query);

            // Gestione errori o risposta vuota
            if (risposta == null || risposta.isBlank() || risposta.equalsIgnoreCase("ERROR")) {
                Label errorLabel = new Label("Errore nella comunicazione con il server o nessun risultato trovato.");
                resultsBox.getChildren().add(errorLabel);
                return resultsBox;
            }

            // Ogni libro è separato da "|"
            for (String line : risposta.split("\\|")) {
                String[] parts = line.split(";");

                // Controllo minimo di validità
                if (parts.length < 7) continue;

                List<String> authors = Arrays.asList(parts[2].split(",")); // esempio: autori separati da virgola
                results.add(new Book(
                        Integer.parseInt(parts[0]),  // id
                        parts[1],                    // titolo
                        authors,                     // autori
                        Integer.parseInt(parts[3]),  // anno
                        parts[4],                    // editore
                        parts[5],                    // mese pubblicazione
                        parts[6]                     // genere o altro
                ));
            }
        } catch (IOException e) {
            Label errorLabel = new Label("Errore di connessione al server.");
            resultsBox.getChildren().add(errorLabel);
            return resultsBox;
        } catch (NumberFormatException e) {
            Label parseError = new Label("Formato dati non valido ricevuto dal server.");
            resultsBox.getChildren().add(parseError);
            return resultsBox;
        }

        /*try {
            String risposta = getString(socket, "get_book;title;"+query);
            for (String line : risposta.split("\\|")) {
                String[] parts = risposta.split(";");
                List<String> authors = Arrays.asList(parts);
                results.add(new Book(Integer.parseInt(parts[0]),parts[1],authors,Integer.parseInt(parts[3]),parts[4],parts[5],parts[6]));
            }
        }
        catch (IOException e) {
            results = null;
        }*/

        // List<Book> results = bookManager.searchByTitle(query.trim());
        System.out.println("🔍 Ricerca: \"" + query + "\" → risultati trovati: " + results.size());

        if (!results.isEmpty()) {
            for (Book b : results) {
                Label resultLabel = new Label("📘 " + b.getTitle() + " — " + String.join(", ", b.getAuthors()));
                resultsBox.getChildren().add(resultLabel);
            }
        } else {
            Label noResult = new Label("Nessun libro trovato per \"" + query + "\"");
            resultsBox.getChildren().add(noResult);
        }

        return resultsBox;


    }

    private static String getString(Socket socket, String richiesta) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(richiesta);
        return in.readLine();
    }
}

