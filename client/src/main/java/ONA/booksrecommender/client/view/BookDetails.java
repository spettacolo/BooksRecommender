package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.controller.AddRmBook;
import ONA.booksrecommender.client.controller.RecommendationHandler;
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
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgb(241, 237, 229);");
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        content.setPrefWidth(Region.USE_COMPUTED_SIZE);
        content.setPrefHeight(Region.USE_COMPUTED_SIZE);
        content.setOnMouseClicked(e -> e.consume());

        // HEADER
        Label closeButton = new Label("✕");
        closeButton.setStyle("-fx-font-size: 18px;");
        closeButton.getStyleClass().add("book-details-text");
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

        // CONTENUTO PRINCIPALE
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
        titleLabel.getStyleClass().add("book-details-text");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label authorLabel = new Label(authors);
        authorLabel.getStyleClass().add("book-details-text");
        authorLabel.setStyle("-fx-font-size: 14px;");
        Label pubYearLabel = new Label((publisher.isEmpty() ? "" : publisher) + (year.isEmpty() ? "" : " - " + year));
        pubYearLabel.getStyleClass().add("book-details-text");
        pubYearLabel.setStyle("-fx-font-size: 14px;");
        Label catLabel = new Label(category);
        catLabel.getStyleClass().add("book-details-text");
        catLabel.setStyle("-fx-font-size: 14px;");
        Label ratLabel = buildRatingLabel();
        ratLabel.getStyleClass().add("book-details-text");
        detailsBox.getChildren().addAll(titleLabel, authorLabel, pubYearLabel, catLabel, ratLabel);
        mainContent.getChildren().addAll(coverBox, detailsBox);

        Label descTitle = new Label("Sinossi");
        descTitle.getStyleClass().add("book-details-text");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label descLabel = new Label(descrizione);
        descLabel.getStyleClass().add("book-details-text");
        descLabel.setWrapText(true);

        Separator divider = new Separator();
        divider.setPadding(new Insets(20, 0, 10, 0));
        divider.getStyleClass().add("divider");

        // RECENSIONI SECTION
        HBox reviewsHeader = new HBox();
        reviewsHeader.setAlignment(Pos.CENTER_LEFT);
        Label reviewsTitle = new Label("Valutazioni e recensioni");
        reviewsTitle.getStyleClass().add("book-details-text");
        reviewsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        reviewsHeader.getChildren().addAll(reviewsTitle);

        if (username != null && !username.isEmpty()) {
            List<AddRmBook.LibraryInfo> libsWithBook = AddRmBook.getLibrariesWithBook(client, username, bookId);
            if (libsWithBook != null && !libsWithBook.isEmpty()) {
                // Spacer per spingere il tasto + tutto a destra
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
        String ratingsResponse = client.send("get_book_ratings;" + bookId);

        if (ratingsResponse == null || ratingsResponse.isBlank()) {
            Label noReviews = new Label("Nessuna valutazione o recensione disponibile per questo libro");
            noReviews.getStyleClass().add("book-details-text");
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

                /// Sostituiamo la FlowPane con un contenitore orizzontale
                HBox horizontalCardsBox = new HBox(20);
                horizontalCardsBox.setAlignment(Pos.CENTER_LEFT);
                horizontalCardsBox.setPadding(new Insets(10, 5, 20, 5));

                // ScrollPane per lo scorrimento laterale
                ScrollPane horizontalScroll = new ScrollPane(horizontalCardsBox);

                // 1. Nascondiamo entrambe le scrollbar
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

                        // --- CARD STILE APPLE STORE ---
                        VBox card = new VBox(8);
                        card.setMinWidth(300);
                        card.setMaxWidth(300);
                        card.setPadding(new Insets(15));
                        card.setStyle("-fx-background-color: #2c2c2e; -fx-background-radius: 15;");

                        // Riga Superiore: User e Data
                        HBox topRow = new HBox();
                        topRow.setAlignment(Pos.CENTER_LEFT);
                        Label userLbl = new Label(user);
                        userLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

                        Region cardSpacer = new Region();
                        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

                        topRow.getChildren().addAll(userLbl, cardSpacer);

                        // Riga Stelle
                        int starsCount = Math.max(0, Math.min(5, vote));
                        Label starsLbl = new Label("★".repeat(starsCount) + "☆".repeat(5 - starsCount));
                        starsLbl.setStyle("-fx-text-fill: #ff9500; -fx-font-size: 14px;");

                        // Testo Recensione
                        Label noteLbl = new Label(noteDecoded);
                        noteLbl.setWrapText(true);
                        noteLbl.setStyle("-fx-text-fill: #ebebeb; -fx-font-size: 14px; -fx-line-spacing: 1.1;");
                        // Impedisce al testo di sparire se troppo lungo
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


        // CONSIGLI
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
                Region adviceSpacer = new Region(); HBox.setHgrow(adviceSpacer, Priority.ALWAYS);
                Label addAdviceButton = new Label("+");
                addAdviceButton.getStyleClass().add("book-details-text");
                addAdviceButton.setStyle("-fx-font-size: 22px; -fx-cursor: hand; -fx-font-weight: bold;");
                addAdviceButton.setOnMouseClicked(e -> RecommendationHandler.addRecommendation(bookId, username, client, overlay));
                adviceHeader.getChildren().addAll(adviceSpacer, addAdviceButton);
            }
        }

        // CONSIGLI SECTION
        VBox adviceSection = new VBox(15);
        adviceSection.getChildren().add(adviceHeader);

        String adviceResponse = client.send("get_book_advices;" + bookId);

// Logica per gestire la risposta: 80633;15451,94877,4413|80633;49032,41754|
        if (adviceResponse != null && !adviceResponse.isBlank() && !adviceResponse.equals("NO_RECOMMENDATIONS")) {
            FlowPane adviceGrid = new FlowPane(20, 20);
            adviceGrid.setAlignment(Pos.TOP_LEFT);

            // Usiamo un Set per evitare di mostrare lo stesso libro più volte se consigliato da più utenti
            java.util.Set<Integer> displayedIds = new java.util.HashSet<>();

            String[] recommendationBlocks = adviceResponse.split("\\|");
            for (String block : recommendationBlocks) {
                if (block.isBlank()) continue;

                String[] parts = block.split(";");
                if (parts.length < 2) continue; // Manca la lista dei consigliati

                // parts[1] contiene gli ID consigliati (es: "15451,94877,4413")
                String[] recommendedIds = parts[1].split(",");

                for (String idStr : recommendedIds) {
                    int targetId = Integer.parseInt(idStr.trim());

                    // Filtro: Non mostrare il libro corrente e non duplicare se già aggiunto
                    if (targetId == this.bookId || displayedIds.contains(targetId)) continue;

                    // Recuperiamo i dettagli dal server per questo ID specifico
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
                            if (root != null) root.getChildren().add(nextOverlay);
                        });

                        adviceGrid.getChildren().add(coverContainer);
                        displayedIds.add(targetId); // Segna come visualizzato
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
        Rectangle clip = new Rectangle(); clip.setArcWidth(28); clip.setArcHeight(28);
        overlayPanel.setClip(clip);
        overlayPanel.layoutBoundsProperty().addListener((obs, oldB, newB) -> { clip.setWidth(newB.getWidth()); clip.setHeight(newB.getHeight()); });

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

    private void showLibraryMenu(javafx.scene.Node anchor, List<AddRmBook.LibraryInfo> libraries, boolean isAdd) {
        ContextMenu contextMenu = new ContextMenu();
        // Stile moderno: sfondo chiaro, angoli arrotondati e ombra
        contextMenu.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ddd;");

        for (AddRmBook.LibraryInfo lib : libraries) {
            MenuItem item = new MenuItem(lib.getName());
            item.setStyle("-fx-font-size: 14px; -fx-padding: 5 15 5 15;");

            item.setOnAction(ev -> {
                String cmd = isAdd ? "add_book_to_library;" : "remove_book_from_library;";
                client.send(cmd + lib.getId() + ";" + bookId);
                // Opzionale: qui puoi aggiungere un feedback visivo o ricaricare i dati
            });

            contextMenu.getItems().add(item);
        }

        // Mostra il menu esattamente sotto il tasto cliccato
        contextMenu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showReviewOverlay(StackPane parentOverlay) {
        StackPane reviewOverlay = new StackPane();
        reviewOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        // Pannello centrale più largo per ospitare le due colonne
        VBox container = new VBox(20);
        container.setMaxSize(650, 450);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: rgb(241, 237, 229); -fx-background-radius: 20;");
        container.setAlignment(Pos.TOP_CENTER);

        // Titolo
        Label title = new Label("La tua valutazione");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c2c2e;");

        // Layout a due colonne
        HBox columnsBox = new HBox(30);
        columnsBox.setAlignment(Pos.CENTER);

        // --- COLONNA SINISTRA: VOTI ---
        GridPane ratingsGrid = new GridPane();
        ratingsGrid.setHgap(15);
        ratingsGrid.setVgap(12);
        ratingsGrid.setAlignment(Pos.TOP_LEFT);

        String[] categories = {"Stile", "Contenuto", "Gradimento", "Originalità", "Edizione"};
        TextField[] inputs = new TextField[5];

        for (int i = 0; i < categories.length; i++) {
            Label lbl = new Label(categories[i]);
            lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #444; -fx-font-weight: 500;");
            TextField tf = new TextField();
            tf.setPromptText("1-5");
            tf.setPrefWidth(55);
            tf.setAlignment(Pos.CENTER);
            // Stile input simile a quello dell'app
            tf.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ccc; -fx-padding: 5;");

            tf.textProperty().addListener((obs, oldV, newV) -> {
                if (!newV.matches("[1-5]?")) tf.setText(oldV);
            });
            inputs[i] = tf;

            ratingsGrid.add(lbl, 0, i);
            ratingsGrid.add(tf, 1, i);
        }

        // --- COLONNA DESTRA: NOTE ---
        VBox notesBox = new VBox(8);
        HBox.setHgrow(notesBox, Priority.ALWAYS);

        Label noteHeader = new Label("Raccontaci cosa ne pensi (opzionale):");
        noteHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #444;");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Scrivi qui il tuo commento...");
        notesArea.setWrapText(true);
        notesArea.setPrefHeight(220); // Più alta ora che è a lato
        notesArea.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #ccc; -fx-padding: 8;");

        Label charCount = new Label("0 / 175");
        charCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        notesArea.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 175) notesArea.setText(oldV);
            charCount.setText(notesArea.getText().length() + " / 175");
        });

        notesBox.getChildren().addAll(noteHeader, notesArea, charCount);

        // Aggiunta delle due colonne alla HBox principale
        columnsBox.getChildren().addAll(ratingsGrid, notesBox);

        // Messaggio di errore
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setStyle("-fx-font-size: 12px;");

        // --- PULSANTIERA ---
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annulla");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-cursor: hand; -fx-font-weight: bold;");
        cancelBtn.setOnAction(ev -> parentOverlay.getChildren().remove(reviewOverlay));

        Button submitBtn = new Button("Pubblica Recensione");
        submitBtn.setPrefWidth(200);
        submitBtn.setStyle("-fx-background-color: #2c2c2e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");

        submitBtn.setOnAction(ev -> {
            try {
                for (TextField tf : inputs) if (tf.getText().isEmpty()) throw new Exception("Inserisci tutti i voti obbligatori");

                String encodedNote = Base64.getEncoder().encodeToString(notesArea.getText().getBytes(StandardCharsets.UTF_8));
                String msg = String.format("add_book_review;%d;%s;%s;%s;%s;%s;%s;%s",
                        bookId, username, inputs[0].getText(), inputs[1].getText(),
                        inputs[2].getText(), inputs[3].getText(), inputs[4].getText(), encodedNote);

                if (client.send(msg).contains("OK")) {
                    parentOverlay.getChildren().remove(reviewOverlay);
                    // Suggerimento: qui potresti ricaricare i dettagli o mostrare un feedback
                } else {
                    errorLabel.setText("Errore di connessione al server.");
                }
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        actions.getChildren().addAll(cancelBtn, submitBtn);

        // Composizione finale
        container.getChildren().addAll(title, columnsBox, errorLabel, actions);
        reviewOverlay.getChildren().add(container);

        // Chiudi al click fuori dal pannello
        reviewOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == reviewOverlay) parentOverlay.getChildren().remove(reviewOverlay);
        });
        container.setOnMouseClicked(e -> e.consume());

        parentOverlay.getChildren().add(reviewOverlay);
    }
}