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

            // String risposta = getString(socket, "get_book;top;business & economics;10");
            // String risposta = getString(socket, "get_book;title;harry potter");
//            String risposta = getString(socket, "sign_up;cocomero;nicholias;mariio;abcmammt;cocomo@gmail.com;C0c0m3r0");
            String risposta = getString(socket, request);
            System.out.println("Server response: " + risposta);
//            String risposta2 = getString(socket, "login;cocomero;C0c0m3r0");
//            System.out.println("Server response: " + risposta2);

            return risposta;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getString(Socket socket, String richiesta) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            /*
            // Scrive un messaggio al server
            out.println("Ciao dal client!");

            // Riceve la risposta
            String risposta = in.readLine();
            System.out.println("Risposta del server: " + risposta);
            */

        //out.println("get_user;luigi");
        //out.println("get_book;title;1 is one");
        //out.println("test_get_book_image");
        //out.println("add_library;test;luigi");
        out.println(richiesta);
        return in.readLine();
    }

    // Metodo per filtrare immagini in base a larghezza e altezza
    // Restituisce true se l'immagine è verticale o quadrata (height >= width)
    public boolean isPortraitOrSquare(String imageUrl) {
        if (imageUrl.equals("https://i.ibb.co/QLTNDQc/bookplaceholder.png")) return false;
        try {
            // Carica l'immagine in background
            javafx.scene.image.Image img = new javafx.scene.image.Image(imageUrl, true);
            if (img.getProgress() < 1.0) {
                // L'immagine non è ancora caricata completamente
                // Puoi aggiungere un listener per gestire il caricamento asincrono
                img.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() >= 1.0) {
                        System.out.println("Risposta " + newProgress.doubleValue());
                        double width = img.getWidth();
                        double height = img.getHeight();
                        if (height >= width) {
                            System.out.println("Immagine " + imageUrl + " verticale o quadrata");
                        } else {
                            System.out.println("Immagine " + imageUrl + " orizzontale, scartata");
                        }
                    }
                });
                return false; // ritorna false temporaneamente, la decisione finale avverrà nel listener
            } else {
                return img.getHeight() >= img.getWidth();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento immagine: " + e.getMessage());
            return false;
        }
    }
}