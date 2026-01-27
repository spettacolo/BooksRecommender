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

    /**
     * Recupera tutte le raccomandazioni per un utente.
     * Aggrega i book_recommended_id trovati nel DB per lo stesso book_id.
     */
    /**
     * Recupera le raccomandazioni per un determinato libro.
     * Restituisce una lista di oggetti Recommendation, raggruppati per l'utente che le ha ricevute.
     */
    public List<Recommendation> getRecommendations(int bookId) {
        // Aggiungiamo 'username' alla SELECT per sapere a chi appartiene la raccomandazione
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
                // Ora passiamo correttamente l'utente recuperato dal DB
                result.add(new Recommendation(targetUser, bookIdStr, recIds));
            } catch (IllegalArgumentException e) {
                logger.log("Warning: found " + recIds.size() + " recommendations for user " + targetUser + " on book " + bookIdStr);
                // Fallback: limitiamo a 3 consigli come richiesto dalla logica del tuo oggetto Recommendation
                List<String> subList = recIds.subList(0, Math.min(recIds.size(), 3));
                result.add(new Recommendation(targetUser, bookIdStr, subList));
            }
        }
        return result;
    }

    public List<Recommendation> getRecommendationsMadeBy(String senderUsername) {
        // Nota: Questa query assume che tu abbia aggiunto la colonna 'sender_username' nella tabella recommendations
        String query = "SELECT username, book_id, book_recommended_id FROM recommendations WHERE username = ?";
        Map<String, List<String>> groupedRecs = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, senderUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Qui l'utente 'username' è il destinatario, 'senderUsername' è chi ha creato il consiglio
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

    /**
     * Aggiunge una raccomandazione. Dato che l'oggetto Recommendation può contenere più ID,
     * eseguiamo un inserimento per ogni libro consigliato.
     */
    public boolean addRecommendation(Recommendation rec) {
        String query = "INSERT INTO recommendations (username, book_id, book_recommended_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false); // Usiamo una transazione per sicurezza

            for (String recId : rec.getRecommendedBookIds()) {
                stmt.setString(1, rec.getUserId());
                stmt.setInt(2, Integer.parseInt(rec.getBookId()));
                stmt.setInt(3, Integer.parseInt(recId));
                stmt.addBatch(); // Ottimizza l'inserimento multiplo
            }

            int[] rows = stmt.executeBatch();
            connection.commit();
            return rows.length > 0;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            logger.log("Error adding recommendation batch: " + e.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    /**
     * Rimuove tutte le raccomandazioni per un determinato libro letto da un utente.
     */
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