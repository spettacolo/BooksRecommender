package ONA.booksrecommender.client;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {

    private String host = "localhost"; // oppure l'IP del server, tipo "192.168.1.100"
    private int porta = 1234;

    /**
     * Costruttore della classe Client.
     * Inizializza i parametri di connessione (host e porta) per la comunicazione
     * con il server tramite Socket.
     */
    public Client() {
        this.host = host;
        this.porta = porta;
    }

    /**
     * Invia una richiesta stringa al server e ne riceve la risposta.
     * Apre una connessione TCP (Socket), invia il comando e chiude la connessione
     * dopo aver letto la riga di risposta.
     *
     * @param request Il comando o la stringa di dati da inviare al server.
     * @return La risposta del server come stringa, oppure {@code null} in caso di errore di connessione.
     */
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

    /**
     * Metodo di supporto interno per gestire l'input/output su socket.
     * Invia la stringa di richiesta tramite {@link PrintWriter} e legge la risposta
     * tramite {@link BufferedReader}.
     *
     * @param socket    Il socket attivo verso il server.
     * @param richiesta La stringa da inviare.
     * @return La prima riga della risposta ricevuta dal server.
     * @throws IOException Se si verifica un errore durante la lettura o la scrittura sul socket.
     */
    private static String getString(Socket socket, String richiesta) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println(richiesta);
        return in.readLine();
    }

    /**
     * Crea un contenitore grafico contenente l'immagine della copertina scalata proporzionalmente.
     * Il metodo gestisce il caricamento asincrono dell'immagine, verifica se l'immagine è
     * valida (scartando quelle con orientamento orizzontale) e applica un placeholder
     * se l'URL è nullo o non valido.
     *
     * @param imageUrl L'URL dell'immagine di copertina da caricare.
     * @param maxWidth La larghezza massima consentita per l'immagine.
     * @param maxHeight L'altezza massima consentita per l'immagine.
     * @return Un {@link javafx.scene.layout.StackPane} contenente la {@link javafx.scene.image.ImageView} configurata.
     */
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

    /**
     * Invia una richiesta al server per creare una nuova libreria associata a un utente.
     *
     * @param libraryName Il nome della nuova libreria da creare.
     * @param username    Lo username dell'utente proprietario della libreria.
     * @return {@code true} se la creazione è avvenuta con successo (risposta OK), {@code false} altrimenti.
     */
    public boolean addLibrary(String libraryName, String username) {
        String request = "add_library;" + libraryName + ";" + username;
        String response = send(request);
        return response != null && response.equals("ADD_LIBRARY;OK");
    }
}