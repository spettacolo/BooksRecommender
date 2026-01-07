package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.view.RootView;
import ONA.booksrecommender.objects.Book;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gestisce la barra di ricerca e le operazioni di ricerca dei libri.
 */
public class SearchHandler {

    private Client client;
    public SearchHandler(Client client) {
        this.client = client;
    }

    /**
     * Restituisce la barra di ricerca completa, pronta per essere aggiunta in HomeView.
     *
     * @param root RootView principale dove mostrare i risultati.
     */
    public HBox createSearchBar(RootView root) {
        HBox searchBar = new HBox(10);
        searchBar.setPadding(new Insets(10, 20, 10, 20));
        searchBar.setAlignment(Pos.CENTER_RIGHT);

        Button filtersButton = new Button("️⚙");

        TextField searchField = new TextField();
        searchField.setPromptText("Cerca un libro...");
        double initialWidth = 100;
        double expandedWidth = 300;
        searchField.setPrefWidth(initialWidth);
        searchField.setMaxWidth(initialWidth);

        HBox.setHgrow(filtersButton, Priority.NEVER);
        HBox.setHgrow(searchField, Priority.NEVER);

        StackPane wrapper = new StackPane(searchBar);
        wrapper.setAlignment(Pos.CENTER_RIGHT);

        Duration duration = Duration.millis(300);

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Focus gained: expand searchField width and center wrapper
                Timeline expand = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(searchField.prefWidthProperty(), searchField.getPrefWidth()),
                        new KeyValue(searchField.maxWidthProperty(), searchField.getMaxWidth())
                    ),
                    new KeyFrame(duration,
                        new KeyValue(searchField.prefWidthProperty(), expandedWidth),
                        new KeyValue(searchField.maxWidthProperty(), expandedWidth)
                    )
                );
                expand.play();
                wrapper.setAlignment(Pos.CENTER);
            } else {
                // Focus lost: shrink searchField and move wrapper back to right
                Timeline shrink = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(searchField.prefWidthProperty(), searchField.getPrefWidth()),
                        new KeyValue(searchField.maxWidthProperty(), searchField.getMaxWidth())
                    ),
                    new KeyFrame(duration,
                        new KeyValue(searchField.prefWidthProperty(), initialWidth),
                        new KeyValue(searchField.maxWidthProperty(), initialWidth)
                    )
                );
                shrink.play();
                wrapper.setAlignment(Pos.CENTER_RIGHT);
            }
        });

        Runnable doSearch = () -> {
            String query = searchField.getText().trim();
            searchField.clear();
            List<Book> results = searchBooks(query);
            root.showSearchResults(query, results);
        };

        searchField.setOnAction(e -> doSearch.run());

        searchBar.getChildren().addAll(searchField, filtersButton);
        return searchBar;
    }

    /**
     * Esegue la ricerca dei libri in base al titolo e restituisce la lista dei risultati.
     */
    private List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        try {
            String risposta = this.client.send("get_book;title;" + query);

            if (risposta == null || risposta.isBlank() || risposta.equalsIgnoreCase("ERROR")) {
                return results;
            }

            // Pulizia finale (il server termina sempre con "|")
            String clean = risposta;
            if (clean.endsWith("|")) {
                clean = clean.substring(0, clean.length() - 1);
            }

            // Split solo sui separatori reali tra record (| seguito da un id numerico)
            String[] records = clean.split("\\|(?=\\d+;)");

            for (String record : records) {
                // Limite fondamentale: l'ultimo campo (descrizione) può contenere ';'
                String[] parts = record.split(";", 8);

                if (parts.length < 8) continue;

                List<String> authors = Arrays.asList(parts[2].split(","));
                results.add(new Book(
                        Integer.parseInt(parts[0]),  // id
                        parts[1],                    // titolo
                        authors,                     // autori
                        Integer.parseInt(parts[3]),  // anno
                        parts[4],                    // editore
                        parts[5],                    // categoria
                        parts[6],                    // coverUrl
                        parts[7]                     // descrizione
                ));
            }
        } catch (Exception e) {
            // In caso di errore ritorna lista vuota
        }

        return results;
    }
}