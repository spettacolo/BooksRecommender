package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.controller.SearchHandler;
import ONA.booksrecommender.objects.Book;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class SearchView extends VBox {

    private VBox mainContent;
    private VBox resultsBox;
    private StackPane overlayContainer;
    private List<Book> currentResults;
    private RootView root;
    private String currentQuery;
    private SearchHandler searchHandler;

    private int currentOffset = 0;
    private final int LIMIT = 20;
    private boolean isLoading = false;
    private String currentTab = "title";

    /**
     * Costruttore della vista di ricerca.
     * Inizializza l'interfaccia per mostrare i risultati, configura l'header con la barra di ricerca,
     * imposta i tab per il filtraggio (Titolo/Autore) e predispone lo scroll infinito
     * per il caricamento dinamico di ulteriori risultati.
     *
     * @param root    Il riferimento alla {@link RootView} per la gestione globale e l'accesso al client.
     * @param query   La stringa di ricerca iniziale.
     * @param results La lista iniziale di oggetti {@link Book} restituiti dal server.
     */
    public SearchView(RootView root, String query, List<Book> results) {
        this.root = root;
        this.currentQuery = query;
        this.currentResults = (results != null) ? new ArrayList<>(results) : new ArrayList<>();
        this.searchHandler = new SearchHandler(root.getClient());

        this.getStyleClass().add("search-view-main");

        overlayContainer = new StackPane();
        overlayContainer.setPickOnBounds(false);

        mainContent = new VBox(0);
        mainContent.getStyleClass().add("main-content-container");

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("home-header");

        Label titleLabel = new Label((query == null || query.isBlank()) ? "Risultati ricerca" : "Risultati per: " + query);
        titleLabel.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox searchBar = searchHandler.createSearchBar(root);

        header.getChildren().addAll(titleLabel, spacer, searchBar);
        mainContent.getChildren().add(header);

        // --- TABS ---
        HBox tabsBox = new HBox(30);
        tabsBox.setPadding(new Insets(0, 40, 15, 40));

        Label tabTitolo = new Label("Titolo");
        Label tabAutore = new Label("Autore");

        tabTitolo.getStyleClass().addAll("tab-label", "tab-label-active");
        tabAutore.getStyleClass().add("tab-label");

        tabTitolo.setOnMouseClicked(e -> {
            if(currentTab.equals("title")) return;
            currentTab = "title";
            resetOffset();
            tabTitolo.getStyleClass().add("tab-label-active");
            tabAutore.getStyleClass().remove("tab-label-active");
            updateResults();
        });

        tabAutore.setOnMouseClicked(e -> {
            if(currentTab.equals("author")) return;
            currentTab = "author";
            resetOffset();
            tabAutore.getStyleClass().add("tab-label-active");
            tabTitolo.getStyleClass().remove("tab-label-active");
            updateResults();
        });

        tabsBox.getChildren().addAll(tabTitolo, tabAutore);
        mainContent.getChildren().add(tabsBox);

        // --- RESULTS ---
        resultsBox = new VBox();
        resultsBox.getStyleClass().add("results-container");
        VBox.setVgrow(resultsBox, Priority.ALWAYS);
        mainContent.getChildren().add(resultsBox);

        renderView();

        // --- SCROLL E LISTENER ---
        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.getStyleClass().add("search-scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        scroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.95 && !isLoading && currentResults.size() >= LIMIT) {
                loadMoreResults();
            }
        });

        StackPane mainStack = new StackPane(scroll, overlayContainer);
        VBox.setVgrow(mainStack, Priority.ALWAYS);
        this.getChildren().add(mainStack);
    }

    /**
     * Aggiorna i risultati visualizzati in base alla query corrente e al tab selezionato.
     * Viene invocato quando l'utente cambia il criterio di ricerca (es. da Titolo ad Autore).
     */
    private void updateResults() {
        this.currentResults = searchHandler.searchBooks(currentQuery, currentTab, 0);
        renderView();
    }

    /**
     * Carica in modo asincrono un nuovo set di risultati dal server.
     * Implementa la logica di paginazione: incrementa l'offset, richiede i libri successivi
     * in un thread separato e aggiorna l'interfaccia grafica sul thread {@link Platform#runLater}.
     */
    private void loadMoreResults() {
        isLoading = true;
        currentOffset += LIMIT;
        new Thread(() -> {
            List<Book> nextBooks = searchHandler.searchBooks(currentQuery, currentTab, currentOffset);
            if (nextBooks != null && !nextBooks.isEmpty()) {
                Platform.runLater(() -> {
                    this.currentResults.addAll(nextBooks);
                    renderView();
                    isLoading = false;
                });
            } else {
                isLoading = false;
            }
        }).start();
    }

    /**
     * Gestisce il rendering grafico della lista dei risultati nel contenitore principale.
     * Se la lista è vuota mostra un messaggio informativo, altrimenti popola il box
     * seguendo lo stile del tab attivo (lista semplice per titolo o raggruppata per autore).
     */
    private void renderView() {
        resultsBox.getChildren().clear();
        if (currentResults.isEmpty()) {
            Label empty = new Label("Nessun risultato trovato.");
            empty.getStyleClass().add("subtitle");
            resultsBox.getChildren().add(empty);
            return;
        }

        if (currentTab.equals("title")) {
            for (Book b : currentResults) resultsBox.getChildren().add(createBookRow(b));
        } else {
            renderAutoreView();
        }
    }

    /**
     * Esegue il rendering specifico per la ricerca per autore.
     * Ordina i libri per anno di pubblicazione (dal più recente) e inserisce
     * degli header visivi per separare i volumi prodotti in anni differenti.
     */
    private void renderAutoreView() {
        currentResults.sort((b1, b2) -> Integer.compare(b2.getPublicationYear(), b1.getPublicationYear()));
        int lastYear = -1;
        for (Book b : currentResults) {
            if (b.getPublicationYear() != lastYear) {
                lastYear = b.getPublicationYear();
                VBox yearHeader = new VBox(5);
                yearHeader.setPadding(new Insets(20, 0, 10, 0));
                Label l = new Label(String.valueOf(lastYear));
                l.getStyleClass().add("year-header-label");
                Region line = new Region();
                line.getStyleClass().add("year-separator-line");
                yearHeader.getChildren().addAll(l, line);
                resultsBox.getChildren().add(yearHeader);
            }
            resultsBox.getChildren().add(createBookRow(b));
        }
    }

    /**
     * Crea una riga grafica rappresentante un singolo libro.
     * Include la miniatura della copertina, il titolo e gli autori. Configura inoltre
     * l'evento di click per aprire l'overlay dei dettagli tramite {@link BookDetails}.
     *
     * @param b L'oggetto {@link Book} da visualizzare.
     * @return Un {@link HBox} formattato come riga della lista risultati.
     */
    private HBox createBookRow(Book b) {
        HBox row = new HBox(20);
        row.getStyleClass().add("book-row");
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView cover = new ImageView();
        cover.setFitWidth(60);
        cover.setFitHeight(90);
        cover.setPreserveRatio(true);
        cover.getStyleClass().add("book-cover");

        if (b.getCoverImageUrl() != null && !"null".equals(b.getCoverImageUrl())) {
            try { cover.setImage(new Image(b.getCoverImageUrl(), true)); } catch (Exception e) {}
        }

        VBox info = new VBox(5);
        Label t = new Label(b.getTitle());
        t.getStyleClass().add("book-title");
        t.setWrapText(true);

        Label a = new Label(String.join(", ", b.getAuthors()));
        a.getStyleClass().add("book-author");

        info.getChildren().addAll(t, a);
        row.getChildren().addAll(cover, info);

        row.setOnMouseClicked(e -> {
            BookDetails details = new BookDetails(b.getId(), root.getUsername());
            overlayContainer.getChildren().add(details.createOverlay());
        });
        return row;
    }

    /**
     * Ripristina l'offset della paginazione a zero.
     * Utile quando si avvia una nuova ricerca o si cambia tab.
     */
    public void resetOffset() { this.currentOffset = 0; }

    /**
     * Aggiorna la query di ricerca corrente e resetta lo stato della paginazione.
     *
     * @param query La nuova stringa di ricerca.
     */
    public void setCurrentQuery(String query) { this.currentQuery = query; resetOffset(); }
}