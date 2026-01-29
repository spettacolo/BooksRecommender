package ONA.booksrecommender.client;

import ONA.booksrecommender.client.view.RootView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    /**
     * Punto di ingresso principale dell'interfaccia grafica JavaFX.
     * Inizializza lo stage principale impostando il titolo, le dimensioni minime
     * e caricando la {@link RootView}. Configura inoltre la scena applicando
     * il foglio di stile CSS esterno e definendo il colore di sfondo predefinito.
     *
     * @param primaryStage Lo stage principale fornito dalla piattaforma JavaFX.
     */
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

    /**
     * Metodo main standard per l'avvio dell'applicazione.
     * Utilizza il metodo {@code launch(args)} per far partire il ciclo di vita
     * dell'applicazione JavaFX e invocare il metodo {@link #start(Stage)}.
     *
     * @param args Argomenti passati da riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
