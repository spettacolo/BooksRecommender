package ONA.booksrecommender.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class Database implements AutoCloseable {

    private static final String URL =
            "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require";
    private static final String USER = "postgres.bhklgkxycybbqhhdwphq";
    private static final String PASSWORD = "Cicci0n3";

    private final Map<Class<? extends DAO>, DAO> daoMap = new HashMap<>();

    public Database(Logger logger) throws SQLException {
        Connection conn = createConnection();
        addDAO(new UserDAO(logger, conn));
        // addDAO(new BookDAO(logger, conn));
    }

    private void addDAO(DAO dao) {
        daoMap.put(dao.getClass(), dao);
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @SuppressWarnings("unchecked")
    public <T extends DAO> T getDAO(Class<T> clazz) {
        return (T) daoMap.get(clazz);
    }

    @Override
    public void close() {
        for (DAO dao : daoMap.values()) {
            dao.close();
        }
    }
}
