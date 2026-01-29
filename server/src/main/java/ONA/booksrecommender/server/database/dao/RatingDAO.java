package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.objects.Library;
import ONA.booksrecommender.objects.Rating;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO extends BaseDAO implements AutoCloseable {
    private BookDAO bookDAO;

    /**
     * Costruttore della classe RatingDAO.
     * Inizializza l'accesso ai dati per le recensioni dei libri.
     *
     * @param logger     L'istanza di {@link Logger} per registrare operazioni ed errori.
     * @param connection La connessione attiva al database PostgreSQL.
     * @param bookDAO    Il riferimento al DAO dei libri (iniettato per future espansioni o join).
     */
    public RatingDAO(Logger logger, Connection connection, BookDAO bookDAO) {
        super(logger, connection);
        this.bookDAO = bookDAO;
    }

    /**
     * Recupera tutte le valutazioni associate a un determinato libro.
     * Utile per calcolare la media dei voti o mostrare i commenti nella scheda libro.
     *
     * @param bookId L'identificativo del libro.
     * @return Una lista di oggetti {@link Rating} contenenti i voti e le note degli utenti.
     * @throws RuntimeException Se si verifica un errore SQL non gestito.
     */
    public List<Rating> getRatings(int bookId) {
        String query = "SELECT * FROM ratings WHERE book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {

                List<Rating> ratings = new ArrayList<>();

                while (rs.next()) {
                    ratings.add(new Rating(rs.getString("username"), rs.getString("book_id"), rs.getInt("style"), rs.getInt("content"), rs.getInt("liking"), rs.getInt("originality"), rs.getInt("edition"), rs.getString("notes")));
                }

                return ratings;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recupera tutte le valutazioni effettuate da uno specifico utente.
     * Viene utilizzato principalmente nell'Area Utente per mostrare lo storico delle attività.
     *
     * @param username Lo username dell'utente.
     * @return Una lista di oggetti {@link Rating} creati dall'utente.
     * @throws RuntimeException Se si verifica un errore SQL durante l'esecuzione della query.
     */
    public List<Rating> getRatings(String username) {
        String query = "SELECT * FROM ratings WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {

                List<Rating> ratings = new ArrayList<>();

                while (rs.next()) {
                    ratings.add(new Rating(rs.getString("username"), rs.getString("book_id"), rs.getInt("style"), rs.getInt("content"), rs.getInt("liking"), rs.getInt("originality"), rs.getInt("edition"), rs.getString("notes")));
                }

                return ratings;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserisce una nuova valutazione multidimensionale nel database.
     * Se le note sono nulle, viene inserito il segnaposto "EMPTY" per evitare conflitti.
     *
     * @param bookId      L'ID del libro da valutare.
     * @param username    L'utente che effettua la valutazione.
     * @param style       Voto per lo stile (1-5).
     * @param content     Voto per il contenuto (1-5).
     * @param liking      Voto per il gradimento personale (1-5).
     * @param originality Voto per l'originalità (1-5).
     * @param edition     Voto per l'edizione (1-5).
     * @param notes       Commento testuale opzionale.
     * @return {@code true} se la recensione è stata salvata correttamente.
     */
    public boolean addRating(int bookId, String username, int style, int content, int liking, int originality, int edition, String notes) {
        if (notes == null) notes = "EMPTY";
        String query = "INSERT INTO ratings (username, book_id, style, content, liking, originality, edition, notes) VALUES (?, ?, ? ,? ,? ,? ,? ,?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, bookId);
            stmt.setInt(3, style);
            stmt.setInt(4, content);
            stmt.setInt(5, liking);
            stmt.setInt(6, originality);
            stmt.setInt(7, edition);
            stmt.setString(8, notes);

            int rows = stmt.executeUpdate();

            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aggiorna i voti o il commento di una valutazione già esistente.
     * La ricerca del record avviene tramite la chiave composta (username, book_id).
     *
     * @return {@code true} se l'aggiornamento ha avuto successo.
     */
    public boolean updateRating(int bookId, String username, int style, int content, int liking, int originality, int edition, String notes) {
        String query = "UPDATE ratings SET style = ?, content = ?, liking = ?, originality = ?, edition = ?, notes = ? WHERE username = ? AND book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, style);
            stmt.setInt(2, content);
            stmt.setInt(3, liking);
            stmt.setInt(4, originality);
            stmt.setInt(5, edition);
            stmt.setString(6, notes);
            stmt.setString(7, username);
            stmt.setInt(8, bookId);

            int rows = stmt.executeUpdate();

            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina definitivamente una valutazione dal database.
     *
     * @param bookId   L'ID del libro associato alla recensione.
     * @param username Lo username dell'autore della recensione.
     * @return {@code true} se il record è stato rimosso.
     */
    public boolean removeRating(int bookId, String username) {
        String query = "DELETE FROM ratings WHERE username = ? AND book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, bookId);

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error removing library: " + e.getMessage());
            return false;
        }
    }
}
