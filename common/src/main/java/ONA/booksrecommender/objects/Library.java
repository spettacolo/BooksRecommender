package ONA.booksrecommender.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rappresenta una biblioteca di libri associata a un utente. La biblioteca è identificata da un nome
 * e un identificatore utente, e contiene un elenco di ID di libri.
 */
public class Library {
    private int id;
    private String name;
    private String userId;
    private List<Book> books;

    /**
     * Costruisce una nuova biblioteca con il nome, l'ID dell'utente e un elenco di ID di libri.
     *
     * @param id l'id della libreria
     * @param name il nome della libreria.
     * @param userId l'ID dell'utente associato alla libreria.
     * @param books la lista dei libri contenuti nella libreria.
     */
    public Library(int id, String name, String userId, List<Book> books) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.books = books;
    }

    // Getters
    /**
     * Restituisce l'id della libreria.
     *
     * @return il nome della libreria.
     */
    public int getId() { return id; }

    /**
     * Restituisce il nome della libreria.
     *
     * @return il nome della libreria.
     */
    public String getName() { return name; }

    /**
     * Restituisce l'ID dell'utente associato alla libreria.
     *
     * @return l'ID dell'utente.
     */
    public String getUserId() { return userId; }

    /**
     * Restituisce una copia della lista degli ID dei libri contenuti nella libreria.
     *
     * @return una lista degli ID dei libri.
     */
    public List<Integer> getBookIds() {
        List<Integer> bookIds = new ArrayList<>();
        for (Book book : books) {
            bookIds.add(book.getId());
        }
        return bookIds;
    }

    /**
     * Aggiunge un ID di libro alla libreria, se non è già presente.
     *
     * @param book il libro da aggiungere alla libreria.
     */
    /*public void addBook(String bookId) {
        if (!bookIds.contains(bookId)) {
            bookIds.add(bookId);
        }
    }*/
    public void addBook(Book book) {
        if (!books.contains(book)) {
            books.add(book);
        }
    }
}