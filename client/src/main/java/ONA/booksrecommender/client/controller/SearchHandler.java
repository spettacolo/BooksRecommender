package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.view.RootView;
import ONA.booksrecommender.client.view.SearchView;
import ONA.booksrecommender.objects.Book;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchHandler {

    private Client client;

    public SearchHandler(Client client) {
        this.client = client;
    }

    public HBox createSearchBar(RootView root) {
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_RIGHT);

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Cerca un libro...");
        searchField.setPrefWidth(250);

        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                syncQueryWithExistingView(root, query);

                List<Book> results = searchBooks(query, "title", 0);
                root.showSearchResults(query, results);
            }
        });

        searchBar.getChildren().add(searchField);
        return searchBar;
    }

    private void syncQueryWithExistingView(RootView root, String query) {
        Object mainContent = root.getMainContentContainer();

        if (mainContent instanceof StackPane) {
            StackPane stack = (StackPane) mainContent;
            for (Node node : stack.getChildren()) {
                if (node instanceof ScrollPane) {
                    ScrollPane scroll = (ScrollPane) node;
                    if (scroll.getContent() instanceof SearchView) {
                        ((SearchView) scroll.getContent()).setCurrentQuery(query);
                        ((SearchView) scroll.getContent()).resetOffset();
                    }
                }
            }
        }
    }

    public List<Book> searchBooks(String query, String type, int offset) {
        List<Book> results = new ArrayList<>();
        if (query == null || query.isBlank()) return results;

        try {
            String command = "get_book;" + type + ";" + query + ";" + offset;

            String risposta = this.client.send(command);

            if (risposta == null || risposta.isBlank() || risposta.startsWith("ERROR") || risposta.equals("NOT_FOUND")) {
                return results;
            }

            String clean = risposta.endsWith("|") ? risposta.substring(0, risposta.length() - 1) : risposta;
            String[] records = clean.split("\\|(?=\\d+;)");

            for (String record : records) {
                String[] parts = record.split(";");
                if (parts.length < 8) continue;

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    int last = parts.length - 1;
                    String encodedDesc = parts[last];
                    String coverUrl    = parts[last - 1];
                    String category    = parts[last - 2];
                    String publisher   = parts[last - 3];
                    int year           = Integer.parseInt(parts[last - 4].trim());

                    String authorStr   = parts[last - 5];
                    List<String> authors = Arrays.asList(authorStr.split(",\\s*"));

                    StringBuilder titleBuilder = new StringBuilder();
                    for (int i = 1; i < (last - 5); i++) {
                        titleBuilder.append(parts[i]);
                        if (i < (last - 6)) titleBuilder.append(";");
                    }
                    String title = titleBuilder.toString();

                    results.add(new Book(id, title, authors, year, publisher, category, coverUrl, encodedDesc));
                } catch (Exception e) {
                    System.err.println("Errore di parsing per il record: " + record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}