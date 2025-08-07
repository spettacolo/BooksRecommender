package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseDAO implements DAO {
    protected Connection connection;
    protected Logger logger;

    public BaseDAO(Logger logger) {
        try {
            this.connection = createConnection();
        } catch (SQLException e) {
            logger.log("Failed to create DB connection: " + e.getMessage());
            throw new RuntimeException("Failed to create DB connection", e);
        }
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:postgresql://postgres.bhklgkxycybbqhhdwphq:Cicci0n3@aws-0-eu-central-1.pooler.supabase.com:5432/postgres"
            //"postgres",
            //"Cicci0n3"
        );
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            // e.printStackTrace(); // oppure log
            logger.log("Errore durante la chiusura connessione: " + e.getMessage());
        }
    }
}
