package ONA.booksrecommender.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/BooksRecommender";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Az-3425";

    private Connection connection;

    public Database() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Connection getConnection() {
        return connection;
    }

    // Da sviluppare i metodi per le query, farò in seguito
    public void testQuery() {
        
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
