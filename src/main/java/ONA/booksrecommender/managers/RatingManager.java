package ONA.booksrecommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce le valutazioni dei libri da parte degli utenti.
 * Le valutazioni sono memorizzate in un file CSV e organizzate per libro.
 */
public class RatingManager {
    private Map<String, List<Rating>> bookRatings = new HashMap<>();
    private static final String RATINGS_FILE = "../data/ValutazioniLibri.csv";

    /**
     * Carica le valutazioni dei libri dal file CSV.
     * Ogni riga del file viene convertita in un'istanza di `Rating` e aggiunta alla mappa delle valutazioni.
     *
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public void loadRatings() throws IOException {
        List<String> lines = FileUtils.readLines(RATINGS_FILE);
        for (String line : lines) {
            Rating rating = Rating.fromCsvString(line);
            bookRatings.computeIfAbsent(rating.getBookId(), k -> new ArrayList<>())
                      .add(rating);
        }
    }

    /**
     * Aggiunge una valutazione per un libro.
     * Se l'utente ha già valutato il libro, la valutazione precedente viene sostituita.
     * La valutazione viene poi aggiornata nel file CSV.
     *
     * @param rating la valutazione da aggiungere.
     * @throws IOException se si verifica un errore durante l'aggiornamento del file.
     */
    public void addRating(Rating rating) throws IOException {
        // Verifica se l'utente ha già valutato questo libro
        List<Rating> ratings = bookRatings.computeIfAbsent(rating.getBookId(), k -> new ArrayList<>());
        ratings.removeIf(r -> r.getUserId().equals(rating.getUserId()));
        
        ratings.add(rating);
        
        updateRatingsFile();
    }

    /**
     * Aggiorna il file CSV delle valutazioni con i dati correnti.
     *
     * @throws IOException se si verifica un errore durante la scrittura del file.
     */
    private void updateRatingsFile() throws IOException {
        List<String> lines = new ArrayList<>();
        for (List<Rating> ratings : bookRatings.values()) {
            for (Rating rating : ratings) {
                lines.add(rating.toCsvString());
            }
        }
        FileUtils.writeLines(RATINGS_FILE, lines);
    }

    /**
     * Restituisce tutte le valutazioni di un libro.
     * Se il libro non ha valutazioni, viene restituita una lista vuota.
     *
     * @param bookId l'ID del libro.
     * @return una lista di valutazioni del libro.
     */
    public List<Rating> getBookRatings(String bookId) {
        return bookRatings.getOrDefault(bookId, new ArrayList<>());
    }

    /**
     * Calcola la valutazione media per ciascun criterio (stile, contenuto, godibilità, originalità, edizione, punteggio finale)
     * per un determinato libro.
     *
     * @param bookId l'ID del libro.
     * @return una mappa con le valutazioni medie per ciascun criterio.
     */
    public Map<String, Double> getAggregateRatings(String bookId) {
        List<Rating> ratings = getBookRatings(bookId);
        Map<String, Double> aggregates = new HashMap<>();
        
        if (ratings.isEmpty()) {
            return aggregates;
        }

        aggregates.put("style", ratings.stream().mapToInt(Rating::getStyle).average().orElse(0));
        aggregates.put("content", ratings.stream().mapToInt(Rating::getContent).average().orElse(0));
        aggregates.put("enjoyment", ratings.stream().mapToInt(Rating::getEnjoyment).average().orElse(0));
        aggregates.put("originality", ratings.stream().mapToInt(Rating::getOriginality).average().orElse(0));
        aggregates.put("edition", ratings.stream().mapToInt(Rating::getEdition).average().orElse(0));
        aggregates.put("finalScore", ratings.stream().mapToInt(Rating::getFinalScore).average().orElse(0));
        
        return aggregates;
    }

    /**
     * Verifica se un determinato utente ha già valutato un libro.
     *
     * @param userId l'ID dell'utente.
     * @param bookId l'ID del libro.
     * @return true se l'utente ha già valutato il libro, false altrimenti.
     */
    public boolean hasUserRated(String userId, String bookId) {
        return bookRatings.getOrDefault(bookId, new ArrayList<>()).stream()
            .anyMatch(rating -> rating.getUserId().equals(userId));
    }
}