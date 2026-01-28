package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.User;
import ONA.booksrecommender.utils.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends BaseDAO implements AutoCloseable {

    /**
     * Costruttore della classe UserDAO.
     * Inizializza l'accesso ai dati per la gestione delle utenze, sfruttando la
     * connessione fornita da {@link BaseDAO}.
     *
     * @param logger     L'istanza di {@link Logger} per registrare tentativi di accesso e operazioni.
     * @param connection La connessione attiva al database PostgreSQL.
     */
    public UserDAO(Logger logger, Connection connection) {
        super(logger, connection); // crea la connessione nel costruttore di BaseDAO
    }

    /**
     * Recupera i dati di un utente dal database tramite lo username.
     * Il metodo supporta una modalità di recupero differenziata: se viene richiesto per il login,
     * include l'hash della password nell'oggetto restituito; altrimenti, restituisce un
     * profilo pubblico/informativo con il campo password nullo.
     *
     * @param userId Lo username dell'utente da cercare.
     * @param login  Flag booleano: se {@code true}, carica anche l'hash della password.
     * @return Un oggetto {@link User} se trovato, {@code null} altrimenti.
     */
    public User getUser(String userId, boolean login) {
        String query;

        if (login)
            query = "SELECT username, name, surname, tax_code, email, password FROM users WHERE username = ?";
        else
            query = "SELECT * FROM users WHERE username = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new User(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("tax_code"),
                        rs.getString("email"),
                        login ? rs.getString("password") : null
                );
            }
        } catch (SQLException e) {
            logger.log("Error during login: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gestisce la procedura di autenticazione dell'utente.
     * Recupera l'hash memorizzato nel database e lo confronta con la password fornita in chiaro
     * utilizzando {@link BCryptPasswordEncoder}.
     *
     * @param userId   Lo username fornito dall'utente.
     * @param password La password in chiaro da verificare.
     * @return Un codice intero:
     * <ul>
     * <li>{@code 0}: Autenticazione riuscita.</li>
     * <li>{@code -2}: Password errata.</li>
     * <li>{@code -3}: Utente non trovato o errore di sistema.</li>
     * </ul>
     */
    public int login(String userId, String password) {
        try {
            User user = getUser(userId, true);

            if (user == null)
                return -3;

            BCryptPasswordEncoder passwordManager = new BCryptPasswordEncoder();
            boolean isMatch = passwordManager.matches(password, user.getPassword());

            if (!isMatch) return -2;

            return 0;
        } catch (Exception e) {
            logger.log("Error during user retrieval: " + e.getMessage());
            return -3;
        }
    }

    /**
     * Registra un nuovo utente nel sistema.
     * Verifica preventivamente che lo username non sia già occupato. Prima dell'inserimento,
     * la password viene cifrata in modo sicuro tramite l'algoritmo BCrypt.
     *
     * @param userId     Username univoco scelto dall'utente.
     * @param name       Nome dell'utente.
     * @param surname    Cognome dell'utente.
     * @param fiscalCode Codice Fiscale dell'utente.
     * @param email      Indirizzo email dell'utente.
     * @param password   Password in chiaro che verrà hashata prima del salvataggio.
     * @return {@code true} se la registrazione è avvenuta con successo, {@code false} se lo username esiste già o per errori SQL.
     */
    public boolean signUpUser(String userId, String name, String surname, String fiscalCode, String email, String password) {
        String insertQuery = "INSERT INTO users(username, name, surname, tax_code, email, password) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            User user = getUser(userId, false);

            if (user != null) {
                return false;
            }

            BCryptPasswordEncoder passwordManager = new BCryptPasswordEncoder();
            String hashedPassword = passwordManager.encode(password);

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, userId);
                insertStmt.setString(2, name);
                insertStmt.setString(3, surname);
                insertStmt.setString(4, fiscalCode);
                insertStmt.setString(5, email);
                insertStmt.setString(6, hashedPassword);

                insertStmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            logger.log("Error during user signup: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        super.close();
    }
}