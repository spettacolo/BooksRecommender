package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.SearchHandler;
import ONA.booksrecommender.objects.Book;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class SearchView extends VBox {

    private VBox mainContent;
    private StackPane overlayContainer;
    private HBox searchBar;
    private SearchHandler searchHandler;

    public SearchView(RootView root, String query, List<Book> results) {
        this.setFillWidth(true);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Client client = root.getClient();
        searchHandler = new SearchHandler(client);

        overlayContainer = new StackPane();
        overlayContainer.setPickOnBounds(false);

        mainContent = new VBox(0);
        mainContent.setFillWidth(true);

        // Header with title and search bar (same logic as HomeView)
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 40, 20, 40));

        Label titleLabel = new Label(
                (query == null || query.isBlank())
                        ? "Risultati ricerca"
                        : "Risultati per: " + query
        );
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // usa i campi della classe
        searchBar = searchHandler.createSearchBar(root);

        header.getChildren().addAll(titleLabel, spacer, searchBar);
        mainContent.getChildren().add(header);

        // VBox for book results
        VBox resultsBox = new VBox(12);
        resultsBox.setPadding(new Insets(8, 20, 8, 20));
        resultsBox.setFillWidth(true);
        VBox.setVgrow(resultsBox, Priority.ALWAYS);

        if (results == null || results.isEmpty()) {
            resultsBox.getChildren().add(new Label("Nessun risultato trovato."));
        } else {
            for (Book b : results) {
                HBox bookBox = new HBox(16);
                bookBox.setPadding(new Insets(12));
                bookBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(bookBox, Priority.ALWAYS);

                ImageView cover = new ImageView();
                cover.setFitWidth(120);
                cover.setFitHeight(180);
                cover.setPreserveRatio(true);
                if (b.getCoverImageUrl() != null && !"null".equals(b.getCoverImageUrl())) {
                    cover.setImage(new Image(b.getCoverImageUrl(), true));
                }

                int bookId = b.getId();
                bookBox.setOnMouseClicked(e -> {
                    BookDetails details = new BookDetails(bookId, root.getUsername());
                    StackPane overlay = details.createOverlay();
                    overlayContainer.getChildren().add(overlay);
                });

                VBox info = new VBox(4);
                info.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label titleLbl = new Label(b.getTitle());
                titleLbl.setStyle("-fx-font-weight: bold;");
                titleLbl.setWrapText(true);
                titleLbl.setMaxWidth(Double.MAX_VALUE);

                Label authors = new Label(String.join(", ", b.getAuthors()));
                authors.setWrapText(true);
                authors.setMaxWidth(Double.MAX_VALUE);

                info.getChildren().addAll(titleLbl, authors);
                bookBox.getChildren().addAll(cover, info);
                resultsBox.getChildren().add(bookBox);
            }
        }

        // aggiungi i risultati sotto l'header
        mainContent.getChildren().add(resultsBox);
        VBox.setVgrow(resultsBox, Priority.ALWAYS);

        // ScrollPane che contiene TUTTO il mainContent (header + risultati)
        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // forza la larghezza corretta come in HomeView
        scroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            mainContent.setPrefWidth(newVal.getWidth());
        });

        // Stack con overlay sopra
        StackPane mainStack = new StackPane(scroll, overlayContainer);
        VBox.setVgrow(mainStack, Priority.ALWAYS);

        this.getChildren().add(mainStack);
    }
}