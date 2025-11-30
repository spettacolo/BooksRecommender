package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.RegLog;
import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

public class HomeView extends HBox {

    private HBox searchBar;
    private VBox mainContent;
    private SearchHandler searchHandler;
    private RegLog regLog = new RegLog();
    private Client client = new Client();

    private HBox userSection;
    private Label loginLabel;
    private Label loginLabelReference;

    private LoggedView loggedView;

    public HomeView() {
        this.setSpacing(0);
        searchHandler = new SearchHandler(client);

        ScrollPane sidebar = createSidebar();
        ScrollPane mainContent = createMainContent();

        this.getChildren().addAll(sidebar, mainContent);

        // Sidebar = fissa
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(200);

        // Main content prende tutto lo spazio restante
        HBox.setHgrow(mainContent, Priority.ALWAYS);
    }

    private ScrollPane createSidebar() {
        BorderPane sidebarContent = new BorderPane();

        VBox topContent = new VBox();
        topContent.setPrefWidth(200);
        topContent.setFillWidth(true);
        VBox.setVgrow(topContent, Priority.ALWAYS);

        // === LABELS SIDEBAR ===
        Label cercaLabel = new Label("Cerca");
        cercaLabel.setPadding(new Insets(10, 20, 10, 20));
        cercaLabel.setOnMouseClicked(event -> {
            if (searchBar != null) {
                boolean show = !searchBar.isVisible();
                searchBar.setVisible(show);
                searchBar.setManaged(show);
            }
        });

        Label homeLabel = new Label("Home");
        homeLabel.setPadding(new Insets(10, 20, 10, 20));

        Label librariesLabel = new Label("Le mie librerie");
        librariesLabel.setPadding(new Insets(10, 20, 10, 20));

        Label newLibraryLabel = new Label("Nuova libreria");
        newLibraryLabel.setPadding(new Insets(10, 20, 10, 20));
        newLibraryLabel.setOnMouseClicked(e -> {
            if (loggedView == null) {
                regLog.showLoginForm(this, client);
            }
        });

        topContent.getChildren().addAll(
                cercaLabel,
                homeLabel,
                librariesLabel,
                newLibraryLabel
        );

        sidebarContent.setCenter(topContent);

        // === USER SECTION ===
        userSection = new HBox(10);
        userSection.setAlignment(Pos.CENTER_LEFT);
        userSection.setPadding(new Insets(15));

        loginLabel = new Label("Login");
        loginLabelReference = loginLabel;

        loginLabel.setOnMouseClicked(e -> regLog.showLoginForm(this, client));

        userSection.getChildren().add(loginLabel);
        sidebarContent.setBottom(userSection);

        // === SCROLLPANE SIDEBAR ===
        ScrollPane scrollPane = new ScrollPane(sidebarContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    public void updateLibrariesInSidebar(String username) {
        Platform.runLater(() -> {
            ScrollPane sidebar = (ScrollPane) this.getChildren().get(0);
            BorderPane sidebarContent = (BorderPane) sidebar.getContent();
            VBox topContent = (VBox) sidebarContent.getCenter();

            int index = -1;
            for (int i = 0; i < topContent.getChildren().size(); i++) {
                if (topContent.getChildren().get(i) instanceof Label lbl) {
                    if (lbl.getText().equals("Le mie librerie")) {
                        index = i;
                        break;
                    }
                }
            }

            if (index == -1) return;

            // Rimuovi le vecchie librerie
            while (topContent.getChildren().size() > index + 1 &&
                    topContent.getChildren().get(index + 1) instanceof Label lbl &&
                    !lbl.getText().equals("Nuova libreria")) {
                topContent.getChildren().remove(index + 1);
            }

            String risposta = client.send("get_user_libraries;" + username);
            System.out.println("Id delle librerie: " + risposta);
            if (risposta != null && !risposta.isEmpty()) {

                String[] libs = risposta.split(",");
                for (String lib : libs) {
                    String nomeLibreria = client.send("get_user_library;id;" + lib);
                    Label libLabel = new Label((nomeLibreria.trim()).split(";")[1]);
                    libLabel.setPadding(new Insets(5, 20, 5, 40));
                    topContent.getChildren().add(index + 1, libLabel);
                    index++;
                }
            }
        });
    }

    public Client getClient() { return client; }

    public Label getNewLibraryLabel() {
        ScrollPane sidebar = (ScrollPane) this.getChildren().get(0);
        BorderPane sidebarContent = (BorderPane) sidebar.getContent();
        VBox topContent = (VBox) sidebarContent.getCenter();

        for (var n : topContent.getChildren()) {
            if (n instanceof Label lbl && lbl.getText().equals("Nuova libreria"))
                return lbl;
        }
        return null;
    }

    private ScrollPane createMainContent() {
        mainContent = new VBox(0);
        mainContent.setFillWidth(true);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 0, 20, 40));
        header.getChildren().add(new Label("Home"));

        searchBar = searchHandler.createSearchBar(mainContent);
        mainContent.getChildren().addAll(searchBar, header);

        // === SEZIONI RIPRISTINATE COME PRIMA ===
        mainContent.getChildren().add(createPopularSection());
        mainContent.getChildren().add(createGenreSection("General"));
        mainContent.getChildren().add(createGenreSection("Romance"));
        mainContent.getChildren().add(createGenreSection("Thrillers"));
        mainContent.getChildren().add(createGenreSection("Fiction"));

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    // ======================
    // SEZIONI LIBRI – VERSIONE BELLA
    // ======================

    private VBox createPopularSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0, 0, 0, 40));

        Label subtitle = new Label("I più popolari >");
        subtitle.setPadding(new Insets(12, 0, 12, 0));

        ScrollPane scroll = createPopularBooksScroll();

        section.getChildren().addAll(subtitle, scroll);
        return section;
    }

    private ScrollPane createPopularBooksScroll() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(false);
        scroll.setFitToWidth(false);
        scroll.setPannable(true);
        scroll.setPrefHeight(140);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                String risposta = client.send("get_book;top;none;20");
                if (risposta == null || risposta.isEmpty()) return null;

                String[] entries = risposta.split("\\|");
                for (String entry : entries) {
                    String[] parts = entry.split(";");
                    if (parts.length < 7) continue;

                    String coverUrl = parts[6];

                    StackPane coverContainer = new StackPane();

                    if (coverUrl != null && !coverUrl.equalsIgnoreCase("null")) {

                        ProgressIndicator loading = new ProgressIndicator();
                        loading.setMaxSize(25, 25);

                        ImageView cover = new ImageView();
                        cover.setPreserveRatio(true);

                        Image img = new Image(coverUrl);
                        img.progressProperty().addListener((obs, oldV, newV) -> {
                            if (newV.doubleValue() >= 1.0) {
                                Platform.runLater(() -> {
                                    cover.setImage(img);
                                    coverContainer.getChildren().remove(loading);
                                });
                            }
                        });

                        coverContainer.getChildren().addAll(loading, cover);
                    } else {
                        Region placeholder = new Region();
                        placeholder.setPrefSize(80, 120);
                        placeholder.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 6;");
                        coverContainer.getChildren().add(placeholder);
                    }

                    Platform.runLater(() -> row.getChildren().add(coverContainer));
                }

                return null;
            }
        };

        new Thread(task).start();
        return scroll;
    }

    private VBox createGenreSection(String genreName) {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0, 0, 0, 40));

        Label subtitle = new Label(genreName + " >");
        subtitle.setPadding(new Insets(12, 0, 12, 0));

        section.getChildren().addAll(subtitle, createGenreBooksScroll(genreName));
        return section;
    }

    private ScrollPane createGenreBooksScroll(String genreName) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setPrefHeight(140);

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

                    StackPane coverContainer = new StackPane();

                    if (coverUrl != null && !coverUrl.equalsIgnoreCase("null")) {

                        ProgressIndicator loading = new ProgressIndicator();
                        loading.setMaxSize(25, 25);

                        ImageView cover = new ImageView();
                        cover.setPreserveRatio(true);

                        Image img = new Image(coverUrl, 80, 120, true, true, true);
                        img.progressProperty().addListener((obs, oldV, newV) -> {
                            if (newV.doubleValue() >= 1.0) {
                                Platform.runLater(() -> {
                                    cover.setImage(img);
                                    coverContainer.getChildren().remove(loading);
                                });
                            }
                        });

                        coverContainer.getChildren().addAll(loading, cover);
                    } else {
                        Region placeholder = new Region();
                        placeholder.setPrefSize(80, 120);
                        placeholder.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 6;");
                        coverContainer.getChildren().add(placeholder);
                    }

                    Platform.runLater(() -> row.getChildren().add(coverContainer));
                }
                return null;
            }
        };

        new Thread(task).start();
        return scroll;
    }

    public Label getLoginLabel() { return loginLabelReference; }

    public void setLoggedView(LoggedView lv) {
        this.loggedView = lv;
    }
}