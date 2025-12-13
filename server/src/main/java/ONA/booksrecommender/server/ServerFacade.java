package ONA.booksrecommender.server;

import ONA.booksrecommender.objects.Library;
import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.BookDAO;
import ONA.booksrecommender.server.database.dao.LibraryDAO;
import ONA.booksrecommender.server.database.dao.UserDAO;
import ONA.booksrecommender.utils.Logger;
import ONA.booksrecommender.objects.User;
import ONA.booksrecommender.objects.Book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class ServerFacade {
    private static final String SEPARATOR = ";";
    private static final String ERROR_MESSAGE = "ERROR" + SEPARATOR + "missing_params";
    private final Logger logger;
    private final Database database;

    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final LibraryDAO libraryDAO;

    public ServerFacade(Logger logger, Database database) {
        this.logger = logger;
        this.database = database;

        this.userDAO = database.getDAO(UserDAO.class);
        this.bookDAO = database.getDAO(BookDAO.class);
        this.libraryDAO = database.getDAO(LibraryDAO.class);
        // TODO: Aggiungere gli altri DAO (e.g.: recensioni, consigli)
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
                    if (parts.length < 3) return ERROR_MESSAGE;
                    switch (parts[1]) {
                        case "id": {
                            Book book = bookDAO.getBook(Integer.parseInt(parts[2]));
                            logger.log(book.toString());
                            List<String> authors = book.getAuthors();
                            String authorsString = String.join(", ", authors);
                            // TODO: valutare l'utilizzo di book.toString() in base a cosa è più comodo
                            return String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl());
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
                                books.append(String.join(SEPARATOR, Integer.toString(book.getId()), book.getTitle(), authorsString, Integer.toString(book.getPublicationYear()), book.getPublisher(), book.getCategory(), book.getCoverImageUrl()));
                                books.append("|");
                            }

                            return books.toString();
                        }
                        case "author": {
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
                case "get_book_reviews":
                    return "UNKNOWN_COMMAND";
                case "get_book_advices":
                    return "UNKNOWN_COMMAND";
                case "add_book_review":
                    return "UNKNOWN_COMMAND";
                case "add_book_advice":
                    return "UNKNOWN_COMMAND";
                default:
                    return "UNKNOWN_COMMAND";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR;" + e.getMessage();
        }
    }
}