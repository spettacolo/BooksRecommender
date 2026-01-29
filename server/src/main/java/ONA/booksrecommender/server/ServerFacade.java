package ONA.booksrecommender.server;

import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.utils.Logger;
import ONA.booksrecommender.objects.User;
import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.objects.Library;
import ONA.booksrecommender.objects.Rating;
import ONA.booksrecommender.objects.Recommendation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Implementazione del pattern Facade per la gestione delle richieste dei client.
 * Questa classe funge da punto di smistamento centrale: interpreta i messaggi di testo
 * ricevuti tramite socket, estrae i parametri e delega l'esecuzione delle operazioni
 * ai rispettivi DAO (Data Access Objects). Si occupa inoltre della serializzazione
 * dei risultati in stringhe compatibili con il protocollo di comunicazione del sistema.
 */
public class ServerFacade {
    private static final String SEPARATOR = ";";
    private static final String ERROR_MESSAGE = "ERROR" + SEPARATOR + "missing_params";
    private final Logger logger;
    private final Database database;

    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final LibraryDAO libraryDAO;
    private final RatingDAO ratingDAO;
    private final RecommendationDAO recommendationDAO;

    /**
     * Costruttore della classe. Inizializza i riferimenti ai DAO necessari
     * recuperandoli dall'istanza del database fornita.
     * * @param logger   L'istanza di {@link Logger} per tracciare le richieste elaborate.
     * @param database L'istanza di {@link Database} da cui ottenere i DAO.
     */
    public ServerFacade(Logger logger, Database database) {
        this.logger = logger;
        this.database = database;

        this.userDAO = database.getDAO(UserDAO.class);
        this.bookDAO = database.getDAO(BookDAO.class);
        this.libraryDAO = database.getDAO(LibraryDAO.class);
        this.ratingDAO = database.getDAO(RatingDAO.class);
        this.recommendationDAO = database.getDAO(RecommendationDAO.class);
    }

    /**
     * Metodo di utilità per serializzare una lista di raccomandazioni in un formato stringa.
     * Il formato risultante utilizza delimitatori specifici per separare gli ID dei libri
     * consigliati e le diverse raccomandazioni.
     * * @param list La lista di oggetti {@link Recommendation} da formattare.
     * @return Una stringa formattata pronta per la trasmissione via socket.
     */
    private String formatRecommendationList(List<Recommendation> list) {
        StringBuilder sb = new StringBuilder();
        for (Recommendation rec : list) {
            String recs = String.join(",", rec.getRecommendedBookIds());
            sb.append(rec.getBookId()).append(SEPARATOR).append(recs).append("|");
        }
        return sb.toString();
    }

    /**
     * Metodo principale di elaborazione delle richieste.
     * Analizza il comando contenuto nella stringa {@code req} (basata sul delimitatore SEPARATOR)
     * ed esegue l'azione corrispondente (login, ricerca libri, gestione librerie, recensioni, ecc.).
     * Gestisce inoltre la codifica Base64 per i campi testuali che potrebbero contenere
     * caratteri speciali, garantendo l'integrità del protocollo.
     * * @param req La stringa grezza ricevuta dal client.
     * @return La stringa di risposta da inviare al client, contenente i dati richiesti o un codice di errore.
     */
    public String handleRequest(String req) {
        String[] parts = req.split(SEPARATOR);
        try {
            String cmd = parts[0];
            switch (cmd) {
                case "get_user": {
                    if (parts.length < 2) return ERROR_MESSAGE;
                    String username = parts[1];
                    User user = userDAO.getUser(username, false);
                    if (user == null) return "NOT_FOUND";
                    // serializzazione semplice: username;name;surname;email
                    return String.join(SEPARATOR, user.getUserId(), user.getName(), user.getSurname(), user.getEmail());
                }
                case "login": {
                    if (parts.length < 3) return ERROR_MESSAGE;
                    String username = parts[1];
                    String password = parts[2];
                    int code = userDAO.login(username, password);
                    // codice: 0 success, -2 wrong password, -3 error or not found
                    return "LOGIN" + SEPARATOR + Integer.toString(code);
                }
                case "sign_up": {
                    if (parts.length < 7) return ERROR_MESSAGE;
                    String username = parts[1];
                    String name = parts[2];
                    String surname = parts[3];
                    String fiscalCode = parts[4];
                    String email = parts[5];
                    String password = parts[6];
                    boolean ok = userDAO.signUpUser(username, name, surname, fiscalCode, email, password);
                    return ok ? "SIGNUP" + SEPARATOR + "OK" : "SIGNUP" + SEPARATOR + "FAIL";
                }
                case "get_book": {
                    if (parts.length < 3) return ERROR_MESSAGE;
                    switch (parts[1]) {
                        case "id": {
                            Book book = bookDAO.getBook(Integer.parseInt(parts[2]));
                            logger.log(book.toString());
                            List<String> authors = book.getAuthors();
                            String authorsString = String.join(", ", authors);
                            String encodedDesc = Base64.getEncoder().encodeToString(
                                    book.getDescription().getBytes(StandardCharsets.UTF_8)
                            );
                            return String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl(), encodedDesc);
                        }
                        case "list": {
                            String[] booksIdList = parts[2].split(",");

                            List<Integer> bookIds = Arrays.stream(booksIdList)
                                    .map(String::trim)
                                    .map(Integer::parseInt)
                                    .toList();
                            List<Book> books = bookDAO.getBooks(bookIds);
                        }
                        case "title": {
                            List<Book> booksObj = bookDAO.getBooks(parts[2]);
                            StringBuilder books = new StringBuilder();
                            for (Book book : booksObj) {
                                List<String> authors = book.getAuthors();
                                String authorsString = String.join(", ", authors);
                                String encodedDesc = Base64.getEncoder().encodeToString(
                                        book.getDescription().getBytes(StandardCharsets.UTF_8)
                                );
                                books.append(String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl(), encodedDesc));
                                books.append("|");
                            }

                            return books.toString();
                        }

                        // -----------------------------    =^.^=   --------------------------
                        case "author": {
                            if (parts.length < 3) return ERROR_MESSAGE;

                            String authorName = parts[2];

                            int offset = 0;
                            if (parts.length > 3) {
                                try {
                                    offset = Integer.parseInt(parts[3]);
                                } catch (NumberFormatException e) {
                                    offset = 0; // Fallback in caso di errore nel numero
                                }
                            }

                            int limit = 20; // Numero di libri per ogni "caricamento"

                            List<Book> booksObj = bookDAO.getAuthorBooks(authorName, limit, offset);

                            if (booksObj == null || booksObj.isEmpty()) {
                                return "NOT_FOUND";
                            }

                            StringBuilder books = new StringBuilder();
                            for (Book book : booksObj) {
                                List<String> authors = book.getAuthors();
                                String authorsString = String.join(", ", authors);

                                String encodedDesc = "";
                                if (book.getDescription() != null) {
                                    encodedDesc = Base64.getEncoder().encodeToString(
                                            book.getDescription().getBytes(StandardCharsets.UTF_8)
                                    );
                                }

                                books.append(String.join(SEPARATOR,
                                        Integer.toString(book.getId()),
                                        book.getTitle(),
                                        authorsString,
                                        Integer.toString(book.getPublicationYear()),
                                        book.getPublisher(),
                                        book.getCategory(),
                                        book.getCoverImageUrl(),
                                        encodedDesc));
                                books.append("|");
                            }
                            return books.toString();
                        }

                        case "authors": { // ricerca autori di un libro
                            List<String> authors = bookDAO.getBookAuthors(Integer.parseInt(parts[2]));
                            return String.join(",", authors);
                        }
                        case "top": { // top inteso come i 20 libri più frequenti nelle librerie
                            List<Book> booksObj = bookDAO.getBooks(parts[2], Integer.parseInt(parts[3]));
                            StringBuilder books = new StringBuilder();
                            for (Book book : booksObj) {
                                List<String> authors = book.getAuthors();
                                String authorsString = String.join(", ", authors);
                                books.append(String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl()));
                                books.append("|");
                            }
                            return books.toString();
                        }
                    }
                }
                case "get_user_library":
                    if (parts.length < 3) return ERROR_MESSAGE;
                    switch (parts[1]) {
                        case "id": {
                            Library library = libraryDAO.getLibrary(Integer.parseInt(parts[2]));
                            List<Integer> bookIdIntegers = library.getBookIds();
                            String bookIds = bookIdIntegers.stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(","));
                            return String.join(SEPARATOR, Integer.toString(library.getId()), library.getName(), library.getUserId(), bookIds);
                        }
                        case "name": {
                            if (parts.length < 4) return ERROR_MESSAGE;
                            Library library = libraryDAO.getLibrary(parts[2], parts[3]);
                            List<Integer> bookIdIntegers = library.getBookIds();
                            String bookIds = bookIdIntegers.stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(","));
                            return String.join(SEPARATOR, Integer.toString(library.getId()), library.getName(), library.getUserId(), bookIds);
                        }
                        default:
                            return "UNKNOW_SEARCH_TYPE";
                    }
                case "get_user_libraries": {
                    if (parts.length < 2) return ERROR_MESSAGE;
                    List<Library> libraries = libraryDAO.getLibraries(parts[1]);
                    String libraryIds = libraries.stream()
                            .map(Library::getId)        // 1. Mappa ogni oggetto Library al suo ID
                            .map(String::valueOf)       // 2. Converte l'ID numerico in String
                            .collect(Collectors.joining(",")); // 3. Unisce tutte le Stringhe con la virgola come delimitatore
                    return libraryIds;
                }
                case "add_library": {
                    if (parts.length < 3) return ERROR_MESSAGE;
                    String library = parts[1];
                    String username = parts[2];
                    boolean ok = libraryDAO.addLibrary(library, username);
                    return ok ? "ADD_LIBRARY" + SEPARATOR + "OK" : "ADD_LIBRARY" + SEPARATOR + "FAIL";
                }
                case "remove_library": {
                    if (parts.length < 2) return ERROR_MESSAGE;
                    int lib_id = Integer.parseInt(parts[1]);
                    Library library = libraryDAO.getLibrary(lib_id);
                    boolean ok = libraryDAO.removeLibrary(library);
                    return ok ? "REMOVE_LIBRARY" + SEPARATOR + "OK" : "REMOVE_LIBRARY" + SEPARATOR + "FAIL";
                }
                case "add_book_to_library": {
                    // opzione 1: method, lib_name, username, book_id
                    Book book;
                    Library library;
                    if (parts.length < 4) {
                        // opzione 2: method, lib_id, book_id
                        if (parts.length == 3) {
                            book = bookDAO.getBook(Integer.parseInt(parts[2]));
                            library = libraryDAO.getLibrary(Integer.parseInt(parts[1]));
                        } else return ERROR_MESSAGE;
                    } else {
                        book = bookDAO.getBook(Integer.parseInt(parts[3]));
                        library = libraryDAO.getLibrary(parts[1], parts[2]);
                    }
                    boolean ok = libraryDAO.addBook(book, library);
                    return ok ? "ADD_BOOK_TO_LIBRARY" + SEPARATOR + "OK" : "ADD_BOOK_TO_LIBRARY" + SEPARATOR + "FAIL";
                }
                case "remove_book_from_library": {
                    // opzione 1: method, lib_name, username, book_id
                    Book book;
                    Library library;
                    if (parts.length < 4) {
                        // opzione 2: method, lib_id, book_id
                        if (parts.length == 3) {
                            book = bookDAO.getBook(Integer.parseInt(parts[2]));
                            library = libraryDAO.getLibrary(Integer.parseInt(parts[1]));
                        } else return ERROR_MESSAGE;
                    } else {
                        book = bookDAO.getBook(Integer.parseInt(parts[3]));
                        library = libraryDAO.getLibrary(parts[1], parts[2]);
                    }
                    boolean ok = libraryDAO.removeBook(book, library);
                    return ok ? "REMOVE_BOOK_FROM_LIBRARY" + SEPARATOR + "OK" : "REMOVE_BOOK_FROM_LIBRARY" + SEPARATOR + "FAIL";
                }
                case "get_book_reviews": {
                    if (parts.length < 2) return ERROR_MESSAGE;
                    List<Rating> book_ratings = ratingDAO.getRatings(Integer.parseInt(parts[1]));
                    StringBuilder ratings = new StringBuilder();
                    for (Rating rating : book_ratings) {
                        // Gestione nota null o vuota per evitare crash
                        String noteToEncode = (rating.getNotes() == null) ? "" : rating.getNotes();

                        // Codifica in Base64 per l'invio sul socket (evita che i ';' nella nota rompano il parsing)
                        String encodedNote = Base64.getEncoder().encodeToString(
                                noteToEncode.getBytes(StandardCharsets.UTF_8)
                        );

                        ratings.append(String.join(SEPARATOR,
                                rating.getUserId(),
                                rating.getBookId(),
                                Integer.toString(rating.getStyle()),
                                Integer.toString(rating.getContent()),
                                Integer.toString(rating.getEnjoyment()),
                                Integer.toString(rating.getOriginality()),
                                Integer.toString(rating.getEdition()),
                                Integer.toString(rating.getFinalScore()),
                                encodedNote));
                        ratings.append("|");
                    }
                    return ratings.toString();
                }

                case "get_user_reviews": {
                    if (parts.length < 2) return ERROR_MESSAGE;
                    String username = parts[1];
                    List<Rating> user_ratings = ratingDAO.getRatings(username);

                    if (user_ratings.isEmpty()) return "NO_REVIEWS";

                    StringBuilder sb = new StringBuilder();
                    for (Rating rating : user_ratings) {
                        String noteToEncode = (rating.getNotes() == null) ? "" : rating.getNotes();
                        String encodedNote = Base64.getEncoder().encodeToString(
                                noteToEncode.getBytes(StandardCharsets.UTF_8)
                        );

                        sb.append(String.join(SEPARATOR,
                                rating.getUserId(),
                                rating.getBookId(),
                                Integer.toString(rating.getStyle()),
                                Integer.toString(rating.getContent()),
                                Integer.toString(rating.getEnjoyment()),
                                Integer.toString(rating.getOriginality()),
                                Integer.toString(rating.getEdition()),
                                Integer.toString(rating.getFinalScore()),
                                encodedNote));
                        sb.append("|");
                    }
                    return sb.toString();
                }

                case "add_book_review": {
                    // Formato: add_book_review;book_id;username;style;content;liking;originality;edition;encoded_notes
                    if (parts.length < 8) return ERROR_MESSAGE;

                    try {
                        int bookId = Integer.parseInt(parts[1]);
                        String username = parts[2];
                        int style = Integer.parseInt(parts[3]);
                        int content = Integer.parseInt(parts[4]);
                        int enjoy = Integer.parseInt(parts[5]);
                        int orig = Integer.parseInt(parts[6]);
                        int edit = Integer.parseInt(parts[7]);

                        String notesToSave = "";

                        // Controllo se esiste il nono elemento (indice 8) per le note
                        if (parts.length >= 9 && parts[8] != null && !parts[8].isBlank() && !parts[8].equalsIgnoreCase("EMPTY")) {
                            try {
                                // PULIZIA E DECODIFICA FORZATA
                                String rawBase64 = parts[8].trim();
                                byte[] decodedBytes = Base64.getMimeDecoder().decode(rawBase64);
                                notesToSave = new String(decodedBytes, StandardCharsets.UTF_8);

                                // LOG DI CONTROLLO
                                System.out.println("[SERVER] Nota decodificata correttamente: " + notesToSave);
                            } catch (Exception e) {
                                // Se la decodifica fallisce, forse il client ha mandato testo piano?
                                System.err.println("[SERVER] Fallita decodifica Base64, salvo come testo piano: " + parts[8]);
                                notesToSave = parts[8];
                            }
                        }

                        boolean ok = ratingDAO.addRating(bookId, username, style, content, enjoy, orig, edit, notesToSave);

                        return ok ? "ADD_BOOK_REVIEW" + SEPARATOR + "OK" : "ADD_BOOK_REVIEW" + SEPARATOR + "FAIL";
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "ERROR" + SEPARATOR + "processing_review";
                    }
                }

                case "remove_book_review": {
                    // Formato: remove_book_review;book_id;username
                    if (parts.length < 3) return ERROR_MESSAGE;
                    try {
                        int bookId = Integer.parseInt(parts[1]);
                        String username = parts[2];

                        boolean ok = ratingDAO.removeRating(bookId, username);
                        return ok ? "REMOVE_BOOK_REVIEW" + SEPARATOR + "OK" : "REMOVE_BOOK_REVIEW" + SEPARATOR + "FAIL";
                    } catch (Exception e) {
                        return "ERROR" + SEPARATOR + "invalid_id";
                    }
                }

                case "get_book_advices": { // Consigli ricevuti
                    if (parts.length < 2) return ERROR_MESSAGE;
                    List<Recommendation> recommendations = recommendationDAO.getRecommendations(Integer.parseInt(parts[1]));
                    if (recommendations.isEmpty()) return "NO_RECOMMENDATIONS";
                    return formatRecommendationList(recommendations);
                }

                case "get_advices_made_by_user": { // Consigli creati dall'utente
                    if (parts.length < 2) return ERROR_MESSAGE;
                    List<Recommendation> made = recommendationDAO.getRecommendationsMadeBy(parts[1]);
                    if (made.isEmpty()) return "NO_RECOMMENDATIONS_MADE";
                    return formatRecommendationList(made);
                }

                case "add_book_advice": {
                    // Formato: add_book_advice;username;book_id;rec_id1,rec_id2,rec_id3
                    if (parts.length < 4) return ERROR_MESSAGE;

                    String username = parts[1];
                    String bookId = parts[2];
                    // Split della lista di libri consigliati (separati da virgola)
                    List<String> recommendedIds = Arrays.asList(parts[3].split(","));

                    try {
                        Recommendation rec = new Recommendation(username, bookId, recommendedIds);
                        boolean ok = recommendationDAO.addRecommendation(rec);
                        return ok ? "ADD_BOOK_ADVICE" + SEPARATOR + "OK" : "ADD_BOOK_ADVICE" + SEPARATOR + "FAIL";
                    } catch (IllegalArgumentException e) {
                        return "ERROR" + SEPARATOR + "too_many_recommendations";
                    }
                }

                case "remove_book_advice": {
                    // Formato: remove_book_advice;username;book_id
                    if (parts.length < 3) return ERROR_MESSAGE;
                    String username = parts[1];
                    String bookId = parts[2];

                    boolean ok = recommendationDAO.removeRecommendations(username, bookId);
                    return ok ? "REMOVE_BOOK_ADVICE" + SEPARATOR + "OK" : "REMOVE_BOOK_ADVICE" + SEPARATOR + "FAIL";
                }
                default:
                    return "UNKNOWN_COMMAND";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR;" + e.getMessage();
        }
    }
}