package ONA.booksrecommender.objects;

import java.util.Arrays;
/**
 * La classe Rating rappresenta una valutazione di un libro da parte di un utente. 
 * La valutazione include criteri come stile, contenuto, divertimento, originalità e edizione, 
 * con un punteggio finale calcolato sulla base di questi parametri.
 */
public class Rating {
    private String userId;
    private String bookId;
    private int style;
    private int content;
    private int enjoyment;
    private int originality;
    private int edition;
    private int finalScore;
    private String notes;

    /**
     * Costruttore della classe Rating. Inizializza tutti i campi della valutazione e calcola il punteggio finale.
     *
     * @param userId L'ID dell'utente che ha fornito la valutazione.
     * @param bookId L'ID del libro valutato.
     * @param style Il punteggio assegnato allo stile del libro.
     * @param content Il punteggio assegnato al contenuto del libro.
     * @param enjoyment Il punteggio assegnato al grado di divertimento offerto dal libro.
     * @param originality Il punteggio assegnato all'originalità del libro.
     * @param edition Il punteggio assegnato alla qualità dell'edizione del libro.
     * @param notes Le eventuali note aggiuntive fornite dall'utente.
     */
    public Rating(String userId, String bookId, int style, int content, int enjoyment, 
                 int originality, int edition, String notes) {
        this.userId = userId;
        this.bookId = bookId;
        this.style = style;
        this.content = content;
        this.enjoyment = enjoyment;
        this.originality = originality;
        this.edition = edition;
        this.notes = notes;
        this.finalScore = calculateFinalScore();
    }

    /**
     * Calcola il punteggio finale come media dei singoli criteri di valutazione.
     *
     * @return Il punteggio finale arrotondato, calcolato come media di stile, contenuto, divertimento, originalità ed edizione.
     */
    private int calculateFinalScore() {
        return Math.round((style + content + enjoyment + originality + edition) / 5.0f);
    }

    // Getters

    /**
     * Restituisce l'ID dell'utente che ha fornito la valutazione.
     *
     * @return L'ID dell'utente.
     */
    public String getUserId() { return userId; }

    /**
     * Restituisce l'ID del libro valutato.
     *
     * @return L'ID del libro.
     */
    public String getBookId() { return bookId; }

    /**
     * Restituisce il punteggio assegnato allo stile del libro.
     *
     * @return Il punteggio dello stile.
     */
    public int getStyle() { return style; }

    /**
     * Restituisce il punteggio assegnato al contenuto del libro.
     *
     * @return Il punteggio del contenuto.
     */
    public int getContent() { return content; }

    /**
     * Restituisce il punteggio assegnato al divertimento offerto dal libro.
     *
     * @return Il punteggio del divertimento.
     */
    public int getEnjoyment() { return enjoyment; }

    /**
     * Restituisce il punteggio assegnato all'originalità del libro.
     *
     * @return Il punteggio dell'originalità.
     */
    public int getOriginality() { return originality; }

    /**
     * Restituisce il punteggio assegnato alla qualità dell'edizione del libro.
     *
     * @return Il punteggio dell'edizione.
     */
    public int getEdition() { return edition; }

    /**
     * Restituisce il punteggio finale calcolato sulla base dei singoli criteri.
     *
     * @return Il punteggio finale.
     */
    public int getFinalScore() { return finalScore; }

    /**
     * Restituisce le note aggiuntive fornite dall'utente nella valutazione.
     *
     * @return Le note dell'utente.
     */
    public String getNotes() { return notes; }

    /**
     * Converte l'oggetto Rating in una stringa CSV, dove i campi sono separati da virgole.
     *
     * @return Una stringa CSV che rappresenta la valutazione.
     */
    public String toCsvString() {
        return String.join(",", Arrays.asList(
            userId,
            bookId,
            String.valueOf(style),
            String.valueOf(content),
            String.valueOf(enjoyment),
            String.valueOf(originality),
            String.valueOf(edition),
            String.valueOf(finalScore),
            notes
        ));
    }

    /**
     * Crea un nuovo oggetto Rating a partire da una stringa CSV. 
     * La stringa CSV deve contenere i dettagli della valutazione, con i campi separati da virgole.
     *
     * @param csv La stringa CSV che rappresenta i dettagli della valutazione.
     * @return Un nuovo oggetto Rating.
     */
    public static Rating fromCsvString(String csv) {
        String[] parts = csv.split(",");
        return new Rating(
            parts[0],
            parts[1],
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4]),
            Integer.parseInt(parts[5]),
            Integer.parseInt(parts[6]),
            parts.length > 8 ? parts[8]:""
        );
    }
}