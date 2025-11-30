package ONA.booksrecommender.client;

import ONA.booksrecommender.client.view.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("\ud83d\udcda Books Recommender \ud83d\udcda");
        HomeView homeView = new HomeView();
        Scene scene = new Scene(homeView, (double) 1000.0F, (double) 500.0F);
        // scene.getStylesheets().add(this.getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinHeight((double) 500.0F);
        primaryStage.setMinWidth((double) 900.0F);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
