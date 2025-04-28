package ONA.booksrecommender.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Books Recommender — Client");
        // Qui costruisci la tua UI JavaFX, ad esempio:
        primaryStage.setScene(new Scene(new Label("Benvenuto nel Client!"), 400, 200));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
