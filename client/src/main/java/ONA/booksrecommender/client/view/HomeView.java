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

public class HomeView extends HBox {

    private HBox searchBar;
    private VBox mainContent;
    private SearchHandler searchHandler;
    private RegLog regLog = new RegLog();
    private Client client =  new Client();
    public HomeView() {
        this.setSpacing(0);
        searchHandler = new SearchHandler(client);

        // === SIDEBAR SINISTRA ===
        ScrollPane sidebar = createSidebar();

        // === CONTENUTO PRINCIPALE ===
        ScrollPane mainContent = createMainContent();

        // Aggiunta dei componenti principali
        this.getChildren().addAll(sidebar, mainContent);

        // Configurazione delle proporzioni
        HBox.setHgrow(sidebar, Priority.NEVER);
        VBox.setVgrow(sidebar, Priority.ALWAYS);
        HBox.setHgrow(mainContent, Priority.ALWAYS);
    }

    private ScrollPane createSidebar() {
        BorderPane sidebarContent = new BorderPane();

        VBox topContent = new VBox();
        topContent.setPrefWidth(200);
        topContent.setMinWidth(200);
        topContent.setMaxWidth(200);
        topContent.setFillWidth(true);
        VBox.setVgrow(topContent, Priority.ALWAYS);
        topContent.setPrefHeight(Double.MAX_VALUE);

        Label cercaLabel = new Label("Cerca");
        cercaLabel.setPadding(new Insets(5, 20, 5, 20));
        cercaLabel.setOnMouseClicked(event -> {
            if (searchBar != null) {
                boolean currentlyVisible = searchBar.isVisible();
                searchBar.setVisible(!currentlyVisible);
                searchBar.setManaged(!currentlyVisible);
                if (!currentlyVisible) {
                    searchBar.toFront();
                }
            }
        });

        Label homeLabel = new Label("Home");
        homeLabel.setPadding(new Insets(5, 20, 5, 20));
        homeLabel.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informazione");
            alert.setHeaderText(null);
            alert.setContentText("home");
            alert.showAndWait();
        });


        topContent.getChildren().addAll(cercaLabel, homeLabel);

        // "Le mie librerie" label (non cliccabile)
        Label librariesLabel = new Label("Le mie librerie");
        librariesLabel.setPadding(new Insets(5, 20, 5, 20));
        topContent.getChildren().add(librariesLabel);

        // "Nuova libreria" label (cliccabile, mostra alert)
        Label newLibraryLabel = new Label("Nuova libreria");
        newLibraryLabel.setPadding(new Insets(5, 20, 5, 20));
        newLibraryLabel.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informazione");
            alert.setHeaderText(null);
            alert.setContentText("Nuova libreria");
            alert.showAndWait();
        });
        topContent.getChildren().add(newLibraryLabel);

        sidebarContent.setCenter(topContent);

        HBox userSection = new HBox();
        userSection.setAlignment(Pos.BOTTOM_CENTER);

        Label loginLabel = new Label("Login");
        loginLabel.setOnMouseClicked(event -> {
            regLog.showLoginForm(this, this.client);
        });

        userSection.getChildren().add(loginLabel);

        sidebarContent.setBottom(userSection);

        ScrollPane scrollPane = new ScrollPane(sidebarContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(200);
        scrollPane.setMinWidth(200);
        scrollPane.setMaxWidth(200);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        return scrollPane;
    }

    private HBox createMenuItem(String text, String iconClass, boolean isSelected) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setPickOnBounds(true);

        if (iconClass.equals("search-icon") || iconClass.equals("home-icon") || iconClass.equals("plus-icon")) {
            try {
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/" + iconClass + ".png")));
                icon.setFitWidth(16);
                icon.setFitHeight(16);
                icon.setPreserveRatio(true);

                Label label = new Label(text);
                item.getChildren().addAll(icon, label);
            } catch (Exception e) {
                Region fallbackIcon = new Region();
                fallbackIcon.setPrefSize(16, 16);

                Label label = new Label(text);
                item.getChildren().addAll(fallbackIcon, label);
            }
        } else {
            Region icon = new Region();
            icon.setPrefSize(16, 16);

            Label label = new Label(text);
            item.getChildren().addAll(icon, label);
        }

        if ("Cerca".equals(text)) {
            item.setOnMouseClicked(event -> {
                if (searchBar != null) {
                    searchBar.setVisible(true);
                    searchBar.setManaged(true);
                    searchBar.toFront();
                }
            });
        }

        return item;
    }

    private HBox createUserProfile() {
        HBox userProfile = new HBox(10);
        userProfile.setPadding(new Insets(15, 20, 20, 20));
        userProfile.setAlignment(Pos.CENTER_LEFT);

        Region avatar = new Region();
        avatar.setPrefSize(32, 32);

        Label userName = new Label("Login");

        userProfile.getChildren().addAll(avatar, userName);
        return userProfile;
    }

    private ScrollPane createMainContent() {
        mainContent = new VBox(0);
        mainContent.setFillWidth(true);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 0, 20, 40));

        Label title = new Label("Home");

        header.getChildren().addAll(title);

        // Barra di ricerca gestita da SearchHandler
        searchBar = searchHandler.createSearchBar(mainContent);
        mainContent.getChildren().addAll(searchBar, header);

        // Nascondi la barra di ricerca cliccando fuori da essa
        mainContent.setOnMouseClicked(event -> {
            if (searchBar.isVisible() && !searchBar.isHover()) {
                searchBar.setVisible(false);
                searchBar.setManaged(false);
            }
        });

        // Aggiunta della sezione "I più popolari"
        mainContent.getChildren().add(createPopularSection());

        mainContent.getChildren().add(createGenreSection("General"));
        mainContent.getChildren().add(createGenreSection("Romance"));
        mainContent.getChildren().add(createGenreSection("Thrillers"));
        mainContent.getChildren().add(createGenreSection("Fiction"));

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }


    // Sezione "I più popolari >" con scroll orizzontale di placeholder
    private VBox createPopularSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0, 0, 0, 40));

        Label subtitle = new Label("I più popolari >");
        subtitle.setPadding(new Insets(12, 0, 12, 0));

        String risposta = client.send("get_book;top;business&economics;20");

        ScrollPane popularScroll = createPopularBooksScroll();

        section.getChildren().addAll(subtitle, popularScroll);
        return section;
    }

    // ScrollPane orizzontale con 20 placeholder rettangolari
    private ScrollPane createPopularBooksScroll() {
        HBox booksRow = new HBox(15);
        booksRow.setAlignment(Pos.CENTER_LEFT);
        booksRow.setPadding(new Insets(0, 0, 0, 0));

        String risposta = client.send("get_book;top;;20");
        System.out.println(risposta);
        if (risposta != null && !risposta.isEmpty()) {
            String[] entries = risposta.split("\\|");
            for (String entry : entries) {
                String[] parts = entry.split(";");
                if (parts.length >= 7) {
                    String coverUrl = parts[6];

                    ImageView cover = new ImageView();
                    try {
                        if (coverUrl != null && !coverUrl.equalsIgnoreCase("null")) {
                            Image img = new Image(coverUrl, 80, 120, true, true);
                            cover.setImage(img);
                        }
                    } catch (Exception ignored) {}

                    cover.setFitWidth(80);
                    cover.setFitHeight(120);
                    cover.setPreserveRatio(true);
                    booksRow.getChildren().add(cover);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(booksRow);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setPannable(true);
        scrollPane.setMinViewportWidth(200);
        scrollPane.setPrefHeight(120);
        scrollPane.setPrefViewportHeight(120);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private VBox createGenreSection(String genreName) {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0, 0, 0, 40));

        Label subtitle = new Label(genreName + " >");
        subtitle.setPadding(new Insets(12, 0, 12, 0));

        ScrollPane genreScroll = createGenreBooksScroll(genreName);

        section.getChildren().addAll(subtitle, genreScroll);
        return section;
    }

    private ScrollPane createGenreBooksScroll(String genreName) {
        HBox booksRow = new HBox(15);
        booksRow.setAlignment(Pos.CENTER_LEFT);
        booksRow.setPadding(new Insets(0, 0, 0, 0));

        String risposta = client.send("get_book;top;" + genreName.toLowerCase() + ";20");
        System.out.println("GENRE " + genreName + " -> " + risposta);

        if (risposta != null && !risposta.isEmpty()) {
            String[] entries = risposta.split("\\|");
            for (String entry : entries) {
                String[] parts = entry.split(";");
                if (parts.length >= 7) {
                    String coverUrl = parts[6];

                    ImageView cover = new ImageView();
                    try {
                        if (coverUrl != null && !coverUrl.equalsIgnoreCase("null")) {
                            Image img = new Image(coverUrl, 80, 120, true, true);
                            cover.setImage(img);
                        }
                    } catch (Exception ignored) {}

                    cover.setFitWidth(80);
                    cover.setFitHeight(120);
                    cover.setPreserveRatio(true);
                    booksRow.getChildren().add(cover);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(booksRow);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setPannable(true);
        scrollPane.setMinViewportWidth(200);
        scrollPane.setPrefHeight(120);
        scrollPane.setMinHeight(120);
        scrollPane.setPrefViewportHeight(120);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }
}