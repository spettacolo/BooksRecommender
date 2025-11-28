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

    /**
     * Mostra la finestra di login come overlay centrato all’interno della finestra principale.
     */
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

                // ottieni HomeView, prendi il label e applica l'UI loggata
                ONA.booksrecommender.client.view.HomeView hv = (ONA.booksrecommender.client.view.HomeView) root;

                // crea LoggedView passandogli username e il riferimento al label della sidebar
                ONA.booksrecommender.client.view.LoggedView lv = new ONA.booksrecommender.client.view.LoggedView(username, hv.getLoginLabel());

                // applica l'aspetto "loggato" al bottone (avatar + username)
                lv.applyLoggedUI();

                // opzionale: mostra popup di benvenuto se vuoi (se LoggedView ha showWelcomePopup)
                // lv.showWelcomePopup(true); // decommenta solo se LoggedView implementa questo metodo

                // conserva riferimento in HomeView per future operazioni (es. logout)
                hv.setLoggedView(lv);

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
        popupStage.centerOnScreen();
        popupStage.showAndWait();

        activeOverlay = null;
    }

    /**
     * Mostra la finestra di registrazione come overlay centrato all’interno della finestra principale.
     */
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

            if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() || cf.isEmpty() || email.isEmpty() || password.isEmpty()) {
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
            lv.applyLoggedUI();          // aggiorna sidebar

            hv.setLoggedView(lv);        // salva riferimento

            activeOverlay = null;
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.setOnAction(e -> {
            popupStage.close();
            activeOverlay = null;
        });

        HBox buttons = new HBox(10, registerButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        popupContent.getChildren().addAll(title, nomeField, cognomeField, usernameField, cfField, emailField, passwordField, feedbackLabel, buttons);

        Scene scene = new Scene(popupContent, 400, 400);
        popupStage.setScene(scene);
        popupStage.centerOnScreen();
        popupStage.showAndWait();

        activeOverlay = null;
    }

    /**
     * Metodo fittizio di controllo login — da collegare alla classe Registrazione.java
     */
    private boolean checkLogin(Client client, String username, String password) {
        // invia richiesta al server
        String risposta = client.send("login;" + username + ";" + password);

        if (risposta == null) return false;

        // Il server risponde nel formato: LOGIN;codice
        // dove codice può essere 0, -2, -3
        if (!risposta.contains(";")) return false;

        String[] parts = risposta.split(";");
        if (parts.length < 2) return false;

        String codiceStr = parts[1].trim();

        return codiceStr.equals("0");
    }

    private boolean signUp(Client client, String username, String name, String surname, String taxId, String email, String password) {
        // invia richiesta al server usando il comando corretto "sign_up"
        String comando = "sign_up;" + username + ";" + name + ";" + surname + ";" + taxId + ";" + email + ";" + password;
        String risposta = client.send(comando);

        if (risposta == null) return false;

        // Il server risponde ad esempio: SIGNUP;OK oppure SIGNUP;FAIL
        if (!risposta.contains(";")) return false;

        String[] parts = risposta.split(";");
        if (parts.length < 2) return false;

        String codice = parts[1].trim();

        return codice.equals("OK");
    }
}
