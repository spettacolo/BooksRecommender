package ONA.booksrecommender.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection conn;
    
    public UserDAO(Connection conn) {
        this.conn = conn;
    }
    
    public int login(String userId, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) return -1;
            
            String storedPassword = rs.getString("password");
            
            if (!storedPassword.equals(password)) return -2;
            
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -3;
        }
    }
    
    public boolean signUpUser(String userId, String name, String surname, String fiscalCode, String password) {
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users(username, nome, cognome, cf, password) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setString(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false; // User already exists
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, userId);
                insertStmt.setString(2, name);
                insertStmt.setString(3, surname);
                insertStmt.setString(4, fiscalCode);
                insertStmt.setString(5, password);

                insertStmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
