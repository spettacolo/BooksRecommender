package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Recommendation;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RecommendationDAO extends BaseDAO implements AutoCloseable {

    public RecommendationDAO(Logger logger, Connection connection) {
        super(logger, connection);
    }

    public List<Recommendation> getRecommendations(int bookId) {
        String query = "SELECT username, book_id, book_recommended_id FROM recommendations WHERE book_id = ?";

        // Mappa: Key = Username, Value = Lista di ID dei libri consigliati
        Map<String, List<String>> userRecsMap = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    String recId = String.valueOf(rs.getInt("book_recommended_id"));

                    userRecsMap.computeIfAbsent(user, k -> new ArrayList<>()).add(recId);
                }
            }
        } catch (SQLException e) {
            logger.log("Error retrieving recommendations for book " + bookId + ": " + e.getMessage());
        }

        List<Recommendation> result = new ArrayList<>();
        String bookIdStr = String.valueOf(bookId);

        for (Map.Entry<String, List<String>> entry : userRecsMap.entrySet()) {
            String targetUser = entry.getKey();
            List<String> recIds = entry.getValue();

            try {
                result.add(new Recommendation(targetUser, bookIdStr, recIds));
            } catch (IllegalArgumentException e) {
                logger.log("Warning: found " + recIds.size() + " recommendations for user " + targetUser + " on book " + bookIdStr);
                List<String> subList = recIds.subList(0, Math.min(recIds.size(), 3));
                result.add(new Recommendation(targetUser, bookIdStr, subList));
            }
        }
        return result;
    }

    public List<Recommendation> getRecommendationsMadeBy(String senderUsername) {
        String query = "SELECT username, book_id, book_recommended_id FROM recommendations WHERE username = ?";
        Map<String, List<String>> groupedRecs = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, senderUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // L'utente 'username' è il destinatario, 'senderUsername' è chi ha creato il consiglio
                    String bookId = String.valueOf(rs.getInt("book_id"));
                    String recId = String.valueOf(rs.getInt("book_recommended_id"));
                    groupedRecs.computeIfAbsent(bookId, k -> new ArrayList<>()).add(recId);
                }
            }
        } catch (SQLException e) {
            logger.log("Error: " + e.getMessage());
        }

        List<Recommendation> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groupedRecs.entrySet()) {
            result.add(new Recommendation(senderUsername, entry.getKey(), entry.getValue()));
        }
        return result;
    }

    public boolean addRecommendation(Recommendation rec) {
        String query = "INSERT INTO recommendations (username, book_id, book_recommended_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);

            for (String recId : rec.getRecommendedBookIds()) {
                stmt.setString(1, rec.getUserId());
                stmt.setInt(2, Integer.parseInt(rec.getBookId()));
                stmt.setInt(3, Integer.parseInt(recId));
                stmt.addBatch();
            }

            int[] rows = stmt.executeBatch();
            connection.commit();
            return rows.length > 0;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) {}
            logger.log("Error adding recommendation batch: " + e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
    
    public boolean removeRecommendations(String username, String bookId) {
        String query = "DELETE FROM recommendations WHERE username = ? AND book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, Integer.parseInt(bookId));

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error removing recommendations: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        super.close();
    }
}