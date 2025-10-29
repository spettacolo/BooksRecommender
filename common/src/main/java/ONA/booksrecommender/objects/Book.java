package ONA.booksrecommender.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * La classe Book rappresenta un libro con dettagli quali titolo, autori, anno di pubblicazione, 
 * casa editrice e categoria. Permette di creare un oggetto Book da un formato CSV e di esportarlo 
 * come stringa CSV.
 */
public class Book {
    private int id;
    private String title; // 0
    private List<String> authors; // 1
    //private String description;
    private int publicationYear; // 7
    private String publisher; // 4
    private String category; // 3
    private String coverImageUrl;

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

    public Book(int id, String title, List<String> authors, int publicationYear, String publisher, String category, String coverImageUrl) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.publicationYear = publicationYear;
        this.publisher = publisher;
        this.category = category;
        this.coverImageUrl = coverImageUrl;
    }

    // Getters

    /**
     * Restituisce l'ID del libro.
     *
     * @return L'identificativo del libro.
     */
    public int getId() { return id; }
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
     * Restituisce la copertina del libro.
     *
     * @return La copertina del libro.
     */
    public String getCoverImageUrl() { return coverImageUrl; }

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
        return String.format("%s by %s (%d)", title, String.join(", ", authors), publicationYear, coverImageUrl);
    }
}