package ONA.booksrecommender.client;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String host = "localhost"; // oppure l'IP del server, tipo "192.168.1.100"
        int porta = 1234;

        try (Socket socket = new Socket(host, porta)) {
            System.out.println("Connesso al server su " + host + ":" + porta);

            String risposta = getString(socket, "get_user;luigi");
            System.out.println("Server response: " + risposta);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
