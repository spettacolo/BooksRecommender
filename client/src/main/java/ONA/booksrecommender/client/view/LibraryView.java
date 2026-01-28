package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;

public class LibraryView extends StackPane {

    private StackPane overlayContainer;

    /**
     * Costruttore della vista Libreria.
     * Inizializza il layout per visualizzare i libri contenuti in una specifica libreria dell'utente.
     * Configura la barra di ricerca, recupera i dati della libreria dal server e popola
     * una griglia dinamica (FlowPane) con le copertine dei libri.
     *
     * @param root L'istanza di {@link RootView} per l'accesso ai servizi globali e al client.
     * @param lib  L'ID della libreria da caricare e visualizzare.
     */
    public LibraryView(RootView root, String lib) {
        Client client = root.getClient();

        SearchHandler searchHandler = new SearchHandler(client);
        HBox searchBar = searchHandler.createSearchBar(root);

        // 1. CONTENITORE PRINCIPALE
        VBox mainContent = new VBox();
        mainContent.setFillWidth(true);
        VBox.setVgrow(mainContent, javafx.scene.layout.Priority.ALWAYS);
        mainContent.setPadding(new Insets(0, 20, 0, 20));

        String risposta = client.send("get_user_library;id;" + lib);
        System.out.println(risposta);

        // Header (titolo + searchBar)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 40, 20, 40));

        Label nomeLibreria = new Label((risposta.trim()).split(";")[1]);
        nomeLibreria.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nomeLibreria, spacer, searchBar);

        FlowPane flowGrid = new FlowPane();
        flowGrid.setHgap(25);
        flowGrid.setVgap(30);
        flowGrid.setPadding(new Insets(20, 40, 40, 40));
        flowGrid.setAlignment(Pos.TOP_LEFT);

        flowGrid.setMaxWidth(Double.MAX_VALUE);
        flowGrid.setPrefWrapLength(Region.USE_COMPUTED_SIZE);

        String[] partsResp = risposta != null ? risposta.trim().split(";") : new String[0];
        String[] libri = partsResp.length > 3 && !partsResp[3].isBlank() ? partsResp[3].split(",") : new String[0];
        boolean hasAnyBook = false;

        // Creazione delle card cliccabili
        for (String s : libri) {
            if (s == null || s.isBlank()) continue;

            String bookInfo = client.send("get_book;id;" + s).trim();
            String[] parts = bookInfo.split(";");
            if (parts.length < 7) continue;

            String coverUrl = parts[6];
            int bookId = Integer.parseInt(parts[0]);

            StackPane coverNode = client.createScaledCover(coverUrl, 130, 200);
            coverNode.setStyle("-fx-cursor: hand;");

            coverNode.setOnMouseClicked(e -> {
                BookDetails details = new BookDetails(bookId, RootView.getUsername());
                StackPane overlay = details.createOverlay();

                overlay.setOnMouseClicked(ev -> {
                    if (ev.getTarget() == overlay) {
                        overlayContainer.getChildren().remove(overlay);
                    }
                });

                Platform.runLater(() -> {
                    overlayContainer.getChildren().clear();
                    overlayContainer.getChildren().add(overlay);
                });
            });

            flowGrid.getChildren().add(coverNode);
            hasAnyBook = true;
        }

        if (!hasAnyBook) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(100, 0, 0, 0));
            emptyBox.setPrefWidth(1200); // Valore indicativo per centrare il testo

            Label emptyLabel = new Label("Ancora nulla da leggere qui...");
            emptyLabel.getStyleClass().add("library-empty-label");
            emptyBox.getChildren().add(emptyLabel);
            flowGrid.getChildren().add(emptyBox);
        }

        ScrollPane scrollPane = new ScrollPane(flowGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color:transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContent.getChildren().addAll(header, scrollPane);

        // Livello superiore per gli overlay (dettagli libro, popup, ecc.)
        overlayContainer = new StackPane();
        overlayContainer.setPickOnBounds(false);

        // Aggiunta dei due strati alla LibraryView
        this.getChildren().addAll(mainContent, overlayContainer);
        StackPane.setAlignment(mainContent, Pos.TOP_LEFT);
        StackPane.setAlignment(overlayContainer, Pos.CENTER);

        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }
}