package ONA.booksrecommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce le librerie degli utenti, permettendo di caricarle, crearle, aggiungere libri e verificarne la presenza.
 * Le librerie sono salvate in un file CSV.
 */
public class LibraryManager {
    private Map<String, List<Library>> userLibraries = new HashMap<>();
    private static final String LIBRARIES_FILE = "../data/Librerie.csv";

    /**
     * Carica le librerie degli utenti dal file CSV.
     * Ogni riga del file viene convertita in un'istanza di `Library` e aggiunta alla mappa delle librerie.
     *
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public void loadLibraries() throws IOException {
        List<String> lines = FileUtils.readLines(LIBRARIES_FILE);
        for (String line : lines) {
            Library library = Library.fromCsvString(line);
            userLibraries.computeIfAbsent(library.getUserId(), k -> new ArrayList<>())
                        .add(library);
        }
    }

    /**
     * Crea una nuova libreria e la aggiunge alla mappa delle librerie.
     * La libreria viene anche aggiunta al file CSV.
     *
     * @param library la libreria da creare.
     * @throws IOException se si verifica un errore durante la scrittura nel file.
     */
    public void createLibrary(Library library) throws IOException {
        userLibraries.computeIfAbsent(library.getUserId(), k -> new ArrayList<>())
                    .add(library);
        FileUtils.appendLine(LIBRARIES_FILE, library.toCsvString());
    }

    /**
     * Restituisce tutte le librerie associate a un determinato utente.
     * Se l'utente non ha librerie, viene restituita una lista vuota.
     *
     * @param userId l'ID dell'utente.
     * @return una lista di librerie dell'utente.
     */
    public List<Library> getUserLibraries(String userId) {
        return userLibraries.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Aggiunge un libro a una libreria di un determinato utente.
     * Se la libreria non esiste, viene sollevata un'eccezione.
     *
     * @param userId l'ID dell'utente.
     * @param libraryName il nome della libreria.
     * @param bookId l'ID del libro da aggiungere.
     * @throws IOException se si verifica un errore durante l'aggiornamento del file.
     * @throws IllegalArgumentException se la libreria non viene trovata.
     */
    public void addBookToLibrary(String userId, String libraryName, String bookId) throws IOException {
        List<Library> libraries = getUserLibraries(userId);
        for (Library library : libraries) {
            if (library.getName().equals(libraryName)) {
                library.addBook(bookId);
                updateLibrariesFile();
                return;
            }
        }
        throw new IllegalArgumentException("Library not found");
    }

    /**
     * Aggiorna il file CSV delle librerie con i dati correnti.
     *
     * @throws IOException se si verifica un errore durante la scrittura del file.
     */
    private void updateLibrariesFile() throws IOException {
        List<String> lines = new ArrayList<>();
        for (List<Library> userLibs : userLibraries.values()) {
            for (Library library : userLibs) {
                lines.add(library.toCsvString());
            }
        }
        FileUtils.writeLines(LIBRARIES_FILE, lines);
    }

    /**
     * Verifica se un determinato libro è presente nelle librerie di un utente.
     *
     * @param userId l'ID dell'utente.
     * @param bookId l'ID del libro da cercare.
     * @return true se il libro è presente, false altrimenti.
     */
    public boolean hasBook(String userId, String bookId) {
        return getUserLibraries(userId).stream()
            .anyMatch(library -> library.getBookIds().contains(bookId));
    }
}