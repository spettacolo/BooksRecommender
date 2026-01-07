package ONA.booksrecommender.client.view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import ONA.booksrecommender.client.controller.RegLog;
import ONA.booksrecommender.client.controller.SearchHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public class UnLoggedView extends VBox {

    public UnLoggedView(RootView root) {
        this.setSpacing(10);

        Label home = new Label("Home");
        home.setOnMouseClicked(e -> root.showHome());

        Label yourLibraries = new Label("Le tue librerie");
        yourLibraries.setStyle("-fx-font-weight: bold;");

        Label aggiungiLibreria = new Label("+ Aggiungi libreria");
        aggiungiLibreria.setOnMouseClicked(e -> new RegLog().showLoginForm(root, root.getClient()));

        Label login = new Label("Accedi");
        login.setOnMouseClicked(e -> new RegLog().showLoginForm(root, root.getClient()));

        this.getChildren().addAll(home, yourLibraries, aggiungiLibreria, login);
    }
}