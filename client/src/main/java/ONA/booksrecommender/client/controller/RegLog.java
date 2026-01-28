package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.view.RootView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Region;

public class RegLog {

    private StackPane overlay = null;
    private BorderPane overlayPanel = null;
    private RootView root;
    private Client client;

    public void createOverlay(Pane rootPane, Client client) {
        if (!(rootPane instanceof RootView rv)) return;
        this.root = rv;
        this.client = client;

        // Controlla se esiste già un overlay nel main container
        boolean exists = root.getMainContentContainer().getChildren().stream()
                .anyMatch(n -> n.getStyleClass().contains("reglog-overlay"));
        if (exists) return;

        overlay = new StackPane();
        overlay.getStyleClass().add("reglog-overlay");
        overlay.setPickOnBounds(true);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        overlay.prefWidthProperty().bind(root.getMainContentContainer().widthProperty());
        overlay.prefHeightProperty().bind(root.getMainContentContainer().heightProperty());

        Region darkBackground = new Region();
        darkBackground.getStyleClass().add("reglog-dark-bg");
        darkBackground.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        darkBackground.prefWidthProperty().bind(root.getMainContentContainer().widthProperty());
        darkBackground.prefHeightProperty().bind(root.getMainContentContainer().heightProperty());
        darkBackground.setPickOnBounds(true);
        darkBackground.setOnMouseClicked(e -> root.getMainContentContainer().getChildren().remove(overlay));

        overlayPanel = new BorderPane();
        overlayPanel.getStyleClass().add("reglog-panel");
        overlayPanel.setPrefWidth(420);
        overlayPanel.setMinWidth(420);
        overlayPanel.setMaxWidth(420);
        overlayPanel.setOnMouseClicked(e -> e.consume());

        Rectangle clip = new Rectangle();
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        overlayPanel.setClip(clip);
        overlayPanel.layoutBoundsProperty().addListener((obs,o,n)->{
            clip.setWidth(n.getWidth());
            clip.setHeight(n.getHeight());
        });

        overlay.getChildren().addAll(darkBackground, overlayPanel);
        StackPane.setAlignment(overlayPanel, Pos.CENTER);

        root.getMainContentContainer().getChildren().add(overlay);

        showLoginFormInOverlay();
    }

    public void showLoginFormInOverlay() {
        if (overlay == null) return;
        VBox content = new VBox(6);
        content.getStyleClass().add("reglog-form");
        content.setMaxWidth(380);
        content.setSpacing(8);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Accedi al tuo account");
        title.getStyleClass().add("reglog-title");
        Region titleSpacer = new Region();
        titleSpacer.setPrefHeight(12);

        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("reglog-field");
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("reglog-field");
        passwordField.setPromptText("Password");
        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #E21A1A;");

        Button loginButton = new Button("Accedi");
        loginButton.getStyleClass().add("reglog-primary-button");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                feedbackLabel.setText("Compila tutti i campi");
                return;
            }
            if (checkLogin(client, u, p)) {
                root.setUsername(u);
                root.showLoggedSidebar(u);
                root.showHome();
                root.getMainContentContainer().getChildren().remove(overlay);
            } else feedbackLabel.setText("Credenziali non valide.");
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("reglog-secondary-button");
        cancelButton.setOnAction(e -> root.getMainContentContainer().getChildren().remove(overlay));
        HBox buttons = new HBox(10, cancelButton, loginButton);
        buttons.setAlignment(Pos.CENTER);

        Hyperlink registerLink = new Hyperlink("Non hai un account? Registrati qui");
        registerLink.getStyleClass().add("reglog-link");
        registerLink.setOnAction(e -> showSignUpFormInOverlay());

        content.getChildren().setAll(
            title,
            titleSpacer,
            usernameField,
            passwordField,
            feedbackLabel,
            buttons,
            registerLink
        );

        overlayPanel.setPrefHeight(290);
        overlayPanel.setMinHeight(290);
        overlayPanel.setMaxHeight(290);
        overlayPanel.setCenter(content);
    }

    public void showSignUpFormInOverlay() {
        if (overlay == null) return;
        VBox content = new VBox(6);
        content.getStyleClass().add("reglog-form");
        content.setMaxWidth(380);
        content.setSpacing(8);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Crea un nuovo account");
        title.getStyleClass().add("reglog-title");
        Region titleSpacer = new Region();
        titleSpacer.setPrefHeight(12);

        TextField nomeField = new TextField(); nomeField.setPromptText("Nome");
        nomeField.getStyleClass().add("reglog-field");
        TextField cognomeField = new TextField(); cognomeField.setPromptText("Cognome");
        cognomeField.getStyleClass().add("reglog-field");
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("reglog-field");
        TextField cfField = new TextField(); cfField.setPromptText("Codice Fiscale");
        cfField.getStyleClass().add("reglog-field");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        emailField.getStyleClass().add("reglog-field");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("reglog-field");
        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #E21A1A;");

        Button registerButton = new Button("Registrati");
        registerButton.getStyleClass().add("reglog-primary-button");
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            String username = usernameField.getText().trim();
            String cf = cfField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() || cf.isEmpty() || email.isEmpty() || password.isEmpty()) {
                feedbackLabel.setText("Compila tutti i campi");
                return;
            }
            if (!signUp(client, username, nome, cognome, cf, email, password)) {
                feedbackLabel.setText("Username già esistente");
                return;
            }
            root.setUsername(username);
            root.showLoggedSidebar(username);
            root.showHome();
            root.getMainContentContainer().getChildren().remove(overlay);
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("reglog-secondary-button");
        cancelButton.setOnAction(e -> root.getMainContentContainer().getChildren().remove(overlay));
        HBox buttons = new HBox(10, cancelButton, registerButton);
        buttons.setAlignment(Pos.CENTER);

        Hyperlink loginLink = new Hyperlink("Hai già un account? Accedi qui");
        loginLink.getStyleClass().add("reglog-link");
        loginLink.setOnAction(e -> showLoginFormInOverlay());

        content.getChildren().setAll(
            title,
            titleSpacer,
            nomeField,
            cognomeField,
            usernameField,
            cfField,
            emailField,
            passwordField,
            feedbackLabel,
            buttons,
            loginLink
        );

        overlayPanel.setPrefHeight(460);
        overlayPanel.setMinHeight(460);
        overlayPanel.setMaxHeight(460);
        overlayPanel.setCenter(content);
    }

    private boolean checkLogin(Client client, String username, String password) {
        String risposta = client.send("login;" + username + ";" + password);
        if (risposta == null || !risposta.contains(";")) return false;
        String[] parts = risposta.split(";");
        return parts.length >= 2 && parts[1].trim().equals("0");
    }

    private boolean signUp(Client client, String username, String name, String surname, String taxId, String email, String password) {
        String comando = "sign_up;" + username + ";" + name + ";" + surname + ";" + taxId + ";" + email + ";" + password;
        String risposta = client.send(comando);
        if (risposta == null || !risposta.contains(";")) return false;
        String[] parts = risposta.split(";");
        return parts.length >= 2 && parts[1].trim().equals("OK");
    }
}