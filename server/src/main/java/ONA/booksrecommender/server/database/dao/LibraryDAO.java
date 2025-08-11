package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.objects.Library;
import ONA.booksrecommender.objects.User;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.util.List;

public class LibraryDAO extends BaseDAO implements AutoCloseable {
    public LibraryDAO(Logger logger, Connection connection) {
        super(logger, connection); // crea la connessione nel costruttore di BaseDAO
    }

    public Library getLibrary(String libraryName, String username) {
        return null;
    }

    // Nota: potrebbe restituire una lista di librerie, motivo per cui non sarà getLibrary bensì getLibraries
    public List<Library> getLibraries(Book book) {
        return null;
    }

    // Probabilmente ridondante ma utile nel caso si ha solo l'username, si risparmia una richiesta inutile, decidere se tenerle entrambe o meno
    public List<Library> getLibraries(String username) {
        return null;
    }

    public List<Library> getLibraries(User user) {
        return null;
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
