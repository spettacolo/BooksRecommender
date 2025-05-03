package ONA.booksrecommender.server;

import java.io.*;
import java.net.*;

import java.sql.SQLException;
import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.*; // UserDAO + BookDAO

import ONA.booksrecommender.utils.Logger;

public class Server implements Runnable {
    private static final int PORT = 1234;
    private static final String SEPARATOR = ";";
    private volatile boolean running = true; // controllo per chiudere il server

    private ServerSocket serverSocket;
    
    private Logger logger;
    private Database database;
    private UserDAO userDAO;
    private BookDAO bookDAO;
    
    private boolean initLogger() {
        this.logger = new Logger();
        logger.log("Logger started successfully.");
        return true;
    }
    
    private boolean initDatabase() {
        try{
            this.database = new Database();
            this.userDAO = new UserDAO(database.getConnection());
            this.bookDAO = new BookDAO(database.getConnection());
            logger.log("Database started successfully");
            return true;
        } catch (SQLException e) {
            logger.log("Database connection error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void run() {
        logger.log("Starting server . . .");
        initLogger();
        
        logger.log("Starting database . . .");        
        if (!initDatabase()) {
            logger.log("Database init failed. Stopping the server");
            return;
        }
        
        try { // inizializzare connessione al db
            System.out.println("Errore");
        } catch (Exception e) {
            logger.log("Error: " + e);
        }
        
        try (ServerSocket ss = new ServerSocket(PORT)) {
            this.serverSocket = ss;
            logger.log("Server listening on port " + PORT);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    logger.log("Connection accepted by " + socket.getInetAddress());

                    Thread clientThread = new Thread(() -> handleClient(socket));
                    clientThread.start();
                } catch (SocketException e) {
                    if (!running) {
                        logger.log("Server stopped");
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
                logger.log("Invalid choice");
        }
    }

    private void handleClient(Socket socket) { // TODO: sistemare l'handler gestendo le richieste con handleRequest()
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                logger.log("Received: " + message); // TODO: sistemare il logger gestendo i tipi di log
                out.println("Echo: " + message);
            }
        } catch (IOException e) {
            // e.printStackTrace();
            logger.log("Error " + e.getMessage()); // TODO: cambiare in e.printStackTrace(); per ottenere l'errore completo
        } finally {                                // per farlo, però, devo prima salvarlo in un PrintWriter
            try {                                  // StringWriter sw = new StringWriter();
                socket.close();                    // e.printStackTrace(new PrintWriter(sw));
            } catch (IOException e) {              // String logMsg = "Errore di esempio: " + sw.toString();
                // e.printStackTrace();
                logger.log("Error " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        if (database != null) {
            database.close();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // forza la chiusura del blocking accept()
            }
        } catch (IOException e) {
            // e.printStackTrace();
            logger.log("Error " + e.getMessage());
        }
    }
}
