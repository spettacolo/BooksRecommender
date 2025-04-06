package ONA.booksrecommender;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La classe BookManager gestisce un insieme di libri. 
 * Permette il caricamento, l'aggiunta, la ricerca e l'accesso ai libri in base a criteri specifici.
 */
public class BookManager {
    private Map<String, Book> books = new HashMap<>();
    private static final String BOOKS_FILE = "../data/Libri.csv";

    /**
     * Carica i libri da un file CSV e li memorizza nella mappa dei libri.
     * Il file deve essere in formato CSV con una riga di intestazione.
     * Ogni riga del file rappresenta un libro.
     * 
     * @throws IOException Se si verifica un errore di lettura del file.
     */
    public void loadBooks() throws IOException {
        List<String> lines = FileUtils.readLines(BOOKS_FILE);
        boolean isFirstLine = true; // Per ignorare la riga di intestazione
        int bookCount;
        for (String line : lines) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Salta la prima riga
            }
            bookCount = getBookCount() + 1;
            Book book = Book.fromCsvString(line, bookCount);
            books.put(book.getId(), book);
        }
        // System.out.println("Caricati " + books.size() + " libri");
    }
    

    /**
     * Aggiunge un nuovo libro alla mappa dei libri e lo scrive nel file CSV.
     * 
     * @param book Il libro da aggiungere.
     * @throws IOException Se si verifica un errore di scrittura nel file.
     */
    public void addBook(Book book) throws IOException {
        books.put(book.getId(), book);
        FileUtils.appendLine(BOOKS_FILE, book.toCsvString());
    }

    /**
     * Restituisce un libro con l'ID specificato.
     * 
     * @param id L'ID del libro.
     * @return Il libro corrispondente all'ID o null se non esiste.
     */
    public Book getBook(String id) {
        return books.get(id);
    }

    /**
     * Cerca i libri che contengono un termine specifico nel titolo.
     * La ricerca non è case-sensitive.
     * 
     * @param title Il titolo o una parte del titolo da cercare.
     * @return Una lista di libri che contengono il termine nel titolo.
     */
    public List<Book> searchByTitle(String title) {
        String searchTerm = title.toLowerCase();
        return books.values().stream()
                   .filter(b -> b.getTitle().toLowerCase().contains(searchTerm))
                   .toList();
    }

    /**
     * Cerca i libri che contengono un autore specifico.
     * La ricerca non è case-sensitive.
     * 
     * @param author Il nome dell'autore o una parte di esso da cercare.
     * @return Una lista di libri che contengono il termine nell'elenco degli autori.
     */
    public List<Book> searchByAuthor(String author) {
        String searchTerm = author.toLowerCase();
        return books.values().stream()
                   .filter(b -> b.getAuthors().stream()
                   .anyMatch(a -> a.toLowerCase().contains(searchTerm)))
                   .toList();
    }

    /**
     * Cerca i libri che contengono un autore specifico e sono stati pubblicati in un determinato anno.
     * La ricerca non è case-sensitive.
     * 
     * @param author Il nome dell'autore o una parte di esso da cercare.
     * @param year L'anno di pubblicazione del libro.
     * @return Una lista di libri che soddisfano i criteri di ricerca per autore e anno.
     */
    public List<Book> searchByAuthorAndYear(String author, int year) {
        String searchTerm = author.toLowerCase();
        return books.values().stream()
                   .filter(b -> b.getAuthors().stream()
                   .anyMatch(a -> a.toLowerCase().contains(searchTerm))
                   && b.getPublicationYear() == year)
                   .toList();
    }

    /**
     * Restituisce tutti i libri gestiti dalla classe BookManager.
     * 
     * @return Una collezione di tutti i libri.
     */
    public Collection<Book> getAllBooks() {
        return books.values();
    }

    /**
     * Restituisce il numero totale di libri gestiti.
     * 
     * @return Il numero di libri presenti nella mappa.
     */
    public int getBookCount() {
        return books.size();
    }
}