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

    public ServerFacade(Logger logger, Database database) {
        this.logger = logger;
        this.database = database;

        this.userDAO = database.getDAO(UserDAO.class);
        this.bookDAO = database.getDAO(BookDAO.class);
        this.libraryDAO = database.getDAO(LibraryDAO.class);
        this.ratingDAO = database.getDAO(RatingDAO.class);
        this.recommendationDAO = database.getDAO(RecommendationDAO.class);
    }

    private String formatRecommendationList(List<Recommendation> list) {
        StringBuilder sb = new StringBuilder();
        for (Recommendation rec : list) {
            String recs = String.join(",", rec.getRecommendedBookIds());
            sb.append(rec.getBookId()).append(SEPARATOR).append(recs).append("|");
        }
        return sb.toString();
    }

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
                    // get_book;search_type;[id]/[title]/[author[;year]]
                    /* TODO: Come decodificare il campo descrizione lato client:
                    import java.util.Base64;
                    import java.nio.charset.StandardCharsets;
                    String descrizioneDecodificata = new String(
                        Base64.getDecoder().decode(parts[7]),
                        StandardCharsets.UTF_8
                    );
                    */
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
                            // TODO: valutare l'utilizzo di book.toString() in base a cosa è più comodo
                            return String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl(), encodedDesc);
                        }
                        case "list": {
                            String[] booksIdList = parts[2].split(",");

                            List<Integer> bookIds = Arrays.stream(booksIdList)
                                    .map(String::trim) // Rimuove spazi bianchi (utile in caso ci siano " 101, 205")
                                    .map(Integer::parseInt) // Converte ogni Stringa in Integer
                                    .toList(); // Raccoglie gli Integer in una List
                            List<Book> books = bookDAO.getBooks(bookIds);
                        }
                        case "title": {
                            //Book book = bookDAO.getBook(parts[2]);
                            List<Book> booksObj = bookDAO.getBooks(parts[2]);
                            //logger.log(booksObj.toString());
                            //logger.log(book.toString());
                            //List<String> authors = book.getAuthors();
                            //String authorsString = String.join(", ", authors);
                            // TODO: valutare l'utilizzo di book.toString() in base a cosa è più comodo
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
                        /* case "author": { // filtro autore-anno TODO: ordinamento asc/desc, poi farò
                            boolean b = !((parts[3]).equalsIgnoreCase("ASC") || (parts[3]).equalsIgnoreCase("DESC"));
                            if (b) return ERROR_MESSAGE;
                            List<Book> booksObj = bookDAO.getAuthorBooks(parts[2], parts[3]);
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
                        }*/

                        // -----------------------------    =^.^=   --------------------------
                        case "author": {
                            if (parts.length < 3) return ERROR_MESSAGE;

                            String authorName = parts[2];

                            // Leggiamo l'offset dal client (parts[3]), se non c'è mettiamo 0
                            int offset = 0;
                            if (parts.length > 3) {
                                try {
                                    offset = Integer.parseInt(parts[3]);
                                } catch (NumberFormatException e) {
                                    offset = 0; // Fallback in caso di errore nel numero
                                }
                            }

                            int limit = 20; // Numero di libri per ogni "caricamento"

                            // CHIAMATA CORRETTA: Passiamo i 3 parametri richiesti dal nuovo BookDAO
                            List<Book> booksObj = bookDAO.getAuthorBooks(authorName, limit, offset);

                            if (booksObj == null || booksObj.isEmpty()) {
                                return "NOT_FOUND";
                            }

                            StringBuilder books = new StringBuilder();
                            for (Book book : booksObj) {
                                List<String> authors = book.getAuthors();
                                String authorsString = String.join(", ", authors);

                                // Codifica descrizione per evitare conflitti con il separatore ";"
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
                            // , anziché ; perché è una lista di elementi e non più elementi differenti
                            return String.join(",", authors);
                        }
                        case "top": { // top inteso come i 20 libri più frequenti nelle librerie
                            List<Book> booksObj = bookDAO.getBooks(parts[2], Integer.parseInt(parts[3]));
                            //logger.log(booksObj.toString());
                            StringBuilder books = new StringBuilder();
                            for (Book book : booksObj) {
                                List<String> authors = book.getAuthors();
                                String authorsString = String.join(", ", authors);
                                books.append(String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl()));
                                books.append("|");
                                // logger.log(book.toString());
                            }
                            return books.toString();

                            /*switch (parts[2]) {
                                case "general": {
                                    List<Book> booksObj = bookDAO.getBooks(parts[2]);
                                    //logger.log(booksObj.toString());
                                    StringBuilder books = new StringBuilder();
                                    for (Book book : booksObj) {
                                        List<String> authors = book.getAuthors();
                                        String authorsString = String.join(", ", authors);
                                        books.append(String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl()));
                                        books.append("|");
                                }
                                case "thrillers": {

                                }
                                case "romance": {

                                }
                                case "fiction": {

                                }
                                case "gardening": {

                                }
                                case "regional & ethnic": {

                                }
                                case "business & economics": {

                                }
                                default: {
                                    // senza filtri
                                }*/
                        }
                    }
                }
                /*case "test_get_book_image":
                    return bookDAO.getBookImageUrl("%22Harry+Potter+e+il+calice+di+fuoco%22");*/
                case "get_user_library":
                    if (parts.length < 3) return ERROR_MESSAGE;
                    switch (parts[1]) {
                        case "id": {
                            Library library = libraryDAO.getLibrary(Integer.parseInt(parts[2]));
                            List<Integer> bookIdIntegers = library.getBookIds();
                            String bookIds = bookIdIntegers.stream() // 1. Create a Stream<Integer>
                                    .map(String::valueOf)   // 2. Map each Integer to a String
                                    .collect(Collectors.joining(",")); // 3. Collect the Strings, joining them with a comma
                            return String.join(SEPARATOR, Integer.toString(library.getId()), library.getName(), library.getUserId(), bookIds);
                        }
                        case "name": {
                            if (parts.length < 4) return ERROR_MESSAGE;
                            Library library = libraryDAO.getLibrary(parts[2], parts[3]);
                            List<Integer> bookIdIntegers = library.getBookIds();
                            String bookIds = bookIdIntegers.stream() // 1. Create a Stream<Integer>
                                    .map(String::valueOf)   // 2. Map each Integer to a String
                                    .collect(Collectors.joining(",")); // 3. Collect the Strings, joining them with a comma
                            return String.join(SEPARATOR, Integer.toString(library.getId()), library.getName(), library.getUserId(), bookIds);
                        }
                        default:
                            return "UNKNOW_SEARCH_TYPE";
                    }
                case "get_user_libraries": {
                    if (parts.length < 2) return ERROR_MESSAGE;
                    List<Library> libraries = libraryDAO.getLibraries(parts[1]);
                    String libraryIds = libraries.stream()
                            .map(Library::getId)        // 1. Mappa ogni oggetto Library al suo ID (che assumiamo sia un Integer o Long)
                            .map(String::valueOf)       // 2. Converte l'ID numerico in String
                            .collect(Collectors.joining(",")); // 3. Unisce tutte le Stringhe con la virgola come delimitatore
                    return libraryIds; // restituisco solo gli id (o i nomi, nicho cosa preferisci?) per comodità, poi verranno fatte richieste a parte lato client per le singole librerie
                }
                /*case "add_user":
                    if (parts.length < 7) { return "ERROR;missing_args"; }
                    return userDAO.signUpUser(parts[1], );
                    return "UNKNOWN_COMMAND";*/
                /*case "add_book":
                    return "UNKNOWN_COMMAND";*/
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
                case "get_book_ratings":
                    if (parts.length < 2) return ERROR_MESSAGE;
                    List<Rating> book_ratings = ratingDAO.getRatings(Integer.parseInt(parts[1]));
                    StringBuilder ratings = new StringBuilder();
                    for (Rating rating : book_ratings) {
                        // Gestione nota null o vuota per evitare crash
                        String noteToEncode = (rating.getNotes() == null) ? "" : rating.getNotes();

                        // Codifichiamo in Base64 per l'invio sul socket (evita che i ';' nella nota rompano il parsing)
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

                // remove_book_review può servire? lmk
                case "add_book_review": {
                    // Formato atteso: add_book_review;book_id;username;style;content;liking;originality;edition;encoded_notes
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

                        // Controlliamo se esiste il nono elemento (indice 8) per le note
                        if (parts.length >= 9 && parts[8] != null && !parts[8].isBlank() && !parts[8].equalsIgnoreCase("EMPTY")) {
                            try {
                                // PULIZIA E DECODIFICA FORZATA
                                String rawBase64 = parts[8].trim();
                                // Usiamo il MimeDecoder che ignora eventuali sporcizie inviate dal client
                                byte[] decodedBytes = Base64.getMimeDecoder().decode(rawBase64);
                                notesToSave = new String(decodedBytes, StandardCharsets.UTF_8);

                                // LOG DI CONTROLLO (Controlla la console del server!)
                                System.out.println("[SERVER] Nota decodificata correttamente: " + notesToSave);
                            } catch (Exception e) {
                                // Se la decodifica fallisce, forse il client ha mandato testo piano?
                                System.err.println("[SERVER] Fallita decodifica Base64, salvo come testo piano: " + parts[8]);
                                notesToSave = parts[8];
                            }
                        }

                        // PASSAGGIO AL DAO: notesToSave ORA È TESTO IN CHIARO (es: "bibia")
                        boolean ok = ratingDAO.addRating(bookId, username, style, content, enjoy, orig, edit, notesToSave);

                        return ok ? "ADD_BOOK_REVIEW" + SEPARATOR + "OK" : "ADD_BOOK_REVIEW" + SEPARATOR + "FAIL";
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "ERROR" + SEPARATOR + "processing_review";
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
                    // Formato richiesta: add_book_advice;username;book_id;rec_id1,rec_id2,rec_id3
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
                    // Formato richiesta: remove_book_advice;username;book_id
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