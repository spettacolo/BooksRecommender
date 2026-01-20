package ONA.booksrecommender.server;

import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.objects.*;
import ONA.booksrecommender.utils.Logger;
import com.google.gson.*;

import java.util.*;

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
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        this.userDAO             = database.getDAO(UserDAO.class);
        this.bookDAO             = database.getDAO(BookDAO.class);
        this.libraryDAO          = database.getDAO(LibraryDAO.class);
        this.ratingDAO           = database.getDAO(RatingDAO.class);
        this.recommendationDAO   = database.getDAO(RecommendationDAO.class);
    }

    public String handleRequest(String jsonReq) {
        try {
            JsonObject req = JsonParser.parseString(jsonReq).getAsJsonObject();
            String action = req.get("action").getAsString().toLowerCase();
            JsonObject data = req.has("data") ? req.getAsJsonObject("data") : new JsonObject();

            return switch (action) {
                // ── AUTENTICAZIONE ───────────────────────────────────────
                case "login"            -> handleLogin(data);
                case "signup", "sign_up", "register" -> handleSignup(data);

                // ── UTENTE ───────────────────────────────────────────────
                case "get_user"         -> handleGetUser(data);

                // ── LIBRI ─────────────────────────────────────────────────
                case "get_book"         -> handleGetBook(data);
                case "search_books"     -> handleSearchBooks(data);
                case "get_book_authors" -> handleGetBookAuthors(data);

                // ── LIBRERIE ──────────────────────────────────────────────
                case "get_user_libraries"      -> handleGetUserLibraries(data);
                case "get_library"             -> handleGetLibrary(data);
                case "add_library"             -> handleAddLibrary(data);
                case "remove_library"          -> handleRemoveLibrary(data);
                case "add_book_to_library"     -> handleAddBookToLibrary(data);
                case "remove_book_from_library"-> handleRemoveBookFromLibrary(data);

                // ── RECENSIONI ────────────────────────────────────────────
                case "add_book_review", "add_rating" -> handleAddReview(data);
                case "get_book_ratings", "get_book_reviews" -> handleGetBookRatings(data);

                // ── CONSIGLI ──────────────────────────────────────────────
                case "get_user_recommendations", "get_received_recommendations" ->
                        handleGetReceivedRecommendations(data);

                case "get_recommendations_made_by_user", "get_made_recommendations" ->
                        handleGetMadeRecommendations(data);

                case "add_recommendation", "add_book_advice" -> handleAddRecommendation(data);
                case "remove_recommendation", "remove_book_advice" -> handleRemoveRecommendation(data);

                default -> error("Azione non riconosciuta: " + action);
            };

        } catch (JsonSyntaxException e) {
            return error("JSON malformato: " + e.getMessage());
        } catch (Exception e) {
            //logger.error("Errore critico durante elaborazione richiesta", e);
            return error("Errore server interno");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //                         IMPLEMENTAZIONI SINGOLE
    // ────────────────────────────────────────────────────────────────────────

    private String handleLogin(JsonObject data) {
        String username = getRequired(data, "username", String.class);
        String password = getRequired(data, "password", String.class);

        int code = userDAO.login(username, password);
        return response("login", Map.of(
                "code", code,
                "success", code == 0,
                "message", code == 0 ? "Login effettuato" : "Credenziali non valide"
        ));
    }

    private String handleSignup(JsonObject data) {
        String username   = getRequired(data, "username",   String.class);
        String name       = getRequired(data, "name",       String.class);
        String surname    = getRequired(data, "surname",    String.class);
        String fiscalCode = getRequired(data, "fiscalCode", String.class);
        String email      = getRequired(data, "email",      String.class);
        String password   = getRequired(data, "password",   String.class);

        boolean success = userDAO.signUpUser(username, name, surname, fiscalCode, email, password);
        return response("signup", Map.of("success", success));
    }

    private String handleGetUser(JsonObject data) {
        String username = getRequired(data, "username", String.class);
        User user = userDAO.getUser(username, false);
        return user != null ? gson.toJson(user) : error("Utente non trovato");
    }

    private String handleGetBook(JsonObject data) {
        int id = getRequired(data, "id", Integer.class);
        Book book = bookDAO.getBook(id);
        return book != null ? gson.toJson(book) : error("Libro non trovato");
    }

    private String handleSearchBooks(JsonObject data) {
        String type = getRequired(data, "type", String.class).toLowerCase();

        List<Book> books;
        JsonObject extraInfo = new JsonObject();

        switch (type) {
            case "top" -> {
                int limit = Math.min(Math.max(data.has("limit") ? data.get("limit").getAsInt() : 20, 1), 100);
                books = bookDAO.getBooks("top", limit);

                extraInfo.addProperty("query_type", "top");
                extraInfo.addProperty("limit_applied", limit);
            }

            case "category" -> {
                String category = getRequired(data, "category", String.class);
                int limit = data.has("limit") ? Math.min(Math.max(data.get("limit").getAsInt(), 1), 100) : 20;
                books = bookDAO.getBooks(category, limit);   // ← usa la query random per categoria
                extraInfo.addProperty("category", category);
                extraInfo.addProperty("limit_applied", limit);
            }

            case "title" -> {
                String title = getRequired(data, "title", String.class);
                books = bookDAO.getBooks(title);
            }

            case "author" -> {
                String author = getRequired(data, "author", String.class);
                String order = data.has("order") ? data.get("order").getAsString() : "ASC";
                books = bookDAO.getAuthorBooks(author, order);
            }

            default -> throw new IllegalArgumentException("Tipo ricerca non supportato: " + type);
        }

        // Costruzione risposta comune
        JsonObject response = new JsonObject();
        JsonArray booksArray = new JsonArray();
        books.forEach(b -> booksArray.add(gson.toJsonTree(b)));

        response.add("books", booksArray);
        response.addProperty("count", books.size());
        response.addProperty("type", type);

        // Aggiungiamo eventuali metadati extra (solo per top al momento)
        if (!extraInfo.entrySet().isEmpty()) {
            extraInfo.entrySet().forEach(e -> response.addProperty(e.getKey(), e.getValue().getAsString()));
        }

        return gson.toJson(response);
    }

    private String handleGetBookAuthors(JsonObject data) {
        int bookId = getRequired(data, "bookId", Integer.class);
        List<String> authors = bookDAO.getBookAuthors(bookId);
        return gson.toJson(authors);
    }

    // ── Librerie ─────────────────────────────────────────────────────────────

    private String handleGetUserLibraries(JsonObject data) {
        String username = getRequired(data, "username", String.class);
        return gson.toJson(libraryDAO.getLibraries(username));
    }

    private String handleGetLibrary(JsonObject data) {
        int id = getRequired(data, "id", Integer.class);
        Library lib = libraryDAO.getLibrary(id);
        return lib != null ? gson.toJson(lib) : error("Libreria non trovata");
    }

    private String handleAddLibrary(JsonObject data) {
        String name     = getRequired(data, "name",     String.class);
        String username = getRequired(data, "username", String.class);
        boolean ok = libraryDAO.addLibrary(name, username);
        return response("add_library", Map.of("success", ok));
    }

    private String handleRemoveLibrary(JsonObject data) {
        int id = getRequired(data, "id", Integer.class);
        Library lib = libraryDAO.getLibrary(id);
        if (lib == null) return error("Libreria non trovata");
        boolean ok = libraryDAO.removeLibrary(lib);
        return response("remove_library", Map.of("success", ok));
    }

    private String handleAddBookToLibrary(JsonObject data) {
        int libId  = getRequired(data, "libraryId", Integer.class);
        int bookId = getRequired(data, "bookId",    Integer.class);

        Library lib  = libraryDAO.getLibrary(libId);
        Book    book = bookDAO.getBook(bookId);

        if (lib == null || book == null) {
            return error("Libreria o libro non trovato");
        }

        boolean ok = libraryDAO.addBook(book, lib);
        return response("add_book_to_library", Map.of("success", ok));
    }

    private String handleRemoveBookFromLibrary(JsonObject data) {
        int libId  = getRequired(data, "libraryId", Integer.class);
        int bookId = getRequired(data, "bookId",    Integer.class);

        Library lib  = libraryDAO.getLibrary(libId);
        Book    book = bookDAO.getBook(bookId);

        if (lib == null || book == null) {
            return error("Libreria o libro non trovato");
        }

        boolean ok = libraryDAO.removeBook(book, lib);
        return response("remove_book_from_library", Map.of("success", ok));
    }

    // ── Recensioni ───────────────────────────────────────────────────────────

    private String handleAddReview(JsonObject data) {
        Rating rating = gson.fromJson(data, Rating.class);
        boolean success = ratingDAO.addRating(
                Integer.parseInt(rating.getBookId()),
                rating.getUserId(),
                rating.getStyle(),
                rating.getContent(),
                rating.getEnjoyment(),
                rating.getOriginality(),
                rating.getEdition(),
                rating.getNotes()
        );
        return response("add_review", Map.of("success", success));
    }

    private String handleGetBookRatings(JsonObject data) {
        int bookId = getRequired(data, "bookId", Integer.class);
        return gson.toJson(ratingDAO.getRatings(bookId));
    }

    // ── Consigli ─────────────────────────────────────────────────────────────

    private String handleGetReceivedRecommendations(JsonObject data) {
        String bookIdStr = getRequired(data, "book_id", String.class);
        int bookId = Integer.parseInt(bookIdStr);
        return gson.toJson(recommendationDAO.getRecommendations(bookId));
    }

    private String handleGetMadeRecommendations(JsonObject data) {
        String username = getRequired(data, "username", String.class);
        return gson.toJson(recommendationDAO.getRecommendationsMadeBy(username));
    }

    private String handleAddRecommendation(JsonObject data) {
        Recommendation rec = gson.fromJson(data, Recommendation.class);
        boolean ok = recommendationDAO.addRecommendation(rec);
        return response("add_recommendation", Map.of("success", ok));
    }

    private String handleRemoveRecommendation(JsonObject data) {
        String username = getRequired(data, "username", String.class);
        String bookId   = getRequired(data, "bookId",   String.class);
        boolean ok = recommendationDAO.removeRecommendations(username, bookId);
        return response("remove_recommendation", Map.of("success", ok));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private <T> T getRequired(JsonObject obj, String field, Class<T> type) {
        if (!obj.has(field)) {
            throw new IllegalArgumentException("Campo obbligatorio mancante: " + field);
        }
        return gson.fromJson(obj.get(field), type);
    }

    private String response(String type, Map<String, Object> payload) {
        JsonObject res = new JsonObject();
        res.addProperty("status", "ok");
        res.addProperty("type",   type);

        JsonObject data = new JsonObject();
        payload.forEach((k, v) -> {
            if (v instanceof Number n)      data.addProperty(k, n);
            else if (v instanceof Boolean b) data.addProperty(k, b);
            else if (v instanceof String s)  data.addProperty(k, s);
            else data.add(k, gson.toJsonTree(v));
        });

        res.add("data", data);
        return gson.toJson(res);
    }

    private String error(String message) {
        JsonObject res = new JsonObject();
        res.addProperty("status", "error");
        res.addProperty("message", message);
        return gson.toJson(res);
    }
}