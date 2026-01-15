package ONA.booksrecommender.client.view;

import java.util.List;

import ONA.booksrecommender.client.controller.*;
import ONA.booksrecommender.objects.Book;
import javafx.scene.layout.*;
import ONA.booksrecommender.client.Client;
import javafx.scene.control.ScrollPane;

public class RootView extends HBox {

    private VBox sidebarContainer = new VBox();
    private VBox mainContentContainer = new VBox();
    private Client client = new Client();
    private SearchHandler searchHandler = new SearchHandler(client);
    private HBox searchBar;
    private static String username = "";

    public RootView() {

        this.setSpacing(0);

        // Sidebar: fissa
        sidebarContainer.setPrefWidth(180);
        sidebarContainer.setMinWidth(180);
        sidebarContainer.setMaxWidth(180);
        sidebarContainer.setStyle("-fx-padding: 30 20 10 20;");

        // MAIN CONTENT: deve espandersi
        mainContentContainer.setFillWidth(true);
        mainContentContainer.setMinWidth(0);
        mainContentContainer.setPrefWidth(Double.MAX_VALUE);
        mainContentContainer.prefWidthProperty().bind(this.widthProperty().subtract(sidebarContainer.widthProperty()));

        HBox.setHgrow(mainContentContainer, Priority.ALWAYS);
        VBox.setVgrow(mainContentContainer, Priority.ALWAYS);

        // Initialize searchBar once
        searchBar = searchHandler.createSearchBar(this);

        VBox rightContainer = new VBox();
        rightContainer.setFillWidth(true);

        rightContainer.getChildren().setAll(mainContentContainer);
        VBox.setVgrow(mainContentContainer, Priority.ALWAYS);

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

        // 1. Assicurati che la view possa espandersi all'infinito
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
        this.username = username;
        System.out.println("set username: " + username);
    }
}