package ONA.booksrecommender.client.controller;


import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.view.RootView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RegLog {

    private Pane activeOverlay = null;
    private Stage popupStage = null;
    private VBox popupContent = null;
    private RootView root;
    private Client client;

    public void showLoginForm(Pane rootPane, Client client) {
        if (activeOverlay != null) return;

        this.root = (RootView) rootPane;
        this.client = client;

        if (popupStage == null) {
            popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(root.getScene().getWindow());
            popupStage.setTitle("Login");

            popupContent = new VBox(15);
            popupContent.setPadding(new Insets(20));
            popupContent.setAlignment(Pos.CENTER);

            Scene scene = new Scene(popupContent);
            popupStage.setScene(scene);
        }

        popupContent.getChildren().clear();

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label feedbackLabel = new Label();

        Button loginButton = new Button("Accedi");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                feedbackLabel.setText("Compila tutti i campi!");
                return;
            }

            if (checkLogin(client, username, password)) {
                root.setUsername(username);
                popupStage.close();
                root.showLoggedSidebar(username);
                root.showHome();
                activeOverlay = null;
            } else feedbackLabel.setText("Credenziali non valide.");
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.setOnAction(e -> {
            popupStage.close();
            activeOverlay = null;
        });

        HBox buttons = new HBox(10, loginButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        Hyperlink registerLink = new Hyperlink("Non hai un account? Registrati qui");
        registerLink.setOnAction(e -> switchToSignUpForm());

        popupContent.getChildren().addAll(
                new Label("Accedi al tuo account"),
                usernameField, passwordField,
                feedbackLabel, buttons,
                registerLink
        );

        activeOverlay = popupContent;
        popupStage.sizeToScene();
        popupStage.showAndWait();
        activeOverlay = null;
    }

    public void showSignUpForm(Pane rootPane, Client client) {
        if (activeOverlay != null) return;

        this.root = (RootView) rootPane;
        this.client = client;

        if (popupStage == null) {
            popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(root.getScene().getWindow());
            popupStage.setTitle("Registrazione");

            popupContent = new VBox(10);
            popupContent.setPadding(new Insets(20));
            popupContent.setAlignment(Pos.TOP_LEFT);

            Scene scene = new Scene(popupContent);
            popupStage.setScene(scene);
        }

        popupContent.getChildren().clear();

        TextField nomeField = new TextField(); nomeField.setPromptText("Nome"); nomeField.setMinWidth(200);
        TextField cognomeField = new TextField(); cognomeField.setPromptText("Cognome"); cognomeField.setMinWidth(200);
        TextField usernameField = new TextField(); usernameField.setPromptText("Username"); usernameField.setMinWidth(200);
        TextField cfField = new TextField(); cfField.setPromptText("Codice Fiscale"); cfField.setMinWidth(200);
        TextField emailField = new TextField(); emailField.setPromptText("Email"); emailField.setMinWidth(200);
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password"); passwordField.setMinWidth(200);

        Label feedbackLabel = new Label();

        Button registerButton = new Button("Registrati");
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            String username = usernameField.getText().trim();
            String cf = cfField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() ||
                    cf.isEmpty() || email.isEmpty() || password.isEmpty()) {
                feedbackLabel.setText("Compila tutti i campi!");
                return;
            }

            if (!signUp(client, username, nome, cognome, cf, email, password)) {
                feedbackLabel.setText("Username già esistente!");
                return;
            }


            popupStage.close();
            root.setUsername(username);
            root.showLoggedSidebar(username);
            root.showHome();
            activeOverlay = null;
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.setOnAction(ev -> {
            popupStage.close();
            activeOverlay = null;
        });

        HBox buttons = new HBox(10, registerButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        Hyperlink loginLink = new Hyperlink("Hai già un account? Accedi qui");
        loginLink.setOnAction(e -> switchToLoginForm());

        popupContent.getChildren().addAll(
                new Label("Crea un nuovo account"),
                new Label("Nome:"), nomeField,
                new Label("Cognome:"), cognomeField,
                new Label("Username:"), usernameField,
                new Label("Codice Fiscale:"), cfField,
                new Label("Email:"), emailField,
                new Label("Password:"), passwordField,
                feedbackLabel, buttons,
                loginLink
        );

        activeOverlay = popupContent;
        popupStage.sizeToScene();
        popupStage.showAndWait();
        activeOverlay = null;
    }

    private void switchToSignUpForm() {
        if (popupStage != null && root != null && client != null) {
            popupStage.setTitle("Registrazione");
            // Clear and build signup content
            popupContent.getChildren().clear();

            TextField nomeField = new TextField(); nomeField.setPromptText("Nome"); nomeField.setMinWidth(200);
            TextField cognomeField = new TextField(); cognomeField.setPromptText("Cognome"); cognomeField.setMinWidth(200);
            TextField usernameField = new TextField(); usernameField.setPromptText("Username"); usernameField.setMinWidth(200);
            TextField cfField = new TextField(); cfField.setPromptText("Codice Fiscale"); cfField.setMinWidth(200);
            TextField emailField = new TextField(); emailField.setPromptText("Email"); emailField.setMinWidth(200);
            PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password"); passwordField.setMinWidth(200);

            Label feedbackLabel = new Label();

            Button registerButton = new Button("Registrati");
            registerButton.setDefaultButton(true);
            registerButton.setOnAction(e -> {
                String nome = nomeField.getText().trim();
                String cognome = cognomeField.getText().trim();
                String username = usernameField.getText().trim();
                String cf = cfField.getText().trim();
                String email = emailField.getText().trim();
                String password = passwordField.getText().trim();

                if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() ||
                        cf.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    feedbackLabel.setText("Compila tutti i campi!");
                    return;
                }

                if (!signUp(client, username, nome, cognome, cf, email, password)) {
                    feedbackLabel.setText("Username già esistente!");
                    return;
                }

                popupStage.close();
                root.setUsername(username);
                root.showLoggedSidebar(username);
                root.showHome();
                activeOverlay = null;
            });

            Button cancelButton = new Button("Annulla");
            cancelButton.setOnAction(ev -> {
                popupStage.close();
                activeOverlay = null;
            });

            HBox buttons = new HBox(10, registerButton, cancelButton);
            buttons.setAlignment(Pos.CENTER);

            Hyperlink loginLink = new Hyperlink("Hai già un account? Accedi qui");
            loginLink.setOnAction(e -> switchToLoginForm());

            popupContent.getChildren().addAll(
                    new Label("Crea un nuovo account"),
                    new Label("Nome:"), nomeField,
                    new Label("Cognome:"), cognomeField,
                    new Label("Username:"), usernameField,
                    new Label("Codice Fiscale:"), cfField,
                    new Label("Email:"), emailField,
                    new Label("Password:"), passwordField,
                    feedbackLabel, buttons,
                    loginLink
            );
            popupStage.sizeToScene();
        }
    }

    private void switchToLoginForm() {
        if (popupStage != null && root != null && client != null) {
            popupStage.setTitle("Login");
            popupContent.getChildren().clear();

            TextField usernameField = new TextField();
            usernameField.setPromptText("Username");

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Password");

            Label feedbackLabel = new Label();

            Button loginButton = new Button("Accedi");
            loginButton.setDefaultButton(true);
            loginButton.setOnAction(e -> {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    feedbackLabel.setText("Compila tutti i campi!");
                    return;
                }

                if (checkLogin(client, username, password)) {
                    root.setUsername(username);
                    root.showLoggedSidebar(username);
                    popupStage.close();
                    root.showHome();
                    activeOverlay = null;
                } else feedbackLabel.setText("Credenziali non valide.");
            });

            Button cancelButton = new Button("Annulla");
            cancelButton.setOnAction(e -> {
                popupStage.close();
                activeOverlay = null;
            });

            HBox buttons = new HBox(10, loginButton, cancelButton);
            buttons.setAlignment(Pos.CENTER);

            Hyperlink registerLink = new Hyperlink("Non hai un account? Registrati qui");
            registerLink.setOnAction(e -> switchToSignUpForm());

            popupContent.getChildren().addAll(
                    new Label("Accedi al tuo account"),
                    usernameField, passwordField,
                    feedbackLabel, buttons,
                    registerLink
            );
            popupStage.sizeToScene();
        }
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