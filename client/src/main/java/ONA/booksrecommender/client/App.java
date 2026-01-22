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
        scene.setFill(javafx.scene.paint.Color.rgb(44,44,46));
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
        primaryStage.setScene(scene);

        primaryStage.setMinHeight((double) 580.0F);
        primaryStage.setMinWidth((double) 1000.0F);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
