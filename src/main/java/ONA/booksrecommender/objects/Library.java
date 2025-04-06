package ONA.booksrecommender;

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
    private List<String> bookIds;

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

    /**
     * Converte la biblioteca in una stringa in formato CSV.
     * La stringa contiene il nome, l'ID utente e gli ID dei libri separati da una virgola e un punto e virgola.
     *
     * @return una stringa rappresentante la biblioteca in formato CSV.
     */
    public String toCsvString() {
        return String.join(",", Arrays.asList(
            name,
            userId,
            String.join(";", bookIds)
        ));
    }

    /**
     * Crea una nuova istanza di `Library` a partire da una stringa in formato CSV.
     * La stringa deve contenere il nome della biblioteca, l'ID dell'utente e gli ID dei libri separati da virgole
     * e punti e virgola.
     *
     * @param csv la stringa in formato CSV contenente i dati della biblioteca.
     * @return una nuova istanza di `Library`.
     */
    public static Library fromCsvString(String csv) {
        String[] parts = csv.split(",");
        List<String> bookIds = parts.length > 2 ? Arrays.asList(parts[2].split(";")) : new ArrayList<>();
        return new Library(
            parts[0],
            parts[1],
            bookIds
        );
    }
}