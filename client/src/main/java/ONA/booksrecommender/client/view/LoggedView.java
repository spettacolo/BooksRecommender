package ONA.booksrecommender.client.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoggedView {

    private String username;
    private Label loginButton;     // riferimento al tasto nella sidebar

    public LoggedView(String username, Label loginButton) {
        this.username = username;
        this.loginButton = loginButton;
    }

    /** Cambia il pulsante "Login" -> username */
    public void applyLoggedUI() {
        Platform.runLater(() -> {
            loginButton.setText(username);

            // Aggiorna librerie nella sidebar
            if (loginButton.getScene() != null) {
                HomeView hv = (HomeView) loginButton.getScene().getRoot();
                hv.updateLibrariesInSidebar(username);
            }
        });
    }

    /** Torna allo stato "Login" */
    public void applyLoggedOutUI(Runnable loginAction) {
        Platform.runLater(() -> {
            loginButton.setText("Login");
            loginButton.setGraphic(null);
            loginButton.setOnMouseClicked(e -> loginAction.run());
        });
    }

    public String getUsername() {
        return username;
    }
}