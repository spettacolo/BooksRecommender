package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.User;
import ONA.booksrecommender.server.errors.UserNotFoundException;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends BaseDAO implements AutoCloseable {

    public UserDAO(Logger logger, Connection connection) {
        super(logger, connection); // crea la connessione nel costruttore di BaseDAO
    }

    public User getUser(String userId, boolean login) {
        String query;

        if (login)
            query = "SELECT username, name, surname, tax_code, email, password FROM users WHERE username = ?";
        else
            query = "SELECT * FROM users WHERE username = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            return new User(rs.getString("username"), rs.getString("name"), rs.getString("surname"), rs.getString("tax_code"), rs.getString("email"), login ? rs.getString("password") : null);
        } catch (SQLException e) {
            // e.printStackTrace();
            logger.log("Error during login: " + e.getMessage());
            return null;
        }
    }

    public int login(String userId, String password) {
        try {
            User user = getUser(userId, true);

            if (user == null)
                return -3;
            if (!user.getPassword().equals(password)) return -2;

            return 0;
        } catch (Exception e) {
            // e.printStackTrace();
            logger.log("Error during user retrieval: " + e.getMessage());
            return -3;
        }
    }

    // TODO: fixare con getUser
    public boolean signUpUser(String userId, String name, String surname, String fiscalCode, String email, String password) {
        String insertQuery = "INSERT INTO users(username, name, surname, tax_code, email, password) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            User user = getUser(userId, false);

            if (user != null) {
                return false; // User already exists
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, userId);
                insertStmt.setString(2, name);
                insertStmt.setString(3, surname);
                insertStmt.setString(4, fiscalCode);
                insertStmt.setString(5, email);
                insertStmt.setString(6, password);

                insertStmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            // e.printStackTrace();
            logger.log("Error during user signup: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
