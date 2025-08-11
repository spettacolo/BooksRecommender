package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO extends BaseDAO implements AutoCloseable {
    
    public BookDAO (Logger logger, Connection connection){
        super(logger, connection);
    }

    public Book getBook(int id) throws SQLException {
        String query = "SELECT * FROM books WHERE book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            List<String> authors = getBookAuthors(rs.getInt("book_id"));
            /*List<String> authors = new ArrayList<>();
            String authorsQuery = "SELECT a.author_name " +
                    "FROM authors a " +
                    "JOIN book_authors ba ON a.author_id = ba.author_id " +
                    "WHERE ba.book_id = ?";

            try (PreparedStatement authorsStmt = connection.prepareStatement(authorsQuery)) {
                authorsStmt.setInt(1, rs.getInt("book_id"));

                ResultSet authorsRs = authorsStmt.executeQuery();

                while (authorsRs.next()) {
                    authors.add(authorsRs.getString("author_name"));
                }
            } catch (SQLException e) {
                logger.log("Error during book's authors retrieval: " + e.getMessage());
            }*/

            return new Book(rs.getInt("book_id"), rs.getString("title"), authors, rs.getInt("publish_year"), rs.getString("publishers"), rs.getString("category"));
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    public Book getBook(String title) {
        String query = "SELECT * FROM books WHERE title = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, title);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            List<String> authors = getBookAuthors(rs.getInt("book_id"));

            return new Book(rs.getInt("book_id"), rs.getString("title"), authors, rs.getInt("publish_year"), rs.getString("publishers"), rs.getString("category"));
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    public List<Book> getAuthorBooks(String author) {
        String query = "SELECT ba.book_id " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "WHERE a.author_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, author);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                Book book = getBook(rs.getInt("book_id"));
                books.add(book);
            }

            return books;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Book> getAuthorBooks(String author, int year) {
        String query = "SELECT ba.book_id " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "JOIN books b ON ba.book_id = b.book_id " +
                "WHERE a.author_name = ? AND b.publish_year = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, author);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                Book book = getBook(rs.getInt("book_id"));
                books.add(book);
            }

            return books;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /*public Book getBook(List<String> authors) {
        String query = "";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            //
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
        }

        return new Book(1, "test", null, 1, "test", "test");
    }*/

    public List<String> getBookAuthors(int id) {
        String authorsQuery = "SELECT a.author_name " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "WHERE ba.book_id = ?";

        List<String> authors = new ArrayList<>();

        try (PreparedStatement authorsStmt = connection.prepareStatement(authorsQuery)) {
            authorsStmt.setInt(1, id);

            ResultSet authorsRs = authorsStmt.executeQuery();

            while (authorsRs.next()) {
                authors.add(authorsRs.getString("author_name"));
            }

            return authors;
        } catch (SQLException e) {
            logger.log("Error during book's authors retrieval: " + e.getMessage());
            //return null;
            return new ArrayList<>(); // TODO: applicare questo metodo di gestione su tutti i metodi DAO e migliorare i controlli per prevenire errori inaspettati
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
