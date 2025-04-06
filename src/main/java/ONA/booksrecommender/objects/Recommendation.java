package ONA.booksrecommender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rappresenta una raccomandazione di libri per un determinato utente.
 * Un utente può ricevere al massimo 3 libri consigliati per ciascun libro già letto.
 */
public class Recommendation {
    private String userId;
    private String bookId;
    private List<String> recommendedBookIds;

    /**
     * Crea una nuova raccomandazione per un utente, specificando il libro per cui si consiglia
     * una lista di libri.
     * La lista di libri consigliati non può contenere più di 3 libri.
     *
     * @param userId l'ID dell'utente che riceve la raccomandazione.
     * @param bookId l'ID del libro per cui vengono fatti i consigli.
     * @param recommendedBookIds la lista di ID dei libri consigliati.
     * @throws IllegalArgumentException se la lista di libri consigliati contiene più di 3 libri.
     */
    public Recommendation(String userId, String bookId, List<String> recommendedBookIds) {
        this.userId = userId;
        this.bookId = bookId;
        this.recommendedBookIds = recommendedBookIds;
        if (recommendedBookIds.size() > 3) {
            throw new IllegalArgumentException("Cannot recommend more than 3 books");
        }
    }

    /**
     * Restituisce l'ID dell'utente a cui viene fatta la raccomandazione.
     *
     * @return l'ID dell'utente.
     */
    public String getUserId() { return userId; }

    /**
     * Restituisce l'ID del libro per cui viene fatta la raccomandazione.
     *
     * @return l'ID del libro.
     */
    public String getBookId() { return bookId; }

    /**
     * Restituisce la lista di ID dei libri consigliati.
     *
     * @return la lista di ID dei libri consigliati.
     */
    public List<String> getRecommendedBookIds() { return new ArrayList<>(recommendedBookIds); }

    /**
     * Converte la raccomandazione in una stringa CSV.
     * La stringa risultante avrà il formato: "userId,bookId,recommendedBookId1;recommendedBookId2;recommendedBookId3".
     *
     * @return una stringa CSV che rappresenta la raccomandazione.
     */
    public String toCsvString() {
        return String.join(",", Arrays.asList(
            userId,
            bookId,
            String.join(";", recommendedBookIds)
        ));
    }

    /**
     * Crea una nuova raccomandazione a partire da una stringa CSV.
     * La stringa deve avere il formato: "userId,bookId,recommendedBookId1;recommendedBookId2;recommendedBookId3".
     *
     * @param csv la stringa CSV che rappresenta la raccomandazione.
     * @return un'istanza di `Recommendation` creata a partire dalla stringa CSV.
     */
    public static Recommendation fromCsvString(String csv) {
        String[] parts = csv.split(",");
        return new Recommendation(
            parts[0],
            parts[1],
            Arrays.asList(parts[2].split(";"))
        );
    }
}