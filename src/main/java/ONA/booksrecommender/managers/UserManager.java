package ONA.booksrecommender;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce le operazioni relative agli utenti, come la registrazione, l'autenticazione e il caricamento dei dati.
 * La classe consente di registrare nuovi utenti, autenticare gli utenti esistenti e verificare se un utente esiste.
 */
public class UserManager {
    private Map<String, User> users = new HashMap<>();
    private static final String USERS_FILE = "../data/UtentiRegistrati.csv";

    /**
     * Carica gli utenti registrati da un file CSV.
     * Ogni riga del file viene convertita in un oggetto `User` e aggiunta alla mappa degli utenti.
     *
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public void loadUsers() throws IOException {
        List<String> lines = FileUtils.readLines(USERS_FILE);
        for (String line : lines) {
            User user = User.fromCsvString(line);
            users.put(user.getUserId(), user);
        }
    }

    /**
     * Registra un nuovo utente. L'utente viene aggiunto alla mappa e il suo dato viene scritto nel file CSV.
     *
     * @param user l'utente da registrare.
     * @throws IOException se si verifica un errore durante la scrittura nel file.
     * @throws IllegalArgumentException se l'ID utente esiste già.
     */
    public void registerUser(User user) throws IOException {
        if (userExists(user.getUserId())) {
            throw new IllegalArgumentException("User ID already exists");
        }
        users.put(user.getUserId(), user);
        FileUtils.appendLine(USERS_FILE, user.toCsvString());
    }

    /**
     * Autentica un utente tramite il suo ID e password.
     * Restituisce l'oggetto `User` se l'autenticazione è riuscita, altrimenti restituisce `null`.
     *
     * @param userId   l'ID dell'utente.
     * @param password la password dell'utente.
     * @return l'oggetto `User` se l'autenticazione è corretta, `null` altrimenti.
     */
    public User authenticateUser(String userId, String password) {
        User user = users.get(userId);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Verifica se un utente con l'ID specificato esiste già.
     *
     * @param userId l'ID dell'utente da verificare.
     * @return `true` se l'utente esiste, `false` altrimenti.
     */
    public boolean userExists(String userId) {
        return users.containsKey(userId);
    }
}