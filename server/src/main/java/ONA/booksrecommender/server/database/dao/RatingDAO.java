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
