package ONA.booksrecommender.server;

import java.io.*;
import java.net.*;

import java.sql.SQLException;
import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.*;

import ONA.booksrecommender.utils.Logger;

/**
 * Classe core del Server che gestisce il ciclo di vita dell'applicazione.
 * Implementa {@link Runnable} per gestire l'ascolto delle connessioni in un thread separato.
 * Si occupa di inizializzare il sistema di logging, la connessione al database e
 * di smistare le richieste dei client verso il {@link ServerFacade}.
 */
public class Server implements Runnable {
    private static final int PORT = 1234;
    private static final String SEPARATOR = ";";
    private volatile boolean running = true; // controllo per chiudere il server

    private ServerSocket serverSocket;
    
    private Logger logger;
    private Database database;
    private ServerFacade serverFacade;
    private UserDAO userDAO;
    private BookDAO bookDAO;

    /**
     * Inizializza il sistema di logging asincrono su un thread dedicato.
     * * @return {@code true} se il logger è stato avviato correttamente.
     */
    private boolean initLogger() {
        this.logger = new Logger();
        new Thread(this.logger, "LoggerThread").start();
        logger.log("Logger started successfully.");
        return true;
    }

    /**
     * Inizializza la connessione al database e il sistema Facade.
     * Configura tutti i DAO necessari per le operazioni del server.
     * * @return {@code true} se il database e la facciata sono pronti, {@code false} in caso di errore SQL.
     */
    private boolean initDatabase() {
        try{
            this.database = new Database(logger);
            logger.log("Database started successfully");
            this.serverFacade = new ServerFacade(logger, database);
            logger.log("Server facade initialized successfully");
            return true;
        } catch (SQLException e) {
            logger.log("Database connection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ciclo principale del server. Esegue l'inizializzazione dei servizi e
     * rimane in ascolto sulla porta specificata per accettare nuove connessioni.
     * Ogni nuova connessione viene gestita in un thread separato.
     */
    @Override
    public void run() {
        initLogger();
        logger.log("Starting server . . .");
        
        logger.log("Starting database . . .");        
        if (!initDatabase()) {
            logger.log("Database init failed. Stopping the server");
            return;
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

    /**
     * Gestisce la comunicazione con un singolo client connesso.
     * Legge i messaggi dal socket, li delega al {@link ServerFacade}
     * e invia la risposta generata al client.
     * * @param socket Il socket associato alla connessione del client.
     */
    private void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                logger.log("Received: " + message); // TODO: sistemare il logger gestendo i tipi di log
                String response;

                try {
                    response = serverFacade.handleRequest(message);
                } catch (Exception e) {
                    logger.log("Error handling request: " + e.getMessage());
                    response = "ERROR;" + e.getMessage();
                }

                out.println(response);
            }
        } catch (IOException e) {
            logger.log("Error " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log("Error " + e.getMessage());
            }
        }
    }

    /**
     * Interrompe il server in modo pulito.
     * Chiude le connessioni al database e forza la chiusura del {@link ServerSocket}
     * per sbloccare il metodo di accettazione.
     */
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
            logger.log("Error " + e.getMessage());
        }
    }
}
