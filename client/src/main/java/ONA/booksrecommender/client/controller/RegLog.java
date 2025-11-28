package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.view.HomeView;
import ONA.booksrecommender.client.view.LoggedView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RegLog {

    private Pane activeOverlay = null;

    public void showLoginForm(Pane root, Client client) {
        if (activeOverlay != null) return;

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(root.getScene().getWindow());
        popupStage.setTitle("Login");

        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setAlignment(Pos.CENTER);

        Label title = new Label("Accedi al tuo account");

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

            boolean accessoConsentito = checkLogin(client, username, password);

            if (accessoConsentito) {
                feedbackLabel.setText("Accesso riuscito!");
                popupStage.close();

                HomeView hv = (HomeView) root;
                LoggedView lv = new LoggedView(username, hv.getLoginLabel());
                hv.setLoggedView(lv);

                lv.applyLoggedUI(hv);

                activeOverlay = null;

            } else {
                feedbackLabel.setText("Credenziali non valide.");
            }
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.setOnAction(e -> {
            popupStage.close();
            activeOverlay = null;
        });

        HBox buttons = new HBox(10, loginButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        Hyperlink goToSignUp = new Hyperlink("Non hai un account? Registrati qui");
        goToSignUp.setOnAction(e -> {
            popupStage.close();
            activeOverlay = null;
            javafx.application.Platform.runLater(() -> showSignUpForm(root, client));
        });

        popupContent.getChildren().addAll(title, usernameField, passwordField, feedbackLabel, buttons, goToSignUp);

        Scene scene = new Scene(popupContent, 350, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        activeOverlay = null;
    }


    public void showSignUpForm(Pane root, Client client) {
        if (activeOverlay != null) return;

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initOwner(root.getScene().getWindow());
        popupStage.setTitle("Registrazione");

        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setAlignment(Pos.CENTER);

        Label title = new Label("Crea un nuovo account");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome");

        TextField cognomeField = new TextField();
        cognomeField.setPromptText("Cognome");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField cfField = new TextField();
        cfField.setPromptText("Codice Fiscale");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

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

            boolean registrato = signUp(client, username, nome, cognome, cf, email, password);

            if (!registrato) {
                feedbackLabel.setText("Username già esistente!");
                return;
            }

            feedbackLabel.setText("Registrazione completata!");
            popupStage.close();

            HomeView hv = (HomeView) root;
            LoggedView lv = new LoggedView(username, hv.getLoginLabel());
            hv.setLoggedView(lv);

            lv.applyLoggedUI(hv);

            activeOverlay = null;
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.setOnAction(e -> {
            popupStage.close();
            activeOverlay = null;
        });

        HBox buttons = new HBox(10, registerButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        popupContent.getChildren().addAll(
                title, nomeField, cognomeField, usernameField,
                cfField, emailField, passwordField,
                feedbackLabel, buttons
        );

        Scene scene = new Scene(popupContent, 400, 400);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        activeOverlay = null;
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