package ONA.booksrecommender.server;

import java.io.*;
import java.net.*;

public class Server implements Runnable {
    private static final int PORT = 1234;
    private static final String SEPARATOR = ";";
    private volatile boolean running = true; // controllo per chiudere il server

    private ServerSocket serverSocket;

    @Override
    public void run() {
        System.out.println("Avvio del server...");
        
        try { // inizializzare connessione al db
            System.out.println("Errore");
        } catch (Exception e) {
            System.out.println("Errore");
        }
        
        try (ServerSocket ss = new ServerSocket(PORT)) {
            this.serverSocket = ss;
            System.out.println("Server in ascolto sulla porta " + PORT);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Connessione accettata da " + socket.getInetAddress());

                    Thread clientThread = new Thread(() -> handleClient(socket));
                    clientThread.start();
                } catch (SocketException e) {
                    if (!running) {
                        System.out.println("Server interrotto.");
                        break;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void handleRequest(String req) {
        String[] parts = req.split(SEPARATOR);
        switch (req) {
            case "query_command":
                break;
            default:
                System.out.println("Scelta non valida.");
        }
    }

    private void handleClient(Socket socket) { // TODO: sistemare l'handler gestendo le richieste con handleRequest()
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
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // forza la chiusura del blocking accept()
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
