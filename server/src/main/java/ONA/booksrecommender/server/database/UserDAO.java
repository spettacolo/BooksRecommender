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
}
