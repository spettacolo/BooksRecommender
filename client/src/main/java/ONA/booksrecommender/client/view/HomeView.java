package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.SearchHandler;
import ONA.booksrecommender.client.view.BookDetails;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

public class HomeView extends VBox {

    private VBox mainContent;
    private SearchHandler searchHandler;
    private Client client;
    private HBox searchBar;
    private StackPane overlayContainer; // aggiunto campo overlayContainer

    public HomeView(RootView root) {
        this.client = root.getClient();

        // HomeView deve riempire tutto lo spazio
        this.setFillWidth(true);

        searchHandler = new SearchHandler(client);

        ScrollPane mainContentPane = createMainContent();

        overlayContainer = new StackPane();
        overlayContainer.setPickOnBounds(false);

        StackPane mainStack = new StackPane(mainContentPane, overlayContainer);

        searchBar = searchHandler.createSearchBar(root);

        // Add searchBar to the header inside mainContent
        HBox header = (HBox) mainContent.getChildren().get(0);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, searchBar);

        this.getChildren().add(mainStack);
    }

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

    private VBox createGenreSection(String genreName) {
        VBox section = new VBox();
        section.getStyleClass().add("genre-section");
        section.setSpacing(30);
        section.setFillWidth(true);

        Label subtitle;
        if ("none".equals(genreName)) {
            subtitle = new Label("I più popolari >");
            subtitle.getStyleClass().add("genre-subtitle");

        } else {
            subtitle = new Label(genreName + " >");
            subtitle.getStyleClass().add("genre-subtitle");
        }
        subtitle.setPadding(new Insets(0, 0, 0, 10));
        ScrollPane booksScroll = createGenreBooksScroll(genreName);

        section.getChildren().addAll(subtitle, booksScroll);
        return section;
    }

    private ScrollPane createGenreBooksScroll(String genreName) {
        HBox row = new HBox(30);
        row.setAlignment(Pos.BOTTOM_CENTER);
        row.getStyleClass().add("books-row");
        row.setPadding(new Insets(0, 15, 0, 15));

        VBox wrapper = new VBox(row);
        wrapper.setFillWidth(true);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToHeight(false);
        scroll.getStyleClass().add("books-scroll");

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setContent(wrapper);

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