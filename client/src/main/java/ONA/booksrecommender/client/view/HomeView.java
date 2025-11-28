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
    // Sidebar user section
    private HBox userSection;
    // Sidebar login label reference
    private Label loginLabel;
    private Label loginLabelReference;
    private LoggedView loggedView;
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
            if (loggedView == null) {
                // Utente non loggato -> apri login
                regLog.showLoginForm(this, client);
            } else {
                // Utente loggato -> crea nuova libreria
                javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
                dialog.setTitle("Nuova Libreria");
                dialog.setHeaderText("Crea una nuova libreria");
                dialog.setContentText("Nome libreria:");

                dialog.showAndWait().ifPresent(libraryName -> {
                    if (!libraryName.trim().isEmpty()) {
                        boolean ok = client.addLibrary(libraryName, loggedView.getUsername());
                        if (ok) {
                            updateLibrariesInSidebar(loggedView.getUsername());
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Successo");
                            alert.setHeaderText(null);
                            alert.setContentText("Libreria creata correttamente!");
                            alert.showAndWait();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Errore");
                            alert.setHeaderText(null);
                            alert.setContentText("Impossibile creare la libreria.");
                            alert.showAndWait();
                        }
                    }
                });
            }
        });
        topContent.getChildren().add(newLibraryLabel);

        sidebarContent.setCenter(topContent);

        // User section at the bottom (class-level field)
        userSection = new HBox(10);
        userSection.setAlignment(Pos.CENTER_LEFT);
        userSection.setPadding(new Insets(10));

        loginLabel = new Label("Login");
        loginLabelReference = loginLabel;

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

    public void updateLibrariesInSidebar(String username) {
        Platform.runLater(() -> {
            // Trova VBox che contiene le etichette "Le mie librerie" e "Nuova libreria"
            BorderPane sidebarContent = (BorderPane)((ScrollPane)this.getChildren().get(0)).getContent();
            VBox topContent = (VBox) sidebarContent.getCenter();

            // Trova l’indice di "Le mie librerie"
            int index = -1;
            for (int i = 0; i < topContent.getChildren().size(); i++) {
                if (topContent.getChildren().get(i) instanceof Label) {
                    Label lbl = (Label) topContent.getChildren().get(i);
                    if (lbl.getText().equals("Le mie librerie")) {
                        index = i;
                        break;
                    }
                }
            }
            if (index == -1) return;

            // Rimuovi eventuali librerie già presenti
            while (topContent.getChildren().size() > index + 1 &&
                    topContent.getChildren().get(index + 1) instanceof Label &&
                    !((Label)topContent.getChildren().get(index + 1)).getText().equals("Nuova libreria")) {
                topContent.getChildren().remove(index + 1);
            }

            // Recupera librerie dal server
            String risposta = client.send("get_user_libraries;" + username);
            if (risposta != null && !risposta.isEmpty()) {
                String[] libraryNames = risposta.split(",");
                for (String libName : libraryNames) {
                    Label libLabel = new Label(libName.trim());
                    libLabel.setPadding(new Insets(5, 20, 5, 40)); // leggero rientro
                    libLabel.setOnMouseClicked(e -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Libreria");
                        alert.setHeaderText(null);
                        alert.setContentText("Hai selezionato la libreria: " + libName);
                        alert.showAndWait();
                    });
                    topContent.getChildren().add(index + 1, libLabel);
                    index++; // inserimento in sequenza
                }
            }
        });
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

        //String risposta = client.send("get_book;top;none;20");

        ScrollPane popularScroll = createPopularBooksScroll();

        section.getChildren().addAll(subtitle, popularScroll);

        return section;
    }

    // ScrollPane orizzontale con 20 placeholder rettangolari
    private ScrollPane createPopularBooksScroll() {
        HBox booksRow = new HBox(15);
        booksRow.setAlignment(Pos.CENTER_LEFT);
        booksRow.setPadding(new Insets(0, 0, 0, 0));

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

        // Caricamento asincrono delle copertine
        Task<Void> loadImagesTask = new Task<>() {
            @Override
            protected Void call() {
                // Chiamata aggiornata per i libri più popolari (top globali)
                String risposta = client.send("get_book;top;none;20");
                if (risposta == null || risposta.isEmpty()) return null;
                String[] entries = risposta.split("\\|");
                for (String entry : entries) {
                    String[] parts = entry.split(";");
                    if (parts.length >= 7) {
                        String coverUrl = parts[6];

                        StackPane coverContainer = new StackPane();
                        coverContainer.setPrefSize(80, 120);

                        if (coverUrl != null && !coverUrl.equalsIgnoreCase("null")) {
                            ProgressIndicator progress = new ProgressIndicator();
                            progress.setMaxSize(30, 30);
                            coverContainer.getChildren().add(progress);

                            ImageView cover = new ImageView();
                            cover.setFitWidth(80);
                            cover.setFitHeight(120);
                            cover.setPreserveRatio(true);

                            Image img = new Image(coverUrl, 80, 120, true, true, true);
                            img.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                                if (newProgress.doubleValue() >= 1.0) {
                                    Platform.runLater(() -> {
                                        cover.setImage(img);
                                        coverContainer.getChildren().remove(progress);
                                    });
                                }
                            });

                            coverContainer.getChildren().add(cover);
                        } else {
                            Region placeholder = new Region();
                            placeholder.setPrefSize(80, 120);
                            placeholder.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 6;");
                            coverContainer.getChildren().add(placeholder);
                        }

                        Platform.runLater(() -> booksRow.getChildren().add(coverContainer));
                    }
                }
                return null;
            }
        };

        new Thread(loadImagesTask).start();

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

        // Caricamento asincrono delle copertine
        Task<Void> loadImagesTask = new Task<>() {
            @Override
            protected Void call() {
                String risposta = client.send("get_book;top;" + genreName + ";20");
                if (risposta != null && !risposta.isEmpty()) {
                    String[] entries = risposta.split("\\|");
                    for (String entry : entries) {
                        String[] parts = entry.split(";");
                        if (parts.length >= 7) {
                            String coverUrl = parts[6];

                            StackPane coverContainer = new StackPane();
                            coverContainer.setPrefSize(80, 120);

                            if (coverUrl != null && !coverUrl.equalsIgnoreCase("null")) {
                                ProgressIndicator progress = new ProgressIndicator();
                                progress.setMaxSize(30, 30);
                                coverContainer.getChildren().add(progress);

                                ImageView cover = new ImageView();
                                cover.setFitWidth(80);
                                cover.setFitHeight(120);
                                cover.setPreserveRatio(true);

                                Image img = new Image(coverUrl, 80, 120, true, true, true);
                                img.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                                    if (newProgress.doubleValue() >= 1.0) {
                                        Platform.runLater(() -> {
                                            cover.setImage(img);
                                            coverContainer.getChildren().remove(progress);
                                        });
                                    }
                                });

                                coverContainer.getChildren().add(cover);
                            } else {
                                Region placeholder = new Region();
                                placeholder.setPrefSize(80, 120);
                                placeholder.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 6;");
                                coverContainer.getChildren().add(placeholder);
                            }

                            Platform.runLater(() -> booksRow.getChildren().add(coverContainer));
                        }
                    }
                }
                return null;
            }
        };

        new Thread(loadImagesTask).start();

        return scrollPane;
    }

    public Label getLoginLabel() {
        return loginLabelReference;
    }

    public void setLoggedView(LoggedView lv) {
        this.loggedView = lv;
    }
}