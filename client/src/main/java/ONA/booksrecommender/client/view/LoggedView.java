package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.controller.RegLog;
import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ONA.booksrecommender.client.Client;


public class LoggedView extends VBox {

    private String username;

    public LoggedView(RootView root, String username) {
        this.username = username;
        this.setSpacing(10);

        Label home = new Label("Home");
        home.setOnMouseClicked(e -> root.showHome());
        home.getStyleClass().add("default-label");


        Label user = new Label(username);
        user.setOnMouseClicked(e -> root.showUserArea(username));
        user.getStyleClass().add("default-label");

        Label yourLibraries = new Label("Le tue librerie");
        yourLibraries.getStyleClass().add("your-libraries-label");

        this.getChildren().addAll(
                home,
                user,
                yourLibraries
        );

        loadLibraries(root);
    }

    private void loadLibraries(RootView root) {
        Client client = root.getClient();

        String risposta = client.send("get_user_libraries;" + username);

        if (risposta != null && !risposta.isEmpty()) {
            String[] libs = risposta.split(",");

            for (String lib : libs) {
                String nomeLibreria = client.send("get_user_library;id;" + lib);
                if (nomeLibreria == null || nomeLibreria.isEmpty()) continue;

                Label libLabel = new Label((nomeLibreria.trim()).split(";")[1]);
                libLabel.setStyle("-fx-padding: 5 20 5 40;");
                libLabel.setOnMouseClicked(e -> root.showLibrary(lib));

                // Menu contestuale
                ContextMenu menu = new ContextMenu();
                MenuItem delete = new MenuItem("Elimina libreria");
                delete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Conferma eliminazione");
                    confirm.setHeaderText("Sei sicuro?");
                    confirm.setContentText("L'operazione non può essere annullata.");

                    confirm.showAndWait().ifPresent(button -> {
                        if (button == javafx.scene.control.ButtonType.OK) {
                            client.send("remove_library;" + lib);
                            root.showLoggedSidebar(username);
                        }
                    });
                });

                menu.getItems().add(delete);
                libLabel.setOnContextMenuRequested(e ->
                        menu.show(libLabel, e.getScreenX(), e.getScreenY())
                );
                libLabel.getStyleClass().add("default-label");

                this.getChildren().add(libLabel);
            }
        }

        // ➕ Nuova libreria
        Label newLib = new Label("+ Nuova libreria");
        newLib.setStyle("-fx-padding: 10 20 5 40;");
        newLib.setOnMouseClicked(e -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Nuova Libreria");
            dialog.setHeaderText("Crea una nuova libreria");
            dialog.setContentText("Nome:");

            dialog.showAndWait().ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    boolean ok = root.getClient().addLibrary(name, username);
                    if (ok) root.showLoggedSidebar(username);
                }
            });
        });
        newLib.getStyleClass().add("default-label");

        this.getChildren().addAll(newLib);
    }
}