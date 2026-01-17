package ONA.booksrecommender.server;

import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.objects.*;
import ONA.booksrecommender.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

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
        this.gson = new Gson();

        this.userDAO = database.getDAO(UserDAO.class);
        this.bookDAO = database.getDAO(BookDAO.class);
        this.libraryDAO = database.getDAO(LibraryDAO.class);
        this.ratingDAO = database.getDAO(RatingDAO.class);
        this.recommendationDAO = database.getDAO(RecommendationDAO.class);
    }

    public String handleRequest(String jsonReq) {
        try {
            JsonObject request = JsonParser.parseString(jsonReq).getAsJsonObject();
            String action = request.get("action").getAsString();
            JsonObject data = request.has("data") ? request.getAsJsonObject("data") : new JsonObject();

            switch (action) {
                case "login": {
                    int code = userDAO.login(data.get("username").getAsString(), data.get("password").getAsString());
                    return buildJsonResponse("LOGIN_STATUS", code);
                }

                case "get_book": {
                    int id = data.get("id").getAsInt();
                    Book book = bookDAO.getBook(id);
                    return (book != null) ? gson.toJson(book) : buildError("BOOK_NOT_FOUND");
                }

                case "get_user_libraries": {
                    List<Library> libraries = libraryDAO.getLibraries(data.get("username").getAsString());
                    return gson.toJson(libraries);
                }

                case "add_book_review": {
                    Rating rating = gson.fromJson(data, Rating.class);
                    boolean ok = ratingDAO.addRating(
                            Integer.parseInt(rating.getBookId()), rating.getUserId(),
                            rating.getStyle(), rating.getContent(), rating.getEnjoyment(),
                            rating.getOriginality(), rating.getEdition(), rating.getNotes()
                    );
                    return buildJsonResponse("STATUS", ok ? "OK" : "FAIL");
                }

                // --- RECOMMENDATIONS CORRETTE ---

                case "get_user_recommendations": {
                    // Consigli che l'utente deve leggere (destinati a lui)
                    String username = data.get("username").getAsString();
                    List<Recommendation> recs = recommendationDAO.getRecommendations(username);
                    return gson.toJson(recs);
                }

                case "get_recommendations_made_by_user": {
                    // Consigli che l'utente ha creato per altri
                    String username = data.get("username").getAsString();
                    List<Recommendation> recs = recommendationDAO.getRecommendationsMadeBy(username);
                    return gson.toJson(recs);
                }

                case "add_recommendation": {
                    // Crea un nuovo set di consigli. Il JSON 'data' deve corrispondere ai campi di Recommendation.class
                    Recommendation rec = gson.fromJson(data, Recommendation.class);
                    boolean ok = recommendationDAO.addRecommendation(rec);
                    return buildJsonResponse("STATUS", ok ? "OK" : "FAIL");
                }

                default:
                    return buildError("UNKNOWN_ACTION");
            }
        } catch (Exception e) {
            logger.log("JSON Error: " + e.getMessage());
            return buildError(e.getMessage());
        }
    }

    private String buildJsonResponse(String type, Object value) {
        JsonObject res = new JsonObject();
        res.addProperty("type", type);
        res.addProperty("result", value.toString());
        return gson.toJson(res);
    }

    private String buildError(String message) {
        JsonObject res = new JsonObject();
        res.addProperty("status", "ERROR");
        res.addProperty("message", message);
        return gson.toJson(res);
    }
}