package ONA.booksrecommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce le raccomandazioni di libri per utenti specifici.
 * Permette di caricare, aggiungere e recuperare raccomandazioni da un file CSV.
 */
public class RecommendationManager {
    private Map<String, List<Recommendation>> bookRecommendations = new HashMap<>();
    private static final String RECOMMENDATIONS_FILE = "../data/ConsigliLibri.csv";

    /**
     * Carica le raccomandazioni di libri dal file CSV.
     * Ogni riga del file viene trasformata in un'istanza di `Recommendation` e aggiunta alla mappa.
     *
     * @throws IOException se si verificano errori nella lettura del file.
     */
    public void loadRecommendations() throws IOException {
        List<String> lines = FileUtils.readLines(RECOMMENDATIONS_FILE);
        for (String line : lines) {
            Recommendation recommendation = Recommendation.fromCsvString(line);
            bookRecommendations.computeIfAbsent(recommendation.getBookId(), k -> new ArrayList<>())
                              .add(recommendation);
        }
    }

    /**
     * Aggiunge una raccomandazione di libro alla mappa.
     * Se esiste già una raccomandazione per lo stesso libro e lo stesso utente, viene sovrascritta.
     * Dopo l'aggiunta, le raccomandazioni vengono scritte nel file CSV.
     *
     * @param recommendation la raccomandazione da aggiungere.
     * @throws IOException se si verificano errori nella scrittura del file.
     */
    public void addRecommendation(Recommendation recommendation) throws IOException {
        // Rimuovi eventuali raccomandazioni precedenti dello stesso utente per lo stesso libro
        List<Recommendation> recommendations = bookRecommendations.computeIfAbsent(
            recommendation.getBookId(), k -> new ArrayList<>());
        recommendations.removeIf(r -> r.getUserId().equals(recommendation.getUserId()));
        
        // Aggiungi la nuova raccomandazione
        recommendations.add(recommendation);
        
        // Aggiorna il file
        updateRecommendationsFile();
    }

    /**
     * Aggiorna il file CSV con le raccomandazioni correnti.
     *
     * @throws IOException se si verificano errori nella scrittura del file.
     */
    private void updateRecommendationsFile() throws IOException {
        List<String> lines = new ArrayList<>();
        for (List<Recommendation> recommendations : bookRecommendations.values()) {
            for (Recommendation recommendation : recommendations) {
                lines.add(recommendation.toCsvString());
            }
        }
        FileUtils.writeLines(RECOMMENDATIONS_FILE, lines);
    }

    /**
     * Restituisce le raccomandazioni di un libro specifico.
     *
     * @param bookId l'ID del libro di cui si vogliono recuperare le raccomandazioni.
     * @return una lista di raccomandazioni per il libro specificato.
     */
    public List<Recommendation> getBookRecommendations(String bookId) {
        return bookRecommendations.getOrDefault(bookId, new ArrayList<>());
    }

    /**
     * Restituisce i libri più raccomandati in relazione a un libro specifico.
     * Viene restituito un mapping dei libri consigliati con il conteggio di quante volte
     * sono stati raccomandati in relazione al libro specificato.
     *
     * @param bookId l'ID del libro per cui si vogliono ottenere i libri consigliati.
     * @return una mappa contenente i libri consigliati e il loro conteggio.
     */
    public Map<String, Integer> getTopRecommendedBooks(String bookId) {
        List<Recommendation> recommendations = getBookRecommendations(bookId);
        Map<String, Integer> recommendationCounts = new HashMap<>();
        
        for (Recommendation recommendation : recommendations) {
            for (String recommendedBookId : recommendation.getRecommendedBookIds()) {
                recommendationCounts.merge(recommendedBookId, 1, Integer::sum);
            }
        }
        
        return recommendationCounts;
    }

    /**
     * Verifica se un utente ha già fatto una raccomandazione per un libro specifico.
     *
     * @param userId l'ID dell'utente.
     * @param bookId l'ID del libro.
     * @return true se l'utente ha già fatto una raccomandazione per il libro, altrimenti false.
     */
    public boolean hasUserRecommended(String userId, String bookId) {
        return bookRecommendations.getOrDefault(bookId, new ArrayList<>()).stream()
            .anyMatch(recommendation -> recommendation.getUserId().equals(userId));
    }
}