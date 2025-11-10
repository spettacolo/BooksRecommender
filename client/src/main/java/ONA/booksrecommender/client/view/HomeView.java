package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class HomeView extends HBox {

    private HBox searchBar;
    private VBox mainContent;
    private SearchHandler searchHandler;
    private RegLog regLog = new RegLog();

    public HomeView() {
        this.setSpacing(0);
        searchHandler = new SearchHandler();

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
            regLog.showLoginForm(this);
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
        mainContent.getChildren().addAll(header, searchBar);

        // Nascondi la barra di ricerca cliccando fuori da essa
        mainContent.setOnMouseClicked(event -> {
            if (searchBar.isVisible() && !searchBar.isHover()) {
                searchBar.setVisible(false);
                searchBar.setManaged(false);
            }
        });

        // Aggiunta della sezione "I più popolari"
        mainContent.getChildren().add(createPopularSection());

        mainContent.getChildren().add(createGenreSection("Fantascienza"));
        mainContent.getChildren().add(createGenreSection("Romanzi Storici"));
        mainContent.getChildren().add(createGenreSection("Gialli e Thriller"));
        mainContent.getChildren().add(createGenreSection("Fantasy"));

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createFeaturedSection() {
        HBox featuredSection = new HBox(20);
        HBox.setHgrow(featuredSection, Priority.ALWAYS);

        VBox featuredCard = createFeaturedCard(
                "FEATURED COLLECTION",
                "Check out summer's hottest listens."
        );

        VBox staffCard = createFeaturedCard(
                "STAFF PICKS",
                "Dig into 25 captivating true stories."
        );

        VBox thirdCard = createFeaturedCard(
                "STAFF PICKS",
                "Check out this collection."
        );

        featuredSection.getChildren().addAll(featuredCard, staffCard, thirdCard);
        return featuredSection;
    }

    private VBox createFeaturedCard(String category, String title) {
        VBox card = new VBox(10);
        card.setPrefSize(300, 200);
        card.setMinSize(300, 200);
        card.setPadding(new Insets(20));

        Label categoryLabel = new Label(category);
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);

        Region decoration = new Region();
        decoration.setPrefSize(80, 80);

        card.getChildren().addAll(categoryLabel, titleLabel, decoration);
        return card;
    }

    private ScrollPane createBooksHorizontalScroll() {
        HBox booksList = new HBox(15);
        booksList.setFillHeight(true);
        booksList.setPadding(new Insets(10, 0, 10, 0));
        booksList.setAlignment(Pos.CENTER_LEFT);
        booksList.setPrefWidth(Region.USE_COMPUTED_SIZE);

        String[] bookTitles = {
                "Literary Genius", "Walking Robot", "A Book", "Adult Reading",
                "Book Cover", "Another Book", "Yet Another", "Final Book"
        };

        for (String bookTitle : bookTitles) {
            VBox bookItem = createBookItem(bookTitle);
            booksList.getChildren().add(bookItem);
        }

        // === Scroll SOLO orizzontale per le righe di libri ===
        ScrollPane scrollPane = new ScrollPane(booksList);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // orizzontale
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);     // no verticale
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false); // evita resize orizzontale
        scrollPane.setPannable(true);
        scrollPane.setMinViewportWidth(200);
        scrollPane.setPrefHeight(160);
        scrollPane.setPrefViewportHeight(160);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        return scrollPane;
    }

    private VBox createBookItem(String title) {
        VBox bookItem = new VBox(8);
        bookItem.setAlignment(Pos.TOP_CENTER);

        StackPane cover = new StackPane();
        cover.setPrefSize(90, 120);
        bookItem.getChildren().add(cover);

        return bookItem;
    }

    // Sezione "I più popolari >" con scroll orizzontale di placeholder
    private VBox createPopularSection() {
        VBox section = new VBox(0);
        section.setPadding(new Insets(0, 0, 0, 40));

        Label subtitle = new Label("I più popolari >");
        subtitle.setPadding(new Insets(12, 0, 12, 0));

        ScrollPane popularScroll = createPopularBooksScroll();

        section.getChildren().addAll(subtitle, popularScroll);
        return section;
    }

    // ScrollPane orizzontale con 20 placeholder rettangolari
    private ScrollPane createPopularBooksScroll() {
        HBox placeholders = new HBox(15);
        placeholders.setAlignment(Pos.CENTER_LEFT);
        placeholders.setPadding(new Insets(0, 0, 0, 0));
        for (int i = 0; i < 20; i++) {
            Region rect = new Region();
            rect.setPrefSize(80, 120);
            rect.setMinSize(80, 120);
            rect.setMaxSize(80, 120);
            rect.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
            placeholders.getChildren().add(rect);
        }

        ScrollPane scrollPane = new ScrollPane(placeholders);
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

        ScrollPane genreScroll = createGenreBooksScroll();

        section.getChildren().addAll(subtitle, genreScroll);
        return section;
    }

    private ScrollPane createGenreBooksScroll() {
        HBox placeholders = new HBox(15);
        placeholders.setAlignment(Pos.CENTER_LEFT);
        placeholders.setPadding(new Insets(0, 0, 0, 0));
        for (int i = 0; i < 20; i++) {
            Region rect = new Region();
            rect.setPrefSize(80, 120);
            rect.setMinSize(80, 120);
            rect.setMaxSize(80, 120);
            rect.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
            placeholders.getChildren().add(rect);
        }
        ScrollPane scrollPane = new ScrollPane(placeholders);
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