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
    public List<Recommendation> getRecommendations(String username) {
        // Query che recupera tutte le righe per l'utente
        String query = "SELECT book_id, book_recommended_id FROM recommendations WHERE username = ?";

        // Mappa per raggruppare i consigli: Key = book_id, Value = Lista di ID consigliati
        Map<String, List<String>> groupedRecs = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String bookId = String.valueOf(rs.getInt("book_id"));
                    String recId = String.valueOf(rs.getInt("book_recommended_id"));

                    groupedRecs.computeIfAbsent(bookId, k -> new ArrayList<>()).add(recId);
                }
            }
        } catch (SQLException e) {
            logger.log("Error retrieving recommendations for " + username + ": " + e.getMessage());
        }

        // Trasformiamo la mappa in una lista di oggetti Recommendation
        List<Recommendation> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groupedRecs.entrySet()) {
            try {
                result.add(new Recommendation(username, entry.getKey(), entry.getValue()));
            } catch (IllegalArgumentException e) {
                logger.log("Warning: found more than 3 recommendations for book " + entry.getKey());
                // Opzionale: aggiungi solo i primi 3 se il DB ne contiene di più per errore
                List<String> subList = entry.getValue().subList(0, Math.min(entry.getValue().size(), 3));
                result.add(new Recommendation(username, entry.getKey(), subList));
            }
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