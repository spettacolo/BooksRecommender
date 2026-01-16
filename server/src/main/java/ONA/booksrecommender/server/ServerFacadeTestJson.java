package ONA.booksrecommender.server;

import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.utils.Logger;
import ONA.booksrecommender.objects.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class ServerFacadeTestJson {
    private final Logger logger;
    private final Database database;
    private final Gson gson;

    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final LibraryDAO libraryDAO;
    private final RatingDAO ratingDAO;
    private final RecommendationDAO recommendationDAO;

    public ServerFacadeTestJson(Logger logger, Database database) {
        this.logger = logger;
        this.database = database;
        this.gson = new Gson(); // Inizializzazione GSON

        this.userDAO = database.getDAO(UserDAO.class);
        this.bookDAO = database.getDAO(BookDAO.class);
        this.libraryDAO = database.getDAO(LibraryDAO.class);
        this.ratingDAO = database.getDAO(RatingDAO.class);
        this.recommendationDAO = database.getDAO(RecommendationDAO.class);
    }

    /**
     * Gestisce la richiesta ricevuta in formato JSON.
     * Formato atteso: { "action": "CMD_NAME", "data": { ... } }
     */
    public String handleRequest(String jsonReq) {
        try {
            JsonObject request = JsonParser.parseString(jsonReq).getAsJsonObject();
            String action = request.get("action").getAsString();
            JsonObject data = request.has("data") ? request.getAsJsonObject("data") : new JsonObject();

            switch (action) {
                // --- USER OPERATIONS ---
                case "login": {
                    int code = userDAO.login(data.get("username").getAsString(), data.get("password").getAsString());
                    return createResponse("LOGIN", code);
                }

                case "sign_up": {
                    boolean ok = userDAO.signUpUser(
                            data.get("username").getAsString(),
                            data.get("name").getAsString(),
                            data.get("surname").getAsString(),
                            data.get("fiscalCode").getAsString(),
                            data.get("email").getAsString(),
                            data.get("password").getAsString()
                    );
                    return createResponse("SIGNUP", ok ? "OK" : "FAIL");
                }

                // --- BOOK OPERATIONS ---
                case "get_book_by_id": {
                    Book book = bookDAO.getBook(data.get("id").getAsInt());
                    return (book != null) ? gson.toJson(book) : createError("Book not found");
                }

                case "search_books_by_title": {
                    List<Book> books = bookDAO.getBooks(data.get("title").getAsString());
                    return gson.toJson(books);
                }

                case "get_top_books": {
                    String category = data.get("category").getAsString();
                    int limit = data.get("limit").getAsInt();
                    List<Book> books = bookDAO.getBooks(category, limit);
                    return gson.toJson(books);
                }

                // --- LIBRARY OPERATIONS ---
                case "get_user_libraries": {
                    List<Library> libraries = libraryDAO.getLibraries(data.get("username").getAsString());
                    return gson.toJson(libraries);
                }

                case "add_book_to_library": {
                    Book book = bookDAO.getBook(data.get("bookId").getAsInt());
                    Library library = libraryDAO.getLibrary(data.get("libraryId").getAsInt());
                    boolean ok = libraryDAO.addBook(book, library);
                    return createResponse("ADD_BOOK", ok ? "OK" : "FAIL");
                }

                // --- RATING OPERATIONS ---
                case "add_book_review": {
                    // Deserializzazione diretta da JSON a oggetto Rating
                    Rating rating = gson.fromJson(data, Rating.class);
                    boolean ok = ratingDAO.addRating(
                            Integer.parseInt(rating.getBookId()),
                            rating.getUserId(),
                            rating.getStyle(),
                            rating.getContent(),
                            rating.getEnjoyment(),
                            rating.getOriginality(),
                            rating.getEdition(),
                            rating.getNotes()
                    );
                    return createResponse("ADD_REVIEW", ok ? "OK" : "FAIL");
                }

                case "get_book_ratings": {
                    List<Rating> ratings = ratingDAO.getRatings(data.get("bookId").getAsInt());
                    return gson.toJson(ratings);
                }

                // --- RECOMMENDATION OPERATIONS ---
                case "get_recommendations": {
                    List<Recommendation> recs = recommendationDAO.getRecommendations(data.get("username").getAsString());
                    return gson.toJson(recs);
                }

                case "add_recommendation": {
                    // Esempio di conversione automatica JSON -> Oggetto Java
                    Recommendation rec = gson.fromJson(data, Recommendation.class);
                    boolean ok = recommendationDAO.addRecommendation(rec);
                    return createResponse("ADD_RECOMMENDATION", ok ? "OK" : "FAIL");
                }

                default:
                    return createError("UNKNOWN_COMMAND");
            }
        } catch (Exception e) {
            logger.log("Error handling JSON request: " + e.getMessage());
            return createError(e.getMessage());
        }
    }

    // --- UTILS PER RISPOSTE STANDARD ---

    private String createResponse(String type, Object value) {
        JsonObject res = new JsonObject();
        res.addProperty("type", type);
        res.addProperty("result", value.toString());
        return gson.toJson(res);
    }

    private String createError(String message) {
        JsonObject res = new JsonObject();
        res.addProperty("status", "ERROR");
        res.addProperty("message", message);
        return gson.toJson(res);
    }
}