package ONA.booksrecommender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * La classe Book rappresenta un libro con dettagli quali titolo, autori, anno di pubblicazione, 
 * casa editrice e categoria. Permette di creare un oggetto Book da un formato CSV e di esportarlo 
 * come stringa CSV.
 */
public class Book {
    private String id;
    private String title; // 0
    private List<String> authors; // 1
    private int publicationYear; // 7
    private String publisher; // 4
    private String category; // 3

    /**
     * Costruttore della classe Book. Inizializza tutti i campi del libro.
     *
     * @param id L'identificativo del libro.
     * @param title Il titolo del libro.
     * @param authors La lista degli autori del libro.
     * @param publicationYear L'anno di pubblicazione del libro.
     * @param publisher La casa editrice del libro.
     * @param category La categoria del libro.
     */

    public Book(String id, String title, List<String> authors, int publicationYear, String publisher, String category) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.publicationYear = publicationYear;
        this.publisher = publisher;
        this.category = category;
    }

    // Getters

    /**
     * Restituisce l'ID del libro.
     *
     * @return L'identificativo del libro.
     */
    public String getId() { return id; }
    /**
     * Restituisce il titolo del libro.
     *
     * @return Il titolo del libro.
     */
    public String getTitle() { return title; }
    /**
     * Restituisce una copia della lista degli autori del libro.
     *
     * @return Una lista degli autori del libro.
     */
    public List<String> getAuthors() { return new ArrayList<>(authors); }
    /**
     * Restituisce l'anno di pubblicazione del libro.
     *
     * @return L'anno di pubblicazione del libro.
     */
    public int getPublicationYear() { return publicationYear; }
    /**
     * Restituisce la casa editrice del libro.
     *
     * @return La casa editrice del libro.
     */
    public String getPublisher() { return publisher; }
    /**
     * Restituisce la categoria del libro.
     *
     * @return La categoria del libro.
     */
    public String getCategory() { return category; }

    /**
     * Converte l'oggetto Book in una stringa CSV, dove i campi sono separati da virgole.
     * Gli autori sono separati da punto e virgola.
     *
     * @return Una stringa CSV che rappresenta il libro.
     */
    public String toCsvString() {
        return String.join(",", Arrays.asList(
            id,
            title,
            String.join(";", authors),
            String.valueOf(publicationYear),
            publisher,
            category
        ));
    }

    /**
     * Crea un nuovo oggetto Book a partire da una stringa CSV. La stringa CSV deve contenere
     * i dettagli del libro, con i campi separati da virgole. Gli autori devono essere separati da punto e virgola.
     *
     * @param csv La stringa CSV che rappresenta i dettagli del libro.
     * @param bookCount Un contatore che rappresenta l'ID del libro.
     * @return Un nuovo oggetto Book.
     */
    public static Book fromCsvString(String csv, int bookCount) {
        List<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentPart = new StringBuilder();

        for (char c : csv.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(currentPart.toString().trim());
                currentPart.setLength(0);
            } else {
                currentPart.append(c);
            }
        }
        parts.add(currentPart.toString().trim());

        return new Book(String.valueOf(bookCount), parts.get(0),
                Arrays.asList(parts.get(1).split(";")),
                Integer.parseInt(parts.get(7)),
                parts.get(4),
                parts.get(3));
    }

    /**
     * Restituisce una rappresentazione in formato stringa dell'oggetto, 
     * contenente il titolo, gli autori e l'anno di pubblicazione del libro.
     *
     * @return Una stringa formattata che include il titolo del libro, gli autori 
     *         separati da virgola e l'anno di pubblicazione nel seguente formato:
     *         "titolo by autore1, autore2, ... (anno)".
     */
    @Override
    public String toString() {
        return String.format("%s by %s (%d)", title, String.join(", ", authors), publicationYear);
    }
}