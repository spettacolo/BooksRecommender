package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;

public class BookDAO extends BaseDAO implements AutoCloseable {
    private HttpClient client;

    public BookDAO(Logger logger, Connection connection) {
        super(logger, connection);
        this.client = HttpClient.newHttpClient();
    }

    public Book getBook(int id) {
        String query = "SELECT * FROM books WHERE book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                List<String> authors = getBookAuthors(rs.getInt("book_id"));

                String imageUrl = getBookImageUrl(rs.getInt("book_id"));

                String description = "";
                try {
                    description = rs.getString("description");
                    if (description == null) {
                        description = "";
                    }
                } catch (SQLException e) {
                    description = "";
                }

                return new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        authors,
                        rs.getInt("publish_year"),
                        rs.getString("publishers"),
                        rs.getString("category"),
                        imageUrl,
                        description
                );
            }
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    public List<Book> getBooks(List<Integer> ids) {
        List<Book> books = new ArrayList<>();
        if (ids == null) {
            return books;
        }
        try {
            for (int id : ids) {
                Book book = getBook(id);
                if (book != null) {
                    books.add(book);
                }
            }
            return books;
        } catch (Exception e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return books;
        }
    }

    public List<Book> getBooks(String title) {
        List<Book> books = new ArrayList<>();
        if (title == null) {
            return books;
        }
        String query = "SELECT * FROM books WHERE title ILIKE ? ORDER BY publish_year ASC LIMIT 20";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + title + "%";
            stmt.setString(1, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    List<String> authors = getBookAuthors(rs.getInt("book_id"));

                    String imageUrl = getBookImageUrl(rs.getInt("book_id"));

                    String description = "";
                    try {
                        description = rs.getString("description");
                        if (description == null) {
                            description = "";
                        }
                    } catch (SQLException e) {
                        description = "";
                    }

                    books.add(new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            authors,
                            rs.getInt("publish_year"),
                            rs.getString("publishers"),
                            rs.getString("category"),
                            imageUrl,
                            description
                    ));
                }

                return books;
            }
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return books;
        }
    }

    public List<Book> getBooks(String category, int limit) {
        List<Book> books = new ArrayList<>();
        if (category == null) {
            return books;
        }
        if (limit == 0) limit = 20;

        String query;
        if (category.equals("none")) {
            query = "SELECT b.book_id, b.title, b.publish_year, b.publishers, b.category, COUNT(lb.book_id) AS frequency_count " +
                    "FROM books b INNER JOIN library_books lb ON b.book_id = lb.book_id " +
                    "GROUP BY b.book_id, b.title, b.publish_year, b.publishers, b.category " +
                    "ORDER BY frequency_count DESC LIMIT ?";
        } else {
            query = "SELECT b.book_id, b.title, b.publish_year, b.publishers, b.category, 0 AS frequency_count " +
                    "FROM books b WHERE b.category ILIKE ? ORDER BY RANDOM() DESC LIMIT ?";
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (category.equals("none")) {
                stmt.setInt(1, limit);
            } else {
                String searchPattern = "%" + category + "%";
                stmt.setString(1, searchPattern);
                stmt.setInt(2, limit);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    List<String> authors = getBookAuthors(rs.getInt("book_id"));

                    String imageUrl = getBookImageUrl(rs.getInt("book_id"));

                    String description = "";
                    try {
                        description = rs.getString("description");
                        if (description == null) {
                            description = "";
                        }
                    } catch (SQLException e) {
                        description = "";
                    }

                    books.add(new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            authors,
                            rs.getInt("publish_year"),
                            rs.getString("publishers"),
                            rs.getString("category"),
                            imageUrl,
                            description
                    ));
                }

                return books;
            }
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return books;
        }
    }

    public String getBookImageUrl(int book_id) {
        String query = "SELECT image_url " +
                "FROM book_images " +
                "WHERE book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, book_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                logger.log("RISULTATO: " + rs.getString("image_url"));
                return rs.getString("image_url");
            }
        } catch (SQLException e) {
            logger.log("Error during book image url retrieval: " + e.getMessage());
            return null;
        }
    }

    //  =^.^=

    public List<Book> getAuthorBooks(String author, int limit, int offset) {
        String query = "SELECT ba.book_id " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "JOIN books b ON ba.book_id = b.book_id " +
                "WHERE a.author_name ILIKE ? " +
                "ORDER BY b.publish_year DESC " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + author + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    Book book = getBook(rs.getInt("book_id"));
                    if (book != null) books.add(book);
                }
                return books;
            }
        } catch (SQLException e) {
            logger.log("Error during pagination: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getBookAuthors(int id) {
        String authorsQuery = "SELECT a.author_name " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "WHERE ba.book_id = ?";

        List<String> authors = new ArrayList<>();

        try (PreparedStatement authorsStmt = connection.prepareStatement(authorsQuery)) {
            authorsStmt.setInt(1, id);

            try (ResultSet authorsRs = authorsStmt.executeQuery()) {
                while (authorsRs.next()) {
                    authors.add(authorsRs.getString("author_name"));
                }

                return authors;
            }
        } catch (SQLException e) {
            logger.log("Error during book's authors retrieval: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void close() {
        super.close();
    }
}