package ONA.booksrecommender.client;

import ONA.booksrecommender.client.view.RootView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("\ud83d\udcda Books Recommender \ud83d\udcda");
        RootView root = new RootView();
        Scene scene = new Scene(root, 1000, 500);
        primaryStage.setScene(scene);

        primaryStage.setMinHeight((double) 580.0F);
        primaryStage.setMinWidth((double) 1000.0F);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
