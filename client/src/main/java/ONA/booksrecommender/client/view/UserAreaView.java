package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class UserAreaView extends VBox {

    public UserAreaView(RootView root, String username) {
        this.setSpacing(10);

        Label title = new Label("Ciao " + username + "!");
        title.getStyleClass().add("header-title");
        this.getChildren().add(title);

        Client client = new Client();
        String risposta = client.send("get_user;" + username + ";" + true);
        System.out.println(risposta);

        String[] parts = risposta.split(";");

        Label usernameLabel = new Label("Username: " + parts[0]);
        usernameLabel.getStyleClass().add("default-label");
        this.getChildren().add(usernameLabel);

        Label nomeLabel = new Label("Nome: " + parts[1]);
        nomeLabel.getStyleClass().add("default-label");
        this.getChildren().add(nomeLabel);

        Label cognomeLabel = new Label("Cognome: " + parts[2]);
        cognomeLabel.getStyleClass().add("default-label");
        this.getChildren().add(cognomeLabel);

        Label emailLabel = new Label("Email: " + parts[3]);
        emailLabel.getStyleClass().add("default-label");
        this.getChildren().add(emailLabel);


        Label logout = new Label("Logout");
        logout.setOnMouseClicked(e -> {
            root.showUnloggedSidebar();
            root.showHome();
            root.setUsername("");
        });
        this.getChildren().add(logout);
    }
}