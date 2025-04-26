/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ONA.booksrecommender.server;

/**
 *
 * @author falzy
 */

import java.io.*;
import java.net.*;

public class Server implements Runnable {
    private static final int PORT = 1234;

    @Override
    public void run() {
        System.out.println("Avvio del server...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server in ascolto sulla porta " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connessione accettata da " + socket.getInetAddress());

                Thread clientThread = new Thread(() -> {
                    try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
                    ) {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println("Ricevuto: " + message);
                            out.println("Echo: " + message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                clientThread.start(); // Avvia il thread client
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true); // Imposta il server come daemon
        serverThread.start();

        System.out.println("Server avviato come daemon. Il main thread può terminare senza blocchi.");
        
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/*
SECONDA OPZIONE: 1 SOLO THREAD SERVER CHE GESTISCE TUTTI I CLIENT, CAPIRE COSA CONVIENE FARE

package ONA.booksrecommender.server;

import java.io.*;
import java.net.*;

public class Server implements Runnable {
    private static final int PORT = 1234;

    @Override
    public void run() {
        System.out.println("Avvio del server...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server in ascolto sulla porta " + PORT);

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    System.out.println("Connessione accettata da " + socket.getInetAddress());

                    String message = in.readLine();
                    if (message != null) {
                        System.out.println("Ricevuto: " + message);
                        out.println("Echo: " + message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // il socket viene chiuso automaticamente grazie al try-with-resources
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();

        System.out.println("Server avviato come daemon. Il main thread può terminare senza blocchi.");

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

*/
