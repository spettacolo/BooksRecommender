package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.objects.Library;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibraryDAO extends BaseDAO implements AutoCloseable {
    private BookDAO bookDAO;

    public LibraryDAO(Logger logger, Connection connection, BookDAO bookDAO) {
        super(logger, connection);
        this.bookDAO = bookDAO;
    }

    public Library getLibrary(int id) {
        List<Book> books = new ArrayList<>();

        // 1. Recupero dei libri: usa book_id dalla tabella library_books
        String booksQuery = "SELECT book_id FROM library_books WHERE library_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(booksQuery)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = bookDAO.getBook(rs.getInt("book_id"));
                    if (book != null) {
                        books.add(book);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log("Error retrieving books for library ID " + id + ": " + e.getMessage());
            return null;
        }

        // 2. Recupero dei dettagli della libreria dalla tabella libraries
        String libraryQuery = "SELECT * FROM libraries WHERE library_id = ?";
        try (PreparedStatement libStmt = connection.prepareStatement(libraryQuery)) {
            libStmt.setInt(1, id);

            try (ResultSet libRs = libStmt.executeQuery()) {
                if (libRs.next()) {
                    System.out.println(libRs.getString("library_name"));
                    return new Library(
                            libRs.getInt("library_id"),
                            libRs.getString("library_name"),
                            libRs.getString("username"),
                            books
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            logger.log("Error retrieving library details for ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    public Library getLibrary(String name, String username) {
        String query = "SELECT * FROM libraries WHERE library_name = ? AND username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getLibrary(rs.getInt("library_id"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.log("Error during library retrieval: " + e.getMessage());
            return null;
        }
    }

    public List<Library> getLibraries(Book book) {
        return null;
    }

    public List<Library> getLibraries(String username) {
        String query = "SELECT * FROM libraries WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Library> libraries = new ArrayList<>();

                while (rs.next()) {
                    Library library = getLibrary(rs.getInt("library_id"));
                    if (library != null) {
                        libraries.add(library);
                    }
                }

                return libraries;
            }
        } catch (SQLException e) {
            logger.log("Error during libraries retrieval: " + e.getMessage());
            return new ArrayList<>(); // Restituisce lista vuota invece di null
        }
    }

    public boolean addLibrary(String library, String username) {
        String query = "INSERT INTO libraries (library_name, username) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, library);
            stmt.setString(2, username);

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error adding library: " + e.getMessage());
            return false;
        }
    }

    public boolean updateLibrary(Library library, Library updatedLibrary) {
        String query = "UPDATE libraries SET library_name = ?, username = ? WHERE library_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, updatedLibrary.getName());
            stmt.setString(2, updatedLibrary.getUserId());
            stmt.setInt(3, library.getId());

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error updating library: " + e.getMessage());
            return false;
        }
    }

    public boolean removeLibrary(Library library) {
        String query = "DELETE FROM libraries WHERE library_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, library.getId());

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error removing library: " + e.getMessage());
            return false;
        }
    }

    public boolean addBook(Book book, Library library) {
        String query = "INSERT INTO library_books (book_id, library_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, book.getId());
            stmt.setInt(2, library.getId());

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error adding book to library: " + e.getMessage());
            return false;
        }
    }

    /*public boolean updateBookLinkedLibrary(Book book, Library library) {
        return true;
    }*/

    public boolean removeBook(Book book, Library library) {
        String query = "DELETE FROM library_books WHERE book_id = ? AND library_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, book.getId());
            stmt.setInt(2, library.getId());

            int rows = stmt.executeUpdate();
            // Ritorna true se almeno una riga è stata eliminata
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error removing book from library: " + e.getMessage());
            return false;
        }
    }
}