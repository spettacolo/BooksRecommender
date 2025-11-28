package ONA.booksrecommender.client.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class LoggedView {

    private final String username;
    private final Label loginButton;

    public LoggedView(String username, Label loginButton) {
        this.username = username;
        this.loginButton = loginButton;
    }

    public void applyLoggedUI(HomeView home) {
        Platform.runLater(() -> {

            loginButton.setText(username);

            loginButton.setOnMouseClicked(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Area Utente");
                alert.setHeaderText(null);
                alert.setContentText("Ecco la tua area utente, " + username);
                alert.showAndWait();
            });

            home.updateLibrariesInSidebar(username);

            attachNewLibraryAction(home);
        });
    }

    private void attachNewLibraryAction(HomeView home) {
        Label newLibraryLabel = home.getNewLibraryLabel();

        newLibraryLabel.setOnMouseClicked(e -> {
            javafx.scene.control.TextInputDialog dialog =
                    new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Nuova Libreria");
            dialog.setHeaderText("Crea una nuova libreria");
            dialog.setContentText("Nome libreria:");

            dialog.showAndWait().ifPresent(libraryName -> {
                if (!libraryName.trim().isEmpty()) {
                    boolean ok = home.getClient().addLibrary(libraryName, username);

                    if (ok) {
                        home.updateLibrariesInSidebar(username);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Successo");
                        alert.setHeaderText(null);
                        alert.setContentText("Libreria creata correttamente!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Errore");
                        alert.setHeaderText(null);
                        alert.setContentText("Impossibile creare la libreria.");
                        alert.showAndWait();
                    }
                }
            });
        });
    }

    public String getUsername() { return username; }
}