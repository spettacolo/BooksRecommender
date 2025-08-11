package ONA.booksrecommender.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rappresenta una biblioteca di libri associata a un utente. La biblioteca è identificata da un nome
 * e un identificatore utente, e contiene un elenco di ID di libri.
 */
public class Library {
    private String name;
    private String userId;
    private List<String> bookIds; // TODO: cambiare da lista di String a lista di Book

    /**
     * Costruisce una nuova biblioteca con il nome, l'ID dell'utente e un elenco di ID di libri.
     *
     * @param name il nome della biblioteca.
     * @param userId l'ID dell'utente associato alla biblioteca.
     * @param bookIds la lista degli ID dei libri contenuti nella biblioteca.
     */
    public Library(String name, String userId, List<String> bookIds) {
        this.name = name;
        this.userId = userId;
        this.bookIds = bookIds;
    }

    // Getters
    /**
     * Restituisce il nome della biblioteca.
     *
     * @return il nome della biblioteca.
     */
    public String getName() { return name; }

    /**
     * Restituisce l'ID dell'utente associato alla biblioteca.
     *
     * @return l'ID dell'utente.
     */
    public String getUserId() { return userId; }

    /**
     * Restituisce una copia della lista degli ID dei libri contenuti nella biblioteca.
     *
     * @return una lista degli ID dei libri.
     */
    public List<String> getBookIds() { return new ArrayList<>(bookIds); }

    /**
     * Aggiunge un ID di libro alla biblioteca, se non è già presente.
     *
     * @param bookId l'ID del libro da aggiungere alla biblioteca.
     */
    public void addBook(String bookId) {
        if (!bookIds.contains(bookId)) {
            bookIds.add(bookId);
        }
    }
}