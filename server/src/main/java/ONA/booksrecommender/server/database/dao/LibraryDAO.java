package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.objects.Library;
//import ONA.booksrecommender.objects.User;
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
        super(logger, connection); // crea la connessione nel costruttore di BaseDAO
        this.bookDAO = bookDAO; // TODO: capire se è stato posizionato bene oppure no, al momento lo sfrutto per costruire il resto della classe
    }

    public Library getLibrary(int id) {
        String query = "SELECT * FROM library_books WHERE library_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                Book book = bookDAO.getBook(Integer.parseInt(rs.getString("book_id")));
                books.add(book);
            }

            String libraryQuery = "SELECT * FROM libraries WHERE library_id = ?";

            Library library;

            try (PreparedStatement libStmt = connection.prepareStatement(query)) {
                ResultSet libRs = libStmt.executeQuery();

                library = new Library(libRs.getInt("library_id"), libRs.getString("library_name"), libRs.getString("username"), books);
            }

            return library;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    } // chiama una query sql per ottenere i campi in library_books e chiama il getBook(book_id) per restituire un oggetto completo

    public Library getLibrary(String name) {
        String query = "SELECT * FROM libraries WHERE library_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            return getLibrary(rs.getInt("library_id"));
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    // Nota: potrebbe restituire una lista di librerie, motivo per cui non sarà getLibrary bensì getLibraries
    public List<Library> getLibraries(Book book) {
        return null;
    }

    // Probabilmente ridondante ma utile nel caso si ha solo l'username, si risparmia una richiesta inutile, decidere se tenerle entrambe o meno
    // DECISIONE: mantenere solo il metodo con parametro username, è inutile prendere come parametro tutto l'User
    public List<Library> getLibraries(String username) {
        String query = "SELECT * FROM libraries WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            List<Library> libraries = new ArrayList<>();

            while (rs.next()) {
                Library library = getLibrary(rs.getInt("library_id")); //, rs.getString("library_name"), rs.getString("username"));
                libraries.add(library);
            }

            return libraries;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null; // TODO: decidere se cambiare i null con liste vuote
        }
    }

    /*public List<Library> getLibraries(User user) {
        String query = "SELECT * FROM libraries WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUserId());

            ResultSet rs = stmt.executeQuery();

            List<Library> libraries = new ArrayList<>();

            while (rs.next()) {
                Library library = getLibrary(rs.getInt("library_id")); // rs.getString("library_name"), rs.getString("username"));
                libraries.add(library);
            }

            return libraries;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }*/

    public boolean addLibrary(String library, String username) {
        String query = "INSERT INTO libraries (library_name, username) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, library);
            stmt.setString(2, username);

            int rows = stmt.executeUpdate();

            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
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
            logger.log("Error during book retrieval: " + e.getMessage());
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
            logger.log("Error during book retrieval: " + e.getMessage());
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
            logger.log("Error during book retrieval: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBookLinkedLibrary(Book book, Library library) {
        return true;
    }

    public boolean removeBook(Book book, Library library) {
        return true;
    }
}
