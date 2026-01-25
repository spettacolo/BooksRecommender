package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

// Vista principale della Home: Header, sezioni di generi e overlay per BookDetails
public class HomeView extends VBox {

    // Componenti principali della Home
    private VBox mainContent;
    private SearchHandler searchHandler;
    private Client client;
    private HBox searchBar;
    private StackPane overlayContainer;

    // Assemblaggio della Home, collegamento ricerca e overlay dettagli
    public HomeView(RootView root) {
        this.client = root.getClient();
        this.setFillWidth(true);

        searchHandler = new SearchHandler(client);
        ScrollPane mainContentPane = createMainContent();

        // Overlay per mostrare i dettagli dei libri sopra la Home senza cambiare pagina
        overlayContainer = new StackPane();
        overlayContainer.setPickOnBounds(false);

        // StackPane per tenere i contenuti e i popup nello stesso livello visivo
        StackPane mainStack = new StackPane(mainContentPane, overlayContainer);

        // Prepariamo la barra di ricerca e la piazziamo nell'angolo a destra dell'header
        searchBar = searchHandler.createSearchBar(root);
        HBox header = (HBox) mainContent.getChildren().get(0);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, searchBar);

        this.getChildren().add(mainStack);
    }

    // Costruisce header e sezioni generi dentro uno ScrollPane
    private ScrollPane createMainContent() {
        mainContent = new VBox();
        mainContent.setFillWidth(true);

        // Header della pagina con la scritta "Home"
        HBox header = new HBox();
        header.getStyleClass().add("home-header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label homeLabel = new Label("Home");
        homeLabel.getStyleClass().add("header-title");
        header.getChildren().add(homeLabel);

        mainContent.getChildren().addAll(header);

        // Definiamo i generi da mostrare. "none" recupera i titoli del momento
        mainContent.getChildren().add(createGenreSection("none"));
        mainContent.getChildren().add(createGenreSection("General"));
        mainContent.getChildren().add(createGenreSection("Romance"));
        mainContent.getChildren().add(createGenreSection("Thrillers"));
        mainContent.getChildren().add(createGenreSection("Fiction"));

        // Setup dello ScrollPane per permettere la navigazione verticale
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(mainContent);
        scrollPane.getStyleClass().add("home-scroll-pane");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Listener per adattare la larghezza del contenuto quando si ridimensiona la finestra
        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            mainContent.setPrefWidth(newVal.getWidth());
        });

        return scrollPane;
    }

    // Singola mensola orizzontale (titolo + riga di libri)
    private VBox createGenreSection(String genreName) {
        VBox section = new VBox();
        section.getStyleClass().add("genre-section");
        section.setSpacing(30);
        section.setFillWidth(true);

        Label subtitle;
        if ("none".equals(genreName)) {
            subtitle = new Label("I più popolari >");
        } else {
            subtitle = new Label(genreName + " >");
        }
        subtitle.getStyleClass().add("genre-subtitle");
        subtitle.setPadding(new Insets(0, 0, 0, 10));

        ScrollPane booksScroll = createGenreBooksScroll(genreName);

        section.getChildren().addAll(subtitle, booksScroll);
        return section;
    }

    // Carica libri in background e crea copertine cliccabili
    private ScrollPane createGenreBooksScroll(String genreName) {
        HBox row = new HBox(30);
        row.setAlignment(Pos.BOTTOM_CENTER);
        row.getStyleClass().add("books-row");
        row.setPadding(new Insets(0, 15, 0, 15));

        VBox wrapper = new VBox(row);
        wrapper.setFillWidth(true);

        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("books-scroll");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setContent(wrapper);

        // Task eseguito fuori dal thread FX per non bloccare l'interfaccia
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // Chiediamo i primi 20 libri del genere selezionato
                String risposta = client.send("get_book;top;" + genreName + ";20");
                if (risposta == null || risposta.isEmpty()) return null;

                String[] entries = risposta.split("\\|");
                for (String entry : entries) {
                    String[] parts = entry.split(";");
                    if (parts.length < 7) continue;

                    String coverUrl = parts[6];
                    int bookId = Integer.parseInt(parts[0]);

                    // Creiamo visivamente la copertina
                    StackPane coverContainer = client.createScaledCover(coverUrl, 150, 280);
                    coverContainer.setAlignment(Pos.BOTTOM_CENTER);

                    // Al click apre overlay con dettagli libro
                    coverContainer.setOnMouseClicked(e -> {
                        BookDetails details = new BookDetails(bookId, RootView.getUsername());
                        StackPane overlay = details.createOverlay();
                        Platform.runLater(() -> overlayContainer.getChildren().add(overlay));
                    });

                    // Aggiorniamo la UI solo tramite Platform.runLater (altrimenti JavaFX crasha)
                    Platform.runLater(() -> row.getChildren().add(coverContainer));
                }
                return null;
            }
        };

        new Thread(task).start();
        return scroll;
    }
}