package ONA.booksrecommender.client.view;

import java.util.List;

import ONA.booksrecommender.client.controller.*;
import ONA.booksrecommender.objects.Book;
import javafx.scene.layout.*;
import ONA.booksrecommender.client.Client;
import javafx.scene.control.ScrollPane;

public class RootView extends HBox {

    private VBox sidebarContainer = new VBox();
    private StackPane mainContentContainer = new StackPane();
    private Client client = new Client();
    private SearchHandler searchHandler = new SearchHandler(client);
    private HBox searchBar;
    private static String username = "";
    private ScrollPane mainScrollPane;

    /**
     * Costruttore della vista radice dell'applicazione.
     * Inizializza la struttura a due colonne: una sidebar fissa a sinistra e un contenitore
     * dinamico per il contenuto principale a destra. Configura inoltre lo {@link ScrollPane}
     * globale e imposta lo stato iniziale visualizzando la sidebar per utenti non loggati e la Home.
     */
    public RootView() {

        this.setSpacing(0);
        this.setStyle("-fx-background-color: rgb(44,44,46); -fx-padding: 0; -fx-border-width: 0;");

        // Sidebar
        sidebarContainer.setPrefWidth(200);
        sidebarContainer.setMinWidth(200);
        sidebarContainer.setMaxWidth(200);
        sidebarContainer.setStyle("-fx-padding: 30 10 10 10;");
        sidebarContainer.getStyleClass().add("sidebar");

        // MAIN CONTENT
        mainContentContainer.setMinWidth(0);
        mainContentContainer.setPrefWidth(Double.MAX_VALUE);
        mainContentContainer.prefWidthProperty().bind(this.widthProperty().subtract(sidebarContainer.widthProperty()));
        mainContentContainer.getStyleClass().add("main-content-container");
        mainContentContainer.setFocusTraversable(false);
        mainContentContainer.setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-border-color: transparent; -fx-background-color: transparent;");

        mainScrollPane = new ScrollPane(mainContentContainer);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            javafx.scene.Node viewport = mainScrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });
        mainScrollPane.getStyleClass().add("main-scroll-pane");
        mainScrollPane.setFocusTraversable(false);
        mainScrollPane.setPannable(false);
        mainScrollPane.getStyleClass().add("main-scroll-pane");
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);

        HBox.setHgrow(mainScrollPane, Priority.ALWAYS);
        VBox.setVgrow(mainScrollPane, Priority.ALWAYS);

        searchBar = searchHandler.createSearchBar(this);

        VBox rightContainer = new VBox();
        rightContainer.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0; -fx-border-color: transparent;");
        rightContainer.setFillWidth(true);

        rightContainer.getChildren().setAll(mainScrollPane);
        VBox.setVgrow(mainScrollPane, Priority.ALWAYS);

        HBox.setHgrow(rightContainer, Priority.ALWAYS);

        this.getChildren().setAll(sidebarContainer, rightContainer);

        showUnloggedSidebar();
        showHome();
    }

    /**
     * Restituisce l'istanza del client utilizzata per le comunicazioni con il server.
     *
     * @return L'oggetto {@link Client} attivo.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Visualizza nella sidebar i controlli per gli utenti non autenticati (es. tasto Accedi/Registrati).
     */
    public void showUnloggedSidebar() {
        sidebarContainer.getChildren().setAll(new UnLoggedView(this));
    }

    /**
     * Visualizza nella sidebar i controlli dedicati all'utente autenticato,
     * come l'elenco delle librerie personali e il profilo.
     *
     * @param username Lo username dell'utente loggato.
     */
    public void showLoggedSidebar(String username) {
        sidebarContainer.getChildren().setAll(new LoggedView(this, username));
    }

    /**
     * Carica e visualizza la {@link HomeView} nel contenitore principale dell'applicazione.
     */
    public void showHome() {
        HomeView view = new HomeView(this);
        view.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);

        mainContentContainer.getChildren().setAll(view);
    }

    /**
     * Carica e visualizza l'area profilo dell'utente nel contenitore principale.
     *
     * @param username Lo username dell'utente di cui mostrare l'attività.
     */
    public void showUserArea(String username) {
        UserAreaView view = new UserAreaView(this, username);
        mainContentContainer.getChildren().setAll(view);
    }

    /**
     * Visualizza i risultati di una ricerca di libri nel contenitore principale.
     *
     * @param query   La stringa cercata dall'utente.
     * @param results La lista di oggetti {@link Book} trovati dal server.
     */
    public void showSearchResults(String query, List<Book> results) {
        SearchView view = new SearchView(this, query, results);
        view.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);

        mainContentContainer.getChildren().setAll(view);
    }

    /**
     * Carica e visualizza il contenuto di una specifica libreria nel contenitore principale.
     *
     * @param lib L'identificativo (ID) della libreria da visualizzare.
     */
    public void showLibrary(String lib) {
        LibraryView view = new LibraryView(this, lib);

        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);
        view.setMinHeight(Region.USE_COMPUTED_SIZE);

        mainContentContainer.getChildren().setAll(view);
    }

    /**
     * Restituisce lo username dell'utente attualmente loggato nella sessione.
     *
     * @return Lo username come stringa, o una stringa vuota se non loggato.
     */
    public static String getUsername() {
        System.out.println(username);
        return username;
    }

    /**
     * Imposta lo username dell'utente corrente per la sessione globale.
     *
     * @param username Il nome utente da memorizzare.
     */
    public void setUsername(String username) {
        RootView.username = username;
        System.out.println("set username: " + username);
    }

    /**
     * Restituisce il contenitore {@link StackPane} principale dove vengono iniettate le diverse viste.
     * Utilizzato dai controller per aggiungere overlay o popup sopra il contenuto corrente.
     *
     * @return Il contenitore principale del contenuto.
     */
    public StackPane getMainContentContainer() {
        return mainContentContainer;
    }
}