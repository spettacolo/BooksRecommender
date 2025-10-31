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

    public RatingDAO(Logger logger, Connection connection, BookDAO bookDAO) {
        super(logger, connection);
        this.bookDAO = bookDAO;
    }

    // prendi la recensione fatta dall'utente X sul libro Y
    public Rating getRating(int bookId, String username) {
        String query = "SELECT * FROM ratings WHERE book_id = ? AND username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return new Rating(rs.getString("username"), rs.getString("book_id"), rs.getInt("style"), rs.getInt("content"), rs.getInt("liking"), rs.getInt("originality"), rs.getInt("edition"), rs.getString("notes"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // prendi tutte le recensioni di un libro
    public List<Rating> getRatings(int bookId) {
        String query = "SELECT * FROM ratings WHERE book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            List<Rating> ratings = new ArrayList<>();

            while (rs.next()) {
                ratings.add(new Rating(rs.getString("username"), rs.getString("book_id"), rs.getInt("style"), rs.getInt("content"), rs.getInt("liking"), rs.getInt("originality"), rs.getInt("edition"), rs.getString("notes")));
            }

            return ratings;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // prendi tutte le recensioni di un utente
    public List<Rating> getRatings(String username) {
        String query = "SELECT * FROM ratings WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            List<Rating> ratings = new ArrayList<>();

            while (rs.next()) {
                ratings.add(new Rating(rs.getString("username"), rs.getString("book_id"), rs.getInt("style"), rs.getInt("content"), rs.getInt("liking"), rs.getInt("originality"), rs.getInt("edition"), rs.getString("notes")));
            }

            return ratings;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // se l'utente non scrive alcun commento inviare al metodo una stringa vuota (forse)
    public boolean addRating(int bookId, String username, int style, int content, int liking, int originality, int edition, String notes) {
        /* 3 opzioni:
            1- controllo se c'è già la valutazione
            2- aggiorno la valutazione già esistente
            3- controllo, chiedo all'utente e se accetta allora viene aggiornata (conviene? no)
         */
        // TODO: da fare controllo duplicati
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

    public boolean updateRating(int bookId, String username, int style, int content, int liking, int originality, int edition, String notes) {
        return true;
    }

    public boolean removeRating(int bookId, String username) {
        return true;
    }

    // TODO: toString() ?

}

