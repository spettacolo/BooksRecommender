package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

/**
 * LibraryView estende StackPane per permettere all'overlay dei dettagli
 * di apparire SOPRA la lista dei libri.
 */
public class LibraryView extends StackPane {

    private StackPane overlayContainer;

    public LibraryView(RootView root, String lib) {
        Client client = root.getClient();

        SearchHandler searchHandler = new SearchHandler(client);
        HBox searchBar = searchHandler.createSearchBar(root);

        // 1. CONTENITORE PRINCIPALE (Sotto l'overlay)
        VBox mainContent = new VBox();
        mainContent.setFillWidth(true);
        VBox.setVgrow(mainContent, javafx.scene.layout.Priority.ALWAYS);
        mainContent.setPadding(new Insets(0, 20, 0, 20)); // Margini laterali

        // Richiesta dati libreria
        String risposta = client.send("get_user_library;id;" + lib);
        System.out.println(risposta);

        // Header (titolo + searchBar)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 40, 20, 40));

        Label nomeLibreria = new Label((risposta.trim()).split(";")[1]);
        nomeLibreria.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nomeLibreria, spacer, searchBar);

        // Griglia per le copertine
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setPadding(new Insets(10, 0, 20, 0));
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setMaxWidth(Double.MAX_VALUE);

        String[] libri = risposta.trim().split(";")[3].split(",");

        int col = 0;
        int row = 0;

        for (String s : libri) {
            if (s == null || s.isBlank()) continue;

            String bookInfo = client.send("get_book;id;" + s).trim();
            String[] parts = bookInfo.split(";");
            if (parts.length < 7) continue;

            String coverUrl = parts[6];
            int bookId = Integer.parseInt(parts[0]);

            // Creazione del nodo copertina
            StackPane coverNode = client.createScaledCover(coverUrl, 120, 200);
            coverNode.setStyle("-fx-cursor: hand;"); // Cambia cursore al passaggio

            // Gestione click sulla copertina
            coverNode.setOnMouseClicked(e -> {
                BookDetails details = new BookDetails(bookId, RootView.getUsername());
                StackPane overlay = details.createOverlay();

                // Chiude l'overlay se si clicca fuori o sul tasto chiudi (se implementato in BookDetails)
                overlay.setOnMouseClicked(ev -> {
                    if (ev.getTarget() == overlay) { // Chiude solo se clicchi sullo sfondo dell'overlay
                        overlayContainer.getChildren().remove(overlay);
                    }
                });

                Platform.runLater(() -> {
                    overlayContainer.getChildren().clear(); // Pulisce eventuali overlay precedenti
                    overlayContainer.getChildren().add(overlay);
                });
            });

            grid.add(coverNode, col, row);

            col++;
            if (col > 3) { // 4 colonne
                col = 0;
                row++;
            }
        }

        // ScrollPane per la griglia
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color:transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        // Aggiungo gli elementi al contenuto principale
        mainContent.getChildren().addAll(header, scrollPane);

        // 2. CONTENITORE OVERLAY (Sopra il contenuto)
        overlayContainer = new StackPane();
        overlayContainer.setPickOnBounds(false); // IMPORTANTE: permette di cliccare i libri quando l'overlay è vuoto

        // Aggiunta dei due strati alla LibraryView
        this.getChildren().addAll(mainContent, overlayContainer);
        StackPane.setAlignment(mainContent, Pos.TOP_LEFT);
        StackPane.setAlignment(overlayContainer, Pos.CENTER);

        // Impostazioni di dimensione per LibraryView
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }
}