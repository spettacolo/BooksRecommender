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

public class HomeView extends VBox {

    private VBox mainContent;
    private SearchHandler searchHandler;
    private Client client;
    private HBox searchBar;
    private StackPane overlayContainer;

    /**
     * Costruttore della vista Home.
     * Inizializza il layout principale, configura il gestore delle ricerche e organizza
     * la gerarchia visiva includendo la barra di ricerca nell'header e il contenitore
     * per gli overlay dei dettagli libro.
     *
     * @param root Il riferimento alla {@link RootView} per accedere al client e alla gestione dei contenuti.
     */
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

        // Preparazione della barra di ricerca e piazzamento nell'angolo a destra dell'header
        searchBar = searchHandler.createSearchBar(root);
        HBox header = (HBox) mainContent.getChildren().get(0);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, searchBar);

        this.getChildren().add(mainStack);
    }

    /**
     * Crea il contenitore principale scorrevole della Home.
     * Genera l'header della pagina e inizializza le diverse sezioni (mensole) divise
     * per genere letterario, inserendole in uno {@link ScrollPane}.
     *
     * @return Uno {@link ScrollPane} contenente l'intera struttura della Home.
     */
    private ScrollPane createMainContent() {
        mainContent = new VBox();
        mainContent.setFillWidth(true);

        HBox header = new HBox();
        header.getStyleClass().add("home-header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label homeLabel = new Label("Home");
        homeLabel.getStyleClass().add("header-title");
        header.getChildren().add(homeLabel);

        mainContent.getChildren().addAll(header);

        mainContent.getChildren().add(createGenreSection("none"));
        mainContent.getChildren().add(createGenreSection("General"));
        mainContent.getChildren().add(createGenreSection("Romance"));
        mainContent.getChildren().add(createGenreSection("Thrillers"));
        mainContent.getChildren().add(createGenreSection("Fiction"));

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(mainContent);
        scrollPane.getStyleClass().add("home-scroll-pane");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            mainContent.setPrefWidth(newVal.getWidth());
        });

        return scrollPane;
    }

    /**
     * Crea una sezione dedicata a un genere specifico o ai libri più popolari.
     * Ogni sezione include un titolo (sottotitolo della categoria) e una riga
     * a scorrimento orizzontale contenente le copertine dei libri.
     *
     * @param genreName Il nome del genere da visualizzare (usa "none" per i più popolari).
     * @return Un {@link VBox} che rappresenta la "mensola" del genere specificato.
     */
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

    /**
     * Genera la riga orizzontale scorrevole dei libri per un determinato genere.
     * Il caricamento dei dati avviene in modo asincrono tramite un {@link Task} per
     * non bloccare il thread dell'interfaccia utente durante la comunicazione con il server.
     * All'interno del task, ogni libro viene trasformato in una card cliccabile che
     * apre l'overlay dei dettagli.
     *
     * @param genreName Il genere di cui recuperare i libri tramite il server.
     * @return Uno {@link ScrollPane} configurato per lo scorrimento orizzontale dei libri.
     */
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
                String risposta = client.send("get_book;top;" + genreName + ";20");
                if (risposta == null || risposta.isEmpty()) return null;

                String[] entries = risposta.split("\\|");
                for (String entry : entries) {
                    String[] parts = entry.split(";");
                    if (parts.length < 7) continue;

                    String coverUrl = parts[6];
                    int bookId = Integer.parseInt(parts[0]);

                    StackPane coverContainer = client.createScaledCover(coverUrl, 150, 280);
                    coverContainer.setAlignment(Pos.BOTTOM_CENTER);

                    coverContainer.setOnMouseClicked(e -> {
                        BookDetails details = new BookDetails(bookId, RootView.getUsername());
                        StackPane overlay = details.createOverlay();
                        Platform.runLater(() -> overlayContainer.getChildren().add(overlay));
                    });

                    Platform.runLater(() -> row.getChildren().add(coverContainer));
                }
                return null;
            }
        };

        new Thread(task).start();
        return scroll;
    }
}