package ONA.booksrecommender.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe centrale per la gestione della persistenza dei dati.
 * Si occupa di stabilire la connessione JDBC con il database PostgreSQL remoto
 * e di inizializzare/mantenere i riferimenti ai vari oggetti DAO (Data Access Object)
 * necessari per le operazioni CRUD.
 */
public class Database implements AutoCloseable {

    private static final String URL =
            "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require";
    private static final String USER = "postgres.bhklgkxycybbqhhdwphq";
    private static final String PASSWORD = "Cicci0n3";

    private final Map<Class<? extends DAO>, DAO> daoMap = new HashMap<>();

    /**
     * Costruttore della classe Database.
     * Stabilisce la connessione iniziale e istanzia tutti i DAO necessari
     * (User, Book, Library, Rating, Recommendation), iniettando le dipendenze
     * incrociate dove richiesto.
     *
     * @param logger L'istanza di {@link Logger} per registrare eventi e query.
     * @throws SQLException Se la connessione al database fallisce o se si verificano errori
     * durante l'inizializzazione dei DAO.
     */
    public Database(Logger logger) throws SQLException {
        Connection conn = createConnection();
        addDAO(new UserDAO(logger, conn));
        addDAO(new BookDAO(logger, conn));
        addDAO(new LibraryDAO(logger, conn, getDAO(BookDAO.class)));
        addDAO(new RatingDAO(logger, conn, getDAO(BookDAO.class)));
        addDAO(new RecommendationDAO(logger, conn));
    }

    /**
     * Registra internamente un'istanza di DAO nella mappa di gestione.
     *
     * @param dao L'oggetto DAO da aggiungere.
     */
    private void addDAO(DAO dao) {
        daoMap.put(dao.getClass(), dao);
    }

    /**
     * Crea e restituisce una nuova connessione JDBC verso il database PostgreSQL.
     * Utilizza i parametri di connessione (URL, USER, PASSWORD) definiti nelle costanti di classe.
     *
     * @return Un oggetto {@link Connection} attivo.
     * @throws SQLException In caso di problemi di rete o credenziali errate.
     */
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Recupera un'istanza specifica di un DAO tramite la sua classe.
     * Utilizzato dai componenti del server per ottenere l'accesso alle funzioni
     * di persistenza specifiche.
     *
     * @param <T>   Il tipo specifico che estende {@link DAO}.
     * @param clazz Il riferimento alla classe DAO richiesta.
     * @return L'istanza del DAO richiesta, castata al tipo appropriato.
     */
    @SuppressWarnings("unchecked")
    public <T extends DAO> T getDAO(Class<T> clazz) {
        return (T) daoMap.get(clazz);
    }

    /**
     * Chiude tutte le risorse associate al database.
     * Itera attraverso la mappa dei DAO e richiama il metodo {@code close()} di ciascuno
     * per chiudere in sicurezza gli Statement e le connessioni SQL attive.
     */
    @Override
    public void close() {
        for (DAO dao : daoMap.values()) {
            dao.close();
        }
    }
}