package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.AddRmBook;
import ONA.booksrecommender.client.controller.RecommendationHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static javafx.geometry.Side.BOTTOM;

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
    private UserAreaView userArea;

    private final Client client = new Client();

    public BookDetails(int bookId, String username) {
        this.bookId = bookId;
        this.username = username;
        loadBookDetails();
    }

    public BookDetails(int bookId, String username, UserAreaView userArea) {
        this.bookId = bookId;
        this.username = username;
        this.userArea = userArea;
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
        String response = client.send("get_book_reviews;" + bookId);
        if (response == null || response.isEmpty()) return new Label("Nessuna recensione");

        String[] ratings = response.split("\\|");
        int count = 0, sum = 0;
        for (String r : ratings) {
            if (r.isBlank()) continue;
            String[] parts = r.split(";");
            if (parts.length < 8) continue;
            try {
                sum += Integer.parseInt(parts[7]);
                count++;
            } catch (NumberFormatException ignored) {
            }
        }
        if (count == 0) return new Label("Nessuna recensione");

        int fullStars = (int) Math.round((double) sum / count);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) stars.append(i < fullStars ? "★" : "☆");

        Label label = new Label(stars + " (" + count + ")");
        label.setStyle("-fx-font-size: 16px;");
        return label;
    }

    public StackPane createOverlay() {
        final StackPane overlay = new StackPane();
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgb(241, 237, 229);");
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        content.setPrefWidth(Region.USE_COMPUTED_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setOnMouseClicked(e -> e.consume());

        // HEADER
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer);

        // PULSANTI + e -
        if (username != null && !username.isEmpty()) {
            AddRmBook addRmBook = new AddRmBook();
            HBox buttonsBox = new HBox(15);
            buttonsBox.setAlignment(Pos.TOP_RIGHT);

            // --- TASTO AGGIUNGI (+) ---
            Label addButton = new Label("+");
            addButton.setStyle("-fx-font-size: 26px; -fx-cursor: hand; -fx-font-weight: bold;");
            addButton.getStyleClass().add("book-details-text");

            addButton.setOnMouseClicked(e -> {
                List<AddRmBook.LibraryInfo> libs = addRmBook.getLibrariesWithoutBook(client, username, bookId);
                if (!libs.isEmpty()) {
                    showLibraryMenu(addButton, libs, true);
                }
            });

            // --- TASTO RIMUOVI (-) ---
            Label removeButton = new Label("-");
            removeButton.setStyle("-fx-font-size: 26px; -fx-cursor: hand; -fx-font-weight: bold;");
            removeButton.getStyleClass().add("book-details-text");

            removeButton.setOnMouseClicked(e -> {
                List<AddRmBook.LibraryInfo> libs = addRmBook.getLibrariesWithBook(client, username, bookId);
                if (!libs.isEmpty()) {
                    showLibraryMenu(removeButton, libs, false);
                }
            });

            buttonsBox.getChildren().addAll(removeButton, addButton);
            header.getChildren().add(buttonsBox);
        }

        // Sezione alta con copertina e dati principali
        HBox mainContent = new HBox(20);
        VBox coverBox = new VBox();
        coverBox.setAlignment(Pos.TOP_LEFT);
        if (coverUrl != null && !coverUrl.isEmpty()) {
            try {
                Image coverImage = new Image(coverUrl, 150, 220, true, true);
                ImageView coverView = new ImageView(coverImage);
                coverBox.getChildren().add(coverView);
            } catch (Exception ignored) {
            }
        }

        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.TOP_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("book-details-text");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        titleLabel.getStyleClass().add("standard-font-fam");
        Label authorLabel = new Label(authors);
        authorLabel.getStyleClass().add("book-details-text");
        authorLabel.setStyle("-fx-font-size: 14px;");
        authorLabel.getStyleClass().add("standard-font-fam");
        Label pubYearLabel = new Label((publisher.isEmpty() ? "" : publisher) + (year.isEmpty() ? "" : " - " + year));
        pubYearLabel.getStyleClass().add("book-details-text");
        pubYearLabel.setStyle("-fx-font-size: 14px;");
        pubYearLabel.getStyleClass().add("standard-font-fam");
        Label catLabel = new Label(category);
        catLabel.getStyleClass().add("book-details-text");
        catLabel.setStyle("-fx-font-size: 14px;");
        catLabel.getStyleClass().add("standard-font-fam");
        Label ratLabel = buildRatingLabel();
        ratLabel.getStyleClass().add("book-details-text");
        ratLabel.setStyle("-fx-font-size: 14px;");
        detailsBox.getChildren().addAll(titleLabel, authorLabel, pubYearLabel, catLabel, ratLabel);
        mainContent.getChildren().addAll(coverBox, detailsBox);

        Label descTitle = new Label("Sinossi");
        descTitle.getStyleClass().add("book-details-text");
        descTitle.getStyleClass().add("standard-font-fam");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label descLabel = new Label(descrizione);
        descLabel.getStyleClass().add("book-details-text");
        descLabel.getStyleClass().add("standard-font-fam");
        descLabel.setWrapText(true);

        Separator divider = new Separator();
        divider.setPadding(new Insets(20, 0, 10, 0));
        divider.getStyleClass().add("divider");

        // Sezione di recensioni e valutazioni
        HBox reviewsHeader = new HBox();
        reviewsHeader.setAlignment(Pos.CENTER_LEFT);
        Label reviewsTitle = new Label("Valutazioni e recensioni");
        reviewsTitle.getStyleClass().add("book-details-text");
        reviewsTitle.getStyleClass().add("standard-font-fam");
        reviewsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        reviewsHeader.getChildren().addAll(reviewsTitle);

        if (username != null && !username.isEmpty()) {
            List<AddRmBook.LibraryInfo> libsWithBook = AddRmBook.getLibrariesWithBook(client, username, bookId);
            if (libsWithBook != null && !libsWithBook.isEmpty()) {
                Region reviewSpacer = new Region();
                HBox.setHgrow(reviewSpacer, Priority.ALWAYS);

                Label addReviewButton = new Label("+");
                addReviewButton.getStyleClass().add("book-details-text");
                addReviewButton.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-font-weight: bold;");
                addReviewButton.setOnMouseClicked(e -> showReviewOverlay(overlay));

                reviewsHeader.getChildren().addAll(reviewSpacer, addReviewButton);
            }
        }

        VBox reviewsSection = new VBox(20);
        reviewsSection.setAlignment(Pos.TOP_LEFT);
        String ratingsResponse = client.send("get_book_reviews;" + bookId);

        if (ratingsResponse == null || ratingsResponse.isBlank()) {
            Label noReviews = new Label("Nessuna valutazione o recensione disponibile per questo libro");
            noReviews.getStyleClass().add("book-details-text");
            noReviews.setStyle("-fx-font-size: 14px;");
            noReviews.setStyle("-fx-font-style: italic;");
            reviewsSection.getChildren().addAll(reviewsHeader, noReviews);
        } else {
            String[] ratings = ratingsResponse.split("\\|");
            int count = 0;
            double sumFinal = 0;
            int[] distribution = new int[6];
            for (String r : ratings) {
                if (r.isBlank()) continue;
                String[] p = r.split(";");
                if (p.length < 8) continue;
                try {
                    int finalVote = Integer.parseInt(p[7]);
                    distribution[finalVote]++;
                    sumFinal += finalVote;
                    count++;
                } catch (NumberFormatException ignored) {
                }
            }

            if (count == 0) {
                Label noReviews = new Label("Nessuna valutazione o recensione disponibile per questo libro");
                noReviews.getStyleClass().add("book-details-text");
                noReviews.setStyle("-fx-font-style: italic;");
                reviewsSection.getChildren().addAll(reviewsHeader, noReviews);
            } else {
                double avgFinal = sumFinal / count;
                VBox left = new VBox(5);
                left.setAlignment(Pos.CENTER);
                Label avg = new Label(String.format("%.1f", avgFinal));
                avg.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
                avg.getStyleClass().add("book-details-text");
                Label overFive = new Label(" su 5");
                overFive.setStyle("-fx-font-size: 14px;");
                overFive.getStyleClass().add("book-details-text");
                HBox avgRow = new HBox(6, avg, overFive);
                avgRow.setAlignment(Pos.BASELINE_LEFT);
                Label countLabel = new Label(count + " valutazioni");
                countLabel.setStyle("-fx-font-size: 12px;");
                countLabel.getStyleClass().add("book-details-text");
                left.getChildren().addAll(avgRow, countLabel);

                VBox bars = new VBox(3);
                bars.setAlignment(Pos.CENTER_RIGHT);
                for (int stars = 5; stars >= 1; stars--) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_RIGHT);
                    Label starLabel = new Label("★".repeat(stars));
                    starLabel.setMinWidth(60);
                    starLabel.setAlignment(Pos.CENTER_RIGHT);
                    starLabel.getStyleClass().add("book-details-text");
                    ProgressBar bar = new ProgressBar((double) distribution[stars] / count);
                    bar.setPrefWidth(400);
                    bar.setPrefHeight(6);
                    bar.getStyleClass().add("custom-progress-bar");
                    row.getChildren().addAll(starLabel, bar);
                    bars.getChildren().add(row);
                }

                HBox summary = new HBox(60, left, bars);
                summary.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(bars, Priority.ALWAYS);

                // Contenitore delle card delle recensioni in scorrimento orizzontale
                HBox horizontalCardsBox = new HBox(20);
                horizontalCardsBox.setAlignment(Pos.CENTER_LEFT);
                horizontalCardsBox.setPadding(new Insets(10, 5, 20, 5));

                // ScrollPane per lo scorrimento laterale
                ScrollPane horizontalScroll = new ScrollPane(horizontalCardsBox);

                // 1. Nascondere entrambe le scrollbar
                horizontalScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                horizontalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                horizontalScroll.setFitToHeight(true);
                horizontalScroll.setMinHeight(230);
                horizontalScroll.setPrefHeight(Region.USE_COMPUTED_SIZE);

                // Rende lo sfondo dello scroll trasparente
                horizontalScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

                final double[] dragX = new double[1];
                final double[] hValue = new double[1];

                // 3. Quando premi il mouse, salva la posizione iniziale
                horizontalScroll.setOnMousePressed(e -> {
                    dragX[0] = e.getSceneX();
                    hValue[0] = horizontalScroll.getHvalue();
                    horizontalScroll.setCursor(javafx.scene.Cursor.CLOSED_HAND);
                });

                // 4. Quando trascini, sposta lo scroll in proporzione
                horizontalScroll.setOnMouseDragged(e -> {
                    double delta = (dragX[0] - e.getSceneX()) / horizontalCardsBox.getWidth();
                    horizontalScroll.setHvalue(hValue[0] + delta);
                });

                // 5. Quando rilasci, ripristina il cursore
                horizontalScroll.setOnMouseReleased(e -> {
                    horizontalScroll.setCursor(javafx.scene.Cursor.HAND);
                });

                for (String r : ratings) {
                    if (r == null || r.isBlank()) continue;

                    String[] p = r.trim().split(";", -1);
                    if (p.length < 9) continue;

                    try {
                        String user = p[0];
                        int vote = Integer.parseInt(p[7]);
                        String rawNote = p[8].trim();
                        String noteDecoded = "";

                        if (!rawNote.isEmpty() && !rawNote.equalsIgnoreCase("EMPTY")) {
                            try {
                                byte[] decodedBytes = Base64.getMimeDecoder().decode(rawNote.replaceAll("\\s", ""));
                                noteDecoded = new String(decodedBytes, StandardCharsets.UTF_8).trim();
                            } catch (Exception e) {
                                noteDecoded = rawNote;
                            }
                        }

                        if (noteDecoded.isEmpty() || noteDecoded.equalsIgnoreCase("EMPTY")) continue;

                        // --- CARD STILE ---
                        VBox card = new VBox(8);
                        card.setMinWidth(300);
                        card.setMaxWidth(300);
                        card.setPadding(new Insets(15));
                        card.getStyleClass().add("book-details-card");

                        // Riga Superiore: User e Data
                        HBox topRow = new HBox();
                        topRow.setAlignment(Pos.CENTER_LEFT);
                        Label userLbl = new Label(user);
                        userLbl.getStyleClass().add("userLbl");

                        Region cardSpacer = new Region();
                        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

                        topRow.getChildren().addAll(userLbl, cardSpacer);

                        // Riga Stelle
                        int starsCount = Math.max(0, Math.min(5, vote));
                        Label starsLbl = new Label("★".repeat(starsCount) + "☆".repeat(5 - starsCount));
                        starsLbl.getStyleClass().add("starsLbl");

                        // Testo Recensione
                        Label noteLbl = new Label(noteDecoded);
                        noteLbl.setWrapText(true);
                        noteLbl.setStyle("-fx-text-fill: #ebebeb; -fx-font-size: 14px; -fx-line-spacing: 1.1;");
                        noteLbl.setMaxHeight(120);

                        card.getChildren().addAll(topRow, starsLbl, noteLbl);
                        horizontalCardsBox.getChildren().add(card);

                    } catch (Exception e) {
                        System.err.println("Errore rendering card: " + e.getMessage());
                    }
                }
                reviewsSection.getChildren().addAll(reviewsHeader, summary, horizontalScroll);
            }
        }


        // Sezione dei libri consigliati dagli altri utenti
        Separator adviceDivider = new Separator();
        adviceDivider.setPadding(new Insets(20, 0, 10, 0));
        adviceDivider.getStyleClass().add("divider");

        HBox adviceHeader = new HBox();
        adviceHeader.setAlignment(Pos.CENTER_LEFT);
        Label adviceTitle = new Label("Consigli dagli altri utenti");
        adviceTitle.getStyleClass().add("book-details-text");
        adviceTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        adviceHeader.getChildren().add(adviceTitle);

        if (username != null && !username.isEmpty()) {
            List<AddRmBook.LibraryInfo> libsWithBook = AddRmBook.getLibrariesWithBook(client, username, bookId);
            if (libsWithBook != null && !libsWithBook.isEmpty()) {
                Region adviceSpacer = new Region();
                HBox.setHgrow(adviceSpacer, Priority.ALWAYS);
                Label addAdviceButton = new Label("+");
                addAdviceButton.getStyleClass().add("book-details-text");
                addAdviceButton.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-font-weight: bold;");
                addAdviceButton.setOnMouseClicked(e -> RecommendationHandler.addRecommendation(bookId, username, client, overlay, this.userArea));
                adviceHeader.getChildren().addAll(adviceSpacer, addAdviceButton);
            }
        }

        // SEZIONE CONSIGLI
        VBox adviceSection = new VBox(15);
        adviceSection.getChildren().add(adviceHeader);

        String adviceResponse = client.send("get_book_advices;" + bookId);

        // Logica per gestire la risposta
        if (adviceResponse != null && !adviceResponse.isBlank() && !adviceResponse.equals("NO_RECOMMENDATIONS")) {
            FlowPane adviceGrid = new FlowPane(20, 20);
            adviceGrid.setAlignment(Pos.TOP_LEFT);

            // Usiamo un Set per evitare di mostrare lo stesso libro più volte se consigliato da più utenti
            java.util.Set<Integer> displayedIds = new java.util.HashSet<>();

            String[] recommendationBlocks = adviceResponse.split("\\|");
            for (String block : recommendationBlocks) {
                if (block == null || block.isBlank()) continue;

                String[] parts = block.split(";", -1);
                if (parts.length < 2 || parts[1] == null || parts[1].isBlank()) continue; // nessun consigliato valido

                // parts[1] contiene gli ID consigliati (es: "15451,94877,4413")
                String[] recommendedIds = parts[1].split(",");

                for (String idStr : recommendedIds) {
                    if (idStr == null) continue;
                    idStr = idStr.trim();
                    if (idStr.isEmpty()) continue;
                    if (!idStr.matches("\\d+")) continue;

                    int targetId = Integer.parseInt(idStr);

                    // Filtro: Non mostrare il libro corrente e non duplicare se già aggiunto
                    if (targetId == this.bookId || displayedIds.contains(targetId)) continue;

                    String bookData = client.send("get_book;id;" + targetId);
                    if (bookData == null || bookData.startsWith("ERROR")) continue;

                    String[] details = bookData.split(";");
                    if (details.length > 6) {
                        String coverUrl = details[6];

                        // Creazione visiva
                        StackPane coverContainer = client.createScaledCover(coverUrl, 110, 160);
                        coverContainer.setCursor(javafx.scene.Cursor.HAND);

                        coverContainer.setOnMouseClicked(e -> {
                            BookDetails nextBook = new BookDetails(targetId, username);
                            StackPane nextOverlay = nextBook.createOverlay();

                            Pane root = (Pane) overlay.getParent();
                            if (root != null) {
                                root.getChildren().add(nextOverlay);
                            }
                        });

                        adviceGrid.getChildren().add(coverContainer);
                        displayedIds.add(targetId);
                    }
                }
            }

            if (!adviceGrid.getChildren().isEmpty()) {
                adviceSection.getChildren().add(adviceGrid);
            } else {
                adviceSection.getChildren().add(new Label("Nessun altro libro consigliato."));
            }
        } else {
            Label noAdvice = new Label("Ancora nessun consiglio per questo libro.");
            noAdvice.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            adviceSection.getChildren().add(noAdvice);
        }

        content.getChildren().addAll(header, mainContent, descTitle, descLabel, divider, reviewsSection, adviceDivider, adviceSection);

        // FINAL OVERLAY SETUP
        Region darkBackground = new Region();
        darkBackground.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        darkBackground.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane outerContainer = new StackPane();
        outerContainer.setPadding(new Insets(50));
        outerContainer.setStyle("-fx-background-color: transparent;");
        outerContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == outerContainer) {
                Pane parent = (Pane) overlay.getParent();
                if (parent != null) parent.getChildren().remove(overlay);
            }
        });

        BorderPane overlayPanel = new BorderPane();
        overlayPanel.setStyle("-fx-background-color: white; -fx-background-radius: 14 14 0 0; -fx-border-radius: 14 14 0 0;");
        Rectangle clip = new Rectangle();
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        overlayPanel.setClip(clip);
        overlayPanel.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            clip.setWidth(newB.getWidth());
            clip.setHeight(newB.getHeight());
        });

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        overlayPanel.setCenter(scrollPane);

        outerContainer.getChildren().add(overlayPanel);
        overlay.getChildren().addAll(darkBackground, outerContainer);
        return overlay;
    }

    // Visualizzazione del menu per aggiungere o rimuovere il libro da una libreria
    private void showLibraryMenu(javafx.scene.Node anchor, List<AddRmBook.LibraryInfo> libraries, boolean isAdd) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("library-context-menu");

        for (AddRmBook.LibraryInfo lib : libraries) {
            MenuItem item = new MenuItem(lib.getName());
            item.getStyleClass().add("library-context-item");

            item.setOnAction(ev -> {
                String cmd = isAdd ? "add_book_to_library;" : "remove_book_from_library;";
                client.send(cmd + lib.getId() + ";" + bookId);
            });

            contextMenu.getItems().add(item);
        }

        contextMenu.setOnShowing(e -> {
            contextMenu.getScene().setFill(Color.TRANSPARENT);
            contextMenu.getScene().getRoot().setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-padding: 1;" +
                            "-fx-background-insets: 0;" +
                            "-fx-border-insets: 0;"
            );
        });

        // 1. Calcolare la larghezza del menu per poterlo allineare a sinistra del tasto
        double menuWidth = contextMenu.prefWidth(-1);
        if (menuWidth <= 0) menuWidth = 150; // Fallback se non ancora calcolato

        // 2. Calcolare l'offset:
        // Obiettivo: il lato destro del menu deve coincidere con il lato destro del tasto
        // Offset = (Larghezza Tasto) - (Larghezza Menu)
        double anchorWidth = anchor.getBoundsInLocal().getWidth();
        double xOffset = anchorWidth - menuWidth;

        // 3. Mostrare il menu con l'offset calcolato
        contextMenu.show(anchor, BOTTOM, xOffset, 8);
    }

    // Popup per l’inserimento di una nuova recensione
    private void showReviewOverlay(StackPane parentOverlay) {
        StackPane reviewOverlay = new StackPane();
        reviewOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");

        VBox container = new VBox(15);
        container.setMaxSize(640, 360); // Leggermente più largo per la colonna sinistra
        container.setPadding(new Insets(20, 25, 20, 25));
        container.setStyle("-fx-background-color: rgb(241, 237, 229); -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        container.setAlignment(Pos.TOP_CENTER);
        container.setOnMouseClicked(e -> e.consume());

        Label title = new Label("Valuta questo libro");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1c1c1e;");

        HBox columnsBox = new HBox(5);
        columnsBox.setAlignment(Pos.CENTER);


        GridPane ratingsGrid = new GridPane();
        ratingsGrid.setHgap(15);
        ratingsGrid.setVgap(12);
        ratingsGrid.setAlignment(Pos.TOP_LEFT);

        ratingsGrid.setMinWidth(175);
        ratingsGrid.setPrefWidth(175);

        String[] categories = {"Stile", "Contenuto", "Gradimento", "Originalità", "Edizione"};
        TextField[] inputs = new TextField[5];

        for (int i = 0; i < categories.length; i++) {
            Label lbl = new Label(categories[i]);
            lbl.setMinWidth(90);
            lbl.setAlignment(Pos.BOTTOM_LEFT);
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1c1c1e; -fx-font-weight: 500;");

            TextField tf = new TextField();
            tf.setPromptText("-");
            tf.setPrefSize(38, 38);
            tf.setMinSize(38, 38);
            tf.setAlignment(Pos.CENTER);
            tf.setStyle("-fx-background-color: white; -fx-text-fill: #1c1c1e; " +
                    "-fx-background-radius: 8; -fx-border-color: #1c1c1e; " +
                    "-fx-border-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label outOfFive = new Label("/5");
            outOfFive.setStyle("-fx-font-size: 10px; -fx-text-fill: #8e8e93;");

            HBox inputWrapper = new HBox(4, tf, outOfFive);
            inputWrapper.setAlignment(Pos.BOTTOM_LEFT);

            outOfFive.setPadding(new Insets(0, 0, 4, 0));

            tf.textProperty().addListener((obs, oldV, newV) -> {
                if (!newV.matches("[1-5]?")) tf.setText(oldV);
                if (newV.length() == 1) {
                    int next = -1;
                    for(int j=0; j<5; j++) if(inputs[j] == tf) next = j+1;
                    if(next < 5) inputs[next].requestFocus();
                }
            });

            inputs[i] = tf;
            ratingsGrid.add(lbl, 0, i);
            ratingsGrid.add(inputWrapper, 1, i);
        }


        VBox notesBox = new VBox(5);
        HBox.setHgrow(notesBox, Priority.ALWAYS);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Raccontaci la tua opinione (opzionale)...");
        notesArea.setWrapText(true);

        notesArea.setPrefHeight(238);
        notesArea.setMinHeight(238);
        notesArea.setMaxHeight(238);
        notesArea.setStyle("-fx-control-inner-background: white; -fx-background-color: white; " +
                "-fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #1c1c1e; -fx-padding: 10; -fx-font-size: 13px;");

        Label charCount = new Label("0 / 190");
        charCount.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        notesArea.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 190) notesArea.setText(oldV);
            charCount.setText(notesArea.getText().length() + " / 190");
        });

        notesBox.getChildren().addAll(notesArea, charCount);
        columnsBox.getChildren().setAll(ratingsGrid, notesBox);


        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annulla");
        cancelBtn.setCursor(javafx.scene.Cursor.HAND);
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-font-size: 14px;");
        cancelBtn.setOnAction(ev -> parentOverlay.getChildren().remove(reviewOverlay));

        Button submitBtn = new Button("Invia");
        submitBtn.setCursor(javafx.scene.Cursor.HAND);
        submitBtn.setPrefWidth(130);
        submitBtn.setStyle("-fx-background-color: #1c1c1e; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-padding: 10 0; -fx-font-size: 14px;");

        submitBtn.setOnAction(ev -> {
            try {
                for (TextField tf : inputs) if (tf.getText().trim().isEmpty()) return;
                String noteRaw = notesArea.getText().trim();
                String noteEncoded = (noteRaw.isEmpty()) ? "EMPTY" :
                        Base64.getEncoder().encodeToString(noteRaw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                String msg = String.format("add_book_review;%d;%s;%s;%s;%s;%s;%s;%s",
                        bookId, username, inputs[0].getText(), inputs[1].getText(),
                        inputs[2].getText(), inputs[3].getText(), inputs[4].getText(), noteEncoded);
                if (client.send(msg).contains("OK")) {
                    parentOverlay.getChildren().remove(reviewOverlay);
                    Platform.runLater(() -> {
                        Pane p = (Pane) parentOverlay.getParent();
                        p.getChildren().remove(parentOverlay);
                        p.getChildren().add(new BookDetails(bookId, username).createOverlay());
                    });
                }
            } catch (Exception ignored) {}
        });

        actions.getChildren().addAll(cancelBtn, submitBtn);
        container.getChildren().addAll(title, columnsBox, actions);
        reviewOverlay.getChildren().add(container);

        reviewOverlay.setOnMousePressed(e -> {
            if (e.getTarget() == reviewOverlay) parentOverlay.getChildren().remove(reviewOverlay);
        });

        parentOverlay.getChildren().add(reviewOverlay);
        reviewOverlay.toFront();
        Platform.runLater(() -> inputs[0].requestFocus());
    }
}