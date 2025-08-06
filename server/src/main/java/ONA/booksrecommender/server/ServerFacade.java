package ONA.booksrecommender.server;

import ONA.booksrecommender.server.database.Database;
import ONA.booksrecommender.server.database.dao.BookDAO;
import ONA.booksrecommender.server.database.dao.UserDAO;
import ONA.booksrecommender.utils.Logger;

import javax.xml.crypto.Data;
import java.sql.SQLException;

public class ServerFacade {
    private static final String SEPARATOR = ";";
    private final Logger logger;
    private final Database database;

    private final UserDAO userDAO;
    private final BookDAO bookDAO;

    public ServerFacade(Logger logger, Database database) {
        this.logger = logger;
        this.database = database;

        this.userDAO = database.getDAO(UserDAO.class);
        this.bookDAO = database.getDAO(BookDAO.class);
        // TODO: Aggiungere gli altri DAO (e.g.: recensioni, consigli, librerie)

    }

    private String handleRequest(String req) {
        String[] parts = req.split(SEPARATOR);
        try {
            switch (req) {
                case "get_user":
                    break;
                case "get_book":
                    break;
                case "get_user_library":
                    break;
                case "get_user_libraries":
                    break;
                case "get_book_reviews":
                    break;
                case "get_book_advices":
                    break;
                case "add_user":
                    break;
                case "add_book":
                    break;
                case "add_library":
                    break;
                case "add_book_review":
                    break;
                case "add_book_advice":
                    break;
                default:
                    return "Unknown command";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return req; // temp
    }
}
