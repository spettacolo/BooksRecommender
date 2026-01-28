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

    /**
     * Costruttore della classe LibraryDAO.
     * Inizializza l'accesso ai dati per le librerie e riceve un'istanza di {@link BookDAO}
     * per popolare gli oggetti {@link Library} con i relativi oggetti {@link Book} completi.
     *
     * @param logger     L'istanza di {@link Logger} per il tracciamento delle operazioni SQL.
     * @param connection La connessione attiva al database.
     * @param bookDAO    Il DAO dei libri necessario per la composizione degli oggetti.
     */
    public LibraryDAO(Logger logger, Connection connection, BookDAO bookDAO) {
        super(logger, connection);
        this.bookDAO = bookDAO;
    }

    /**
     * Recupera una libreria completa partendo dal suo ID univoco.
     * Il processo avviene in due fasi:
     * <ol>
     * <li>Recupero di tutti i {@code book_id} associati nella tabella di giunzione e successiva
     * istanziazione degli oggetti {@link Book} tramite il {@link BookDAO}.</li>
     * <li>Recupero dei metadati della libreria (nome e proprietario) dalla tabella {@code libraries}.</li>
     * </ol>
     *
     * @param id L'identificativo della libreria.
     * @return Un oggetto {@link Library} popolato, o {@code null} se non trovato o in caso di errore.
     */
    public Library getLibrary(int id) {
        List<Book> books = new ArrayList<>();

        // 1. Recupero dei libri
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

        // 2. Recupero dei dettagli della libreria
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

    /**
     * Cerca una libreria specifica basandosi sul nome e sullo username del proprietario.
     *
     * @param name     Il nome della libreria.
     * @param username Lo username dell'utente.
     * @return L'oggetto {@link Library} corrispondente, recuperato con tutti i suoi libri.
     */
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

    /**
     * Recupera l'elenco di tutte le librerie appartenenti a un determinato utente.
     *
     * @param username Lo username dell'utente.
     * @return Una lista di oggetti {@link Library}.
     */
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
            return new ArrayList<>();
        }
    }

    /**
     * Crea una nuova libreria nel database.
     *
     * @param library  Il nome della libreria da creare.
     * @param username Lo username del proprietario.
     * @return {@code true} se l'inserimento è andato a buon fine, {@code false} altrimenti.
     */
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

    /**
     * Aggiorna i dati identificativi di una libreria esistente.
     *
     * @param library        La versione attuale della libreria (usata per l'ID).
     * @param updatedLibrary La versione contenente i nuovi dati da salvare.
     * @return {@code true} se l'aggiornamento ha coinvolto almeno una riga, {@code false} altrimenti.
     */
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

    /**
     * Elimina definitivamente una libreria dal database.
     * Nota: I vincoli di integrità referenziale nel DB dovrebbero gestire la rimozione
     * a cascata dei riferimenti nella tabella di giunzione.
     *
     * @param library L'oggetto {@link Library} da rimuovere.
     * @return {@code true} se l'eliminazione ha avuto successo.
     */
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

    /**
     * Associa un libro a una specifica libreria inserendo un record nella tabella di giunzione.
     *
     * @param book    L'oggetto {@link Book} da aggiungere.
     * @param library L'oggetto {@link Library} di destinazione.
     * @return {@code true} se il libro è stato collegato con successo.
     */
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

    /**
     * Rimuove il collegamento tra un libro e una libreria specifica.
     * Non elimina il libro dal database globale, ma solo dalla collezione indicata.
     *
     * @param book    L'oggetto {@link Book} da scollegare.
     * @param library L'oggetto {@link Library} da cui rimuoverlo.
     * @return {@code true} se il record è stato rimosso dalla tabella di giunzione.
     */
    public boolean removeBook(Book book, Library library) {
        String query = "DELETE FROM library_books WHERE book_id = ? AND library_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, book.getId());
            stmt.setInt(2, library.getId());

            int rows = stmt.executeUpdate();
            return rows >= 1;
        } catch (SQLException e) {
            logger.log("Error removing book from library: " + e.getMessage());
            return false;
        }
    }
}