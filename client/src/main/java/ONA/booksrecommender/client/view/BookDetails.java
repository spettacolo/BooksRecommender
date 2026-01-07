package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.AddRmBook;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.ScrollPane;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BookDetails {

    private final int bookId;
    private final String username;
    private String title;
    private String authors;
    private String year;
    private String publisher;
    private String category;
    private String coverUrl;
    private String descrizione;

    private final Client client = new Client();

    public BookDetails(int bookId, String username) {
        this.bookId = bookId;
        this.username = username;
        loadBookDetails();
    }

    private void loadBookDetails() {
        String response = client.send("get_book;id;" + bookId);
        if (response == null || response.startsWith("ERROR")) {
            this.title = "Libro non trovato";
            this.authors = "";
            this.year = "";
            this.publisher = "";
            this.category = "";
            this.coverUrl = null;
            this.descrizione = "";
            return;
        }

        String[] parts = response.split(";");
        this.title = parts.length > 1 ? parts[1] : "";
        this.authors = parts.length > 2 ? parts[2] : "";
        this.year = parts.length > 3 ? parts[3] : "";
        this.publisher = parts.length > 4 ? parts[4] : "";
        this.category = parts.length > 5 ? parts[5] : "";
        this.coverUrl = parts.length > 6 ? parts[6] : null;
        if (parts.length > 7 && parts[7] != null && !parts[7].isEmpty()) {
            try {
                this.descrizione = new String(Base64.getDecoder().decode(parts[7]), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                this.descrizione = parts[7];
            }
        } else this.descrizione = "";
    }

    private Label buildRatingLabel() {
        String response = client.send("get_book_ratings;" + bookId);
        if (response == null || response.isEmpty()) return new Label("Nessuna recensione");

        String[] ratings = response.split("\\|");
        int count = 0, sum = 0;
        for (String r : ratings) {
            if (r.isBlank()) continue;
            String[] parts = r.split(";");
            if (parts.length < 8) continue;
            try { sum += Integer.parseInt(parts[7]); count++; } catch (NumberFormatException ignored) {}
        }
        if (count == 0) return new Label("Nessuna recensione");

        int fullStars = (int)Math.round((double)sum / count);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) stars.append(i < fullStars ? "★" : "☆");

        Label label = new Label(stars + " (" + count + ")");
        label.setStyle("-fx-font-size: 16px;");
        return label;
    }

    public StackPane createOverlay() {
        final StackPane overlay = new StackPane();
        VBox content = new VBox(20);
        // Margine interno tra bordo overlay e contenuti a 20
        content.setPadding(new Insets(20));

        content.setStyle("-fx-background-color: transparent;");
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        content.setPrefWidth(Region.USE_COMPUTED_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setOnMouseClicked(e -> e.consume());

        // HEADER
        Label closeButton = new Label("✕");
        closeButton.setStyle("-fx-font-size: 18px;");
        // Chiude l'overlay cliccando la X
        closeButton.setOnMouseClicked(e -> {
            Pane parent = (Pane) overlay.getParent();
            if (parent != null) parent.getChildren().remove(overlay);
        });

        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(closeButton, spacer);

        // PULSANTI + e -
        if (username != null && !username.isEmpty()) {
            AddRmBook addRmBook = new AddRmBook();
            List<AddRmBook.LibraryInfo> libsWithoutBook = addRmBook.getLibrariesWithoutBook(client, username, bookId);
            List<AddRmBook.LibraryInfo> libsWithBook = addRmBook.getLibrariesWithBook(client, username, bookId);

            HBox buttonsBox = new HBox(10);
            buttonsBox.setAlignment(Pos.TOP_RIGHT);

            if (!libsWithoutBook.isEmpty()) {
                Label addButton = new Label("+");
                addButton.setStyle("-fx-font-size: 24px;");
                addButton.setOnMouseClicked(e -> {
                    List<String> libNames = new ArrayList<>();
                    for (AddRmBook.LibraryInfo lib : libsWithoutBook) libNames.add(lib.getName());
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(libNames.get(0), libNames);
                    dialog.setTitle("Aggiungi libro a libreria");
                    dialog.setHeaderText("Seleziona una libreria dove aggiungere il libro");
                    dialog.setContentText("Libreria:");
                    dialog.showAndWait().ifPresent(selectedName -> {
                        for (AddRmBook.LibraryInfo lib : libsWithoutBook) {
                            if (lib.getName().equals(selectedName)) {
                                client.send("add_book_to_library;" + lib.getId() + ";" + bookId);
                                break;
                            }
                        }
                    });
                });
                buttonsBox.getChildren().add(addButton);
            }

            if (!libsWithBook.isEmpty()) {
                Label removeButton = new Label("-");
                removeButton.setStyle("-fx-font-size: 24px;");
                removeButton.setOnMouseClicked(e -> {
                    List<String> libNames = new ArrayList<>();
                    for (AddRmBook.LibraryInfo lib : libsWithBook) libNames.add(lib.getName());
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(libNames.get(0), libNames);
                    dialog.setTitle("Rimuovi libro da libreria");
                    dialog.setHeaderText("Seleziona una libreria da cui rimuovere il libro");
                    dialog.setContentText("Libreria:");
                    dialog.showAndWait().ifPresent(selectedName -> {
                        for (AddRmBook.LibraryInfo lib : libsWithBook) {
                            if (lib.getName().equals(selectedName)) {
                                client.send("remove_book_from_library;" + lib.getId() + ";" + bookId);
                                break;
                            }
                        }
                    });
                });
                buttonsBox.getChildren().add(removeButton);
            }

            header.getChildren().add(buttonsBox);
        }

        // CONTENUTO PRINCIPALE (copertina a sinistra, dettagli a destra)
        HBox mainContent = new HBox(20);

        VBox coverBox = new VBox();
        coverBox.setAlignment(Pos.TOP_LEFT);
        if (coverUrl != null && !coverUrl.isEmpty()) {
            try {
                Image coverImage = new Image(coverUrl, 150, 220, true, true);
                ImageView coverView = new ImageView(coverImage);
                coverBox.getChildren().add(coverView);
            } catch (Exception ignored) {}
        }

        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.TOP_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label authorLabel = new Label(authors);
        authorLabel.setStyle("-fx-font-size: 14px;");
        Label publisherYearLabel = new Label((publisher.isEmpty() ? "" : publisher) + (year.isEmpty() ? "" : " - " + year));
        publisherYearLabel.setStyle("-fx-font-size: 14px;");
        Label categoryLabel = new Label(category);
        categoryLabel.setStyle("-fx-font-size: 14px;");
        Label ratingLabel = buildRatingLabel();
        detailsBox.getChildren().addAll(titleLabel, authorLabel, publisherYearLabel, categoryLabel, ratingLabel);

        mainContent.getChildren().addAll(coverBox, detailsBox);

        Label descriptionTitle = new Label("Sinossi");
        descriptionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label descriptionLabel = new Label(descrizione);
        descriptionLabel.setWrapText(true);

        Separator divider = new Separator();
        divider.setPadding(new Insets(20, 0, 10, 0));

        // --- BEGIN reviews header block with optional add review icon ---
        HBox reviewsHeader = new HBox(8);
        reviewsHeader.setAlignment(Pos.CENTER_LEFT);

        Label reviewsTitle = new Label("Valutazioni e recensioni");
        reviewsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        reviewsHeader.getChildren().add(reviewsTitle);

        // Mostra icona recensione solo se loggato e il libro è in almeno una libreria
        if (username != null && !username.isEmpty()) {
            List<AddRmBook.LibraryInfo> libsWithBook = AddRmBook.getLibrariesWithBook(client, username, bookId);

            if (libsWithBook != null && !libsWithBook.isEmpty()) {
                Label addReviewIcon = new Label("✍️");
                addReviewIcon.setStyle("-fx-font-size: 18px;");
                addReviewIcon.setOnMouseClicked(e -> {
                    ReviewDialog dialog = new ReviewDialog(bookId, username, client);
                    dialog.show();
                });
                reviewsHeader.getChildren().add(addReviewIcon);
            }
        }
        // --- END reviews header block ---

        VBox reviewsBox = new VBox(10);
        reviewsBox.setAlignment(Pos.TOP_LEFT);

        String ratingsResponse = client.send("get_book_ratings;" + bookId);
        if (ratingsResponse == null || ratingsResponse.isEmpty()) {
            Label noReviewsLabel = new Label("Nessuna valutazione o recensione disponibile per questo libro");
            noReviewsLabel.setStyle("-fx-font-style: italic;");
            reviewsBox.getChildren().add(noReviewsLabel);
        } else {
            Label noReviewsLabel = new Label("Nessuna valutazione o recensione disponibile per questo libro");
            noReviewsLabel.setStyle("-fx-font-style: italic;");
            reviewsBox.getChildren().add(noReviewsLabel);
        }

        content.getChildren().addAll(header, mainContent, descriptionTitle, descriptionLabel, divider, reviewsHeader, reviewsBox);

        overlay.setPickOnBounds(true);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // DARK BACKGROUND (not clickable, click handled on outerContainer)
        Region darkBackground = new Region();
        darkBackground.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        darkBackground.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        darkBackground.setPickOnBounds(true);

        // OUTER CONTAINER WITH REQUIRED MARGINS (50,50,0,50)
        StackPane outerContainer = new StackPane();
        outerContainer.setPadding(new Insets(50));
        outerContainer.setStyle("-fx-background-color: transparent;");
        outerContainer.setPickOnBounds(false);
        // Cliccando fuori dal pannello, chiudi overlay
        outerContainer.setOnMouseClicked(e -> {
            // Solo se si clicca sull'outerContainer e non su overlayPanel
            if (e.getTarget() == outerContainer) {
                Pane parent = (Pane) overlay.getParent();
                if (parent != null) parent.getChildren().remove(overlay);
            }
        });

        // INNER OVERLAY PANEL
        BorderPane overlayPanel = new BorderPane();
        // Only overlayPanel gets the corner radius
        overlayPanel.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 14 14 0 0;" +
            "-fx-border-radius: 14 14 0 0;"
        );
        // CLIP con angoli superiori arrotondati e inferiori dritti
        Rectangle clip = new Rectangle();
        clip.setArcWidth(28);
        clip.setArcHeight(28);

        // Applica il clip
        overlayPanel.setClip(clip);

        // Aggiorna dinamicamente il clip sulle dimensioni del pannello
        overlayPanel.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            clip.setWidth(newBounds.getWidth());
            clip.setHeight(newBounds.getHeight());

            // Trucco JavaFX: azzera l’arrotondamento in basso
            clip.setArcWidth(28);
            clip.setArcHeight(28);
        });
        // padding interno overlay: 20 (già messo in VBox content)
        overlayPanel.setPadding(Insets.EMPTY);
        overlayPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlayPanel.setOnMouseClicked(e -> e.consume());

        // SCROLLABLE CONTENT
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(content);

        overlayPanel.setCenter(scrollPane);

        // COMPOSE
        outerContainer.getChildren().add(overlayPanel);
        overlay.getChildren().addAll(darkBackground, outerContainer);
        StackPane.setAlignment(outerContainer, Pos.CENTER);

        return overlay;
    }
}

class ReviewDialog extends javafx.stage.Stage {

    public ReviewDialog(int bookId, String username, Client client) {
        setTitle("Inserisci recensione");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField style = new TextField();
        style.setPromptText("Stile (1-5)");

        TextField content = new TextField();
        content.setPromptText("Contenuto (1-5)");

        TextField liking = new TextField();
        liking.setPromptText("Gradimento (1-5)");

        TextField originality = new TextField();
        originality.setPromptText("Originalità (1-5)");

        TextField edition = new TextField();
        edition.setPromptText("Edizione (1-5)");

        TextArea notes = new TextArea();
        notes.setPromptText("Note (opzionali)");

        Label error = new Label();
        error.setTextFill(Color.RED);

        Button submit = new Button("Invia recensione");
        submit.setOnAction(e -> {
            try {
                String msg = "add_book_review;" +
                        bookId + ";" +
                        username + ";" +
                        Integer.parseInt(style.getText()) + ";" +
                        Integer.parseInt(content.getText()) + ";" +
                        Integer.parseInt(liking.getText()) + ";" +
                        Integer.parseInt(originality.getText()) + ";" +
                        Integer.parseInt(edition.getText()) + ";" +
                        (notes.getText().isBlank() ? "" : notes.getText());

                String response = client.send(msg);
                if (response != null && response.contains("OK")) {
                    close();
                } else {
                    error.setText("Errore nell'invio della recensione");
                }
            } catch (Exception ex) {
                error.setText("Valori non validi (usa numeri 1-5)");
            }
        });

        root.getChildren().addAll(
                new Label("Inserisci la tua recensione"),
                style, content, liking, originality, edition, notes,
                submit, error
        );

        Scene scene = new Scene(root, 350, 450);
        setScene(scene);
    }
}
