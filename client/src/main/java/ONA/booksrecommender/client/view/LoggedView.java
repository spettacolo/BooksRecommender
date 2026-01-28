package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.controller.AddRmBook;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ONA.booksrecommender.client.Client;
import javafx.scene.paint.Color;

public class LoggedView extends VBox {

    private String username;

    public LoggedView(RootView root, String username) {
        this.username = username;
        this.setSpacing(10);
        this.setPadding(new Insets(10));

        VBox.setVgrow(this, Priority.ALWAYS);

        VBox topContent = new VBox(5);

        Label home = new Label("Home");
        home.setOnMouseClicked(e -> root.showHome());
        home.getStyleClass().add("default-label");

        Label yourLibraries = new Label("Le tue librerie");
        yourLibraries.getStyleClass().add("your-libraries-label");
        yourLibraries.setStyle("-fx-padding: 10 0 5 0; -fx-font-weight: bold;");

        topContent.getChildren().addAll(home, yourLibraries);
        this.getChildren().add(topContent);

        loadLibraries(root, topContent);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox userBtn = createUserBox(root);

        this.getChildren().addAll(spacer, userBtn);
    }

    private HBox createUserBox(RootView root) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setCursor(javafx.scene.Cursor.HAND);

        box.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 15;");

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #48484a; -fx-background-radius: 15;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #3a3a3c; -fx-background-radius: 15;"));
        box.setOnMouseClicked(e -> root.showUserArea(username));

        StackPane avatar = new StackPane();
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(16, Color.web("#8e8e93"));
        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        avatar.getChildren().addAll(circle, userIcon);

        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 500; -fx-font-size: 14px;");

        box.getChildren().addAll(avatar, nameLabel);
        VBox.setMargin(box, new Insets(10, 0, 5, 0));
        return box;
    }

    private void loadLibraries(RootView root, VBox topContent) {
        Client client = root.getClient();
        String risposta = client.send("get_user_libraries;" + username);

        if (risposta != null && !risposta.isBlank() && !risposta.contains("ERROR") && !risposta.equals("NO_LIBRARIES")) {
            String[] libs = risposta.split(",");

            for (String lib : libs) {
                String dati = client.send("get_user_library;id;" + lib);
                if (dati == null || dati.isEmpty() || dati.startsWith("ERROR")) continue;

                HBox libRow = new HBox(10);
                libRow.setAlignment(Pos.CENTER_LEFT);
                libRow.setPadding(new Insets(5, 10, 5, 20)); // Padding ridotto per far stare la X

                Label libLabel = new Label((dati.trim()).split(";")[1]);
                libLabel.getStyleClass().add("default-label");
                libLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(libLabel, Priority.ALWAYS);

                libLabel.setOnMouseClicked(e -> root.showLibrary(lib));

                // --- TASTO ELIMINA / ANNULLA ---
                StackPane actionBtn = new StackPane();
                actionBtn.setVisible(false);
                actionBtn.setPadding(new Insets(4, 8, 4, 8));
                actionBtn.setCursor(javafx.scene.Cursor.HAND);

                Label btnText = new Label("✕");
                btnText.setStyle("-fx-text-fill: #ff3b30; -fx-font-size: 11px; -fx-font-weight: bold;");
                actionBtn.setStyle("-fx-background-color: rgba(255, 59, 48, 0.1); -fx-background-radius: 8;");
                actionBtn.getChildren().add(btnText);

                // COUNTDOWN
                javafx.animation.Timeline countdown = new javafx.animation.Timeline();
                final int[] secondsLeft = {5};

                countdown.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    secondsLeft[0]--;
                    if (secondsLeft[0] > 0) {
                        btnText.setText("Annulla (" + secondsLeft[0] + ")");
                    } else {
                        countdown.stop();
                        client.send("remove_library;" + lib);
                        root.showLoggedSidebar(username);
                    }
                }));
                countdown.setCycleCount(5);

                actionBtn.setOnMouseClicked(e -> {
                    if (countdown.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                        countdown.stop();
                        secondsLeft[0] = 5;
                        btnText.setText("✕");
                        actionBtn.setVisible(false);
                    } else {
                        btnText.setText("Annulla (5)");
                        countdown.play();
                    }
                    e.consume();
                });

                // TASTO DESTRO -> MOSTRA X
                libLabel.setOnContextMenuRequested(e -> actionBtn.setVisible(true));

                libRow.getChildren().addAll(libLabel, actionBtn);
                topContent.getChildren().add(libRow);
            }
        }

        // --- TASTO NUOVA LIBRERIA ---
        Label newLib = new Label("+ Nuova libreria");
        newLib.getStyleClass().add("default-label");
        newLib.setStyle("-fx-padding: 10 20 5 20;");

        // CHIAMA IL NUOVO OVERLAY INVECE DEL DIALOG
        newLib.setOnMouseClicked(e -> showAddLibraryOverlay(root));

        topContent.getChildren().add(newLib);
    }

    private void showAddLibraryOverlay(RootView root) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.2);");

        BorderPane overlayPanel = new BorderPane();
        overlayPanel.getStyleClass().add("reglog-overlay-panel");

        overlayPanel.setStyle(
                "-fx-background-color: #f1ede5; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 20, 0, 0, 10);"
        );

        overlayPanel.setMaxWidth(335);
        overlayPanel.setMaxHeight(215);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                root.getMainContentContainer().getChildren().remove(overlay);
            }
        });

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        Label title = new Label("Nuova Libreria");
        title.getStyleClass().add("reglog-title");

        TextField libraryNameField = new TextField();
        libraryNameField.getStyleClass().add("reglog-field");
        libraryNameField.setPromptText("Nome della libreria...");

        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #E21A1A; -fx-font-size: 11px;");

        // Bottoni
        Button createButton = new Button("Crea");
        createButton.getStyleClass().add("reglog-primary-button");
        createButton.setDefaultButton(true);

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("reglog-secondary-button");

        HBox buttons = new HBox(12, cancelButton, createButton);
        buttons.setAlignment(Pos.CENTER);

        // Azioni
        cancelButton.setOnAction(e -> root.getMainContentContainer().getChildren().remove(overlay));
        createButton.setOnAction(e -> {
            String name = libraryNameField.getText().trim();
            if (name.isEmpty()) {
                feedbackLabel.setText("Inserisci un nome!");
                return;
            }
            if (root.getClient().addLibrary(name, username)) {
                root.showLoggedSidebar(username);
                root.getMainContentContainer().getChildren().remove(overlay);
            } else {
                feedbackLabel.setText("Errore durante la creazione.");
            }
        });

        content.getChildren().addAll(title, libraryNameField, feedbackLabel, buttons);
        overlayPanel.setCenter(content);

        overlay.getChildren().add(overlayPanel);
        root.getMainContentContainer().getChildren().add(overlay);

        libraryNameField.requestFocus();
    }
}