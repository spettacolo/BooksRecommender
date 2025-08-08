package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO implements DAO {
    protected Connection connection;
    protected Logger logger;

    public BaseDAO(Logger logger, Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }

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
