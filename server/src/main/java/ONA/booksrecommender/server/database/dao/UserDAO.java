package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.utils.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends BaseDAO {
    
    public UserDAO(Logger logger) {
        super(logger); // crea la connessione nel costruttore di BaseDAO
    }
    
    public int login(String userId, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) return -1;
            
            String storedPassword = rs.getString("password");
            
            if (!storedPassword.equals(password)) return -2;
            
            return 0;
        } catch (SQLException e) {
            // e.printStackTrace();
            logger.log("Error during login: " + e.getMessage());
            return -3;
        }
    }
    
    public boolean signUpUser(String userId, String name, String surname, String fiscalCode, String password) {
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users(username, nome, cognome, cf, password) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {

            checkStmt.setString(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false; // User already exists
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, userId);
                insertStmt.setString(2, name);
                insertStmt.setString(3, surname);
                insertStmt.setString(4, fiscalCode);
                insertStmt.setString(5, password);

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
