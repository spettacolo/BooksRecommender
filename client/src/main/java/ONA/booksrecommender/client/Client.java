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
}