package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe astratta che fornisce l'implementazione di base per tutti i Data Access Objects (DAO).
 * Centralizza i riferimenti comuni necessari per l'interazione con il database, come
 * la connessione SQL e l'istanza del logger per il tracciamento delle operazioni.
 */
public abstract class BaseDAO implements DAO {
    protected Connection connection;
    protected Logger logger;

    /**
     * Costruttore della classe BaseDAO.
     * Inizializza i componenti fondamentali necessari a ogni DAO specifico per
     * eseguire query e registrare eventi.
     *
     * @param logger     L'istanza di {@link Logger} utilizzata per il tracciamento delle query e degli errori.
     * @param connection La connessione attiva verso il database PostgreSQL.
     */
    public BaseDAO(Logger logger, Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }

    /**
     * Chiude in sicurezza la connessione al database associata al DAO.
     * Verifica che la connessione sia esistente e ancora aperta prima di procedere alla chiusura.
     * Eventuali errori durante la chiusura vengono catturati e registrati tramite il logger.
     */
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            logger.log("Errore durante la chiusura connessione: " + e.getMessage());
        }
    }
}
