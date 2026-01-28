package ONA.booksrecommender.client;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {

    private String host = "localhost"; // oppure l'IP del server, tipo "192.168.1.100"
    private int porta = 1234;

    public Client() {
        this.host = host;
        this.porta = porta;
    }

    public String send(String request) {
        try (Socket socket = new Socket(host, porta)) {
            System.out.println("Connesso al server su " + host + ":" + porta);

            String risposta = getString(socket, request);
            System.out.println("Server response: " + risposta);

            return risposta;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getString(Socket socket, String richiesta) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println(richiesta);
        return in.readLine();
    }

    public javafx.scene.layout.StackPane createScaledCover(String imageUrl, double maxWidth, double maxHeight) {

        javafx.scene.layout.StackPane container = new javafx.scene.layout.StackPane();

        // Placeholder
        if (imageUrl == null || imageUrl.equalsIgnoreCase("null")) {
            javafx.scene.image.Image placeholderImg = new javafx.scene.image.Image("https://i.ibb.co/QLTNDQc/bookplaceholder.png", true);
            javafx.scene.image.ImageView placeholderView = new javafx.scene.image.ImageView(placeholderImg);
            placeholderView.setFitWidth(maxWidth);
            placeholderView.setFitHeight(maxHeight);
            placeholderView.setPreserveRatio(true);
            container.getChildren().add(placeholderView);
            return container;
        }

        javafx.scene.image.Image img = new javafx.scene.image.Image(imageUrl, true);
        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView();

        img.progressProperty().addListener((obs, oldP, newP) -> {
            if (newP.doubleValue() >= 1.0) {
                double w = img.getWidth();
                double h = img.getHeight();

                // Scarta immagini orizzontali
                if (w > h) {
                    System.out.println(w + " x " + h);      // =^.^=
                    javafx.scene.image.Image placeholderImg = new javafx.scene.image.Image("https://i.ibb.co/QLTNDQc/bookplaceholder.png", true);
                    javafx.scene.image.ImageView placeholderView = new javafx.scene.image.ImageView(placeholderImg);
                    placeholderView.setFitWidth(maxWidth);
                    placeholderView.setFitHeight(maxHeight);
                    placeholderView.setPreserveRatio(true);
                    container.getChildren().setAll(placeholderView);
                    return;
                }

                // Scala proporzionalmente
                double scale = Math.min(maxWidth / w, maxHeight / h);
                double finalW = w * scale;
                double finalH = h * scale;

                view.setImage(img);
                view.setFitWidth(finalW);
                view.setFitHeight(finalH);
                view.setPreserveRatio(true);

                container.getChildren().setAll(view);
            }
        });

        return container;
    }

    public boolean addLibrary(String libraryName, String username) {
        String request = "add_library;" + libraryName + ";" + username;
        String response = send(request);
        return response != null && response.equals("ADD_LIBRARY;OK");
    }
}