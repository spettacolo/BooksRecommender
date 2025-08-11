package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.objects.Library;
import ONA.booksrecommender.objects.User;
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

    public Library getLibrary(String libraryName, String username) {
        String query = "SELECT * FROM library_books WHERE library_name = ? AND username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, libraryName);
            stmt.setString(2, username);

            ResultSet rs = stmt.executeQuery();

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                Book book = bookDAO.getBook(rs.getString("book_id"));
                books.add(book);
            }

            return new Library(rs.getString("library_name"), rs.getString("username"), books);
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    } // chiama una query sql per ottenere i campi in library_books e chiama il getBook(book_id) per restituire un oggetto completo

    // Nota: potrebbe restituire una lista di librerie, motivo per cui non sarà getLibrary bensì getLibraries
    public List<Library> getLibraries(Book book) {
        return null;
    }

    // Probabilmente ridondante ma utile nel caso si ha solo l'username, si risparmia una richiesta inutile, decidere se tenerle entrambe o meno
    public List<Library> getLibraries(String username) {
        String query = "SELECT * FROM libraries WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            List<Library> libraries = new ArrayList<>();

            while (rs.next()) {
                Library library = getLibrary(rs.getString("library_name"), rs.getString("username"));
                libraries.add(library);
            }

            return libraries;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null; // TODO: decidere se cambiare i null con liste vuote
        }
    }

    public List<Library> getLibraries(User user) {
        String query = "SELECT * FROM libraries WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUserId());

            ResultSet rs = stmt.executeQuery();

            List<Library> libraries = new ArrayList<>();

            while (rs.next()) {
                Library library = getLibrary(rs.getString("library_name"), rs.getString("username"));
                libraries.add(library);
            }

            return libraries;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    public boolean addLibrary(Library library) {
        return true;
    }

    public boolean updateLibrary(Library library) {
        return true;
    }

    public boolean removeLibrary(Library library) {
        return true;
    }
}
