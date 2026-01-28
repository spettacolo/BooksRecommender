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

    public Client getClient() {
        return client;
    }

    public void showUnloggedSidebar() {
        sidebarContainer.getChildren().setAll(new UnLoggedView(this));
    }

    public void showLoggedSidebar(String username) {
        sidebarContainer.getChildren().setAll(new LoggedView(this, username));
    }

    public void showHome() {
        HomeView view = new HomeView(this);
        view.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);

        mainContentContainer.getChildren().setAll(view);
    }

    public void showUserArea(String username) {
        UserAreaView view = new UserAreaView(this, username);
        mainContentContainer.getChildren().setAll(view);
    }

    public void showSearchResults(String query, List<Book> results) {
        SearchView view = new SearchView(this, query, results);
        view.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);

        mainContentContainer.getChildren().setAll(view);
    }

    public void showLibrary(String lib) {
        LibraryView view = new LibraryView(this, lib);

        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);
        view.setMinHeight(Region.USE_COMPUTED_SIZE);

        mainContentContainer.getChildren().setAll(view);
    }

    public static String getUsername() {
        System.out.println(username);
        return username;
    }

    public void setUsername(String username) {
        RootView.username = username;
        System.out.println("set username: " + username);
    }

    public StackPane getMainContentContainer() {
        return mainContentContainer;
    }
}