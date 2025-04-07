package ONA.booksrecommender.objects;

import java.util.Arrays;

/**
 * Rappresenta un utente con le informazioni personali e di accesso.
 * La classe include metodi per la gestione dei dati utente, come la conversione in formato CSV.
 */
public class User {
    private String name;
    private String surname;
    private String fiscalCode;
    private String email;
    private String userId;
    private String password;

    /**
     * Costruisce un nuovo oggetto `User` con i dati forniti.
     *
     * @param name       il nome dell'utente.
     * @param surname    il cognome dell'utente.
     * @param fiscalCode il codice fiscale dell'utente.
     * @param email      l'indirizzo email dell'utente.
     * @param userId     l'ID utente.
     * @param password   la password dell'utente.
     */
    public User(String name, String surname, String fiscalCode, String email, String userId, String password) {
        this.name = name;
        this.surname = surname;
        this.fiscalCode = fiscalCode;
        this.email = email;
        this.userId = userId;
        this.password = password;
    }


    // Getters

    /**
     * Restituisce il nome dell'utente.
     *
     * @return il nome dell'utente.
     */
    public String getName() { return name; }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return il cognome dell'utente.
     */
    public String getSurname() { return surname; }

    /**
     * Restituisce il codice fiscale dell'utente.
     *
     * @return il codice fiscale dell'utente.
     */
    public String getFiscalCode() { return fiscalCode; }

    /**
     * Restituisce l'indirizzo email dell'utente.
     *
     * @return l'indirizzo email dell'utente.
     */
    public String getEmail() { return email; }

    /**
     * Restituisce l'ID utente.
     *
     * @return l'ID utente.
     */
    public String getUserId() { return userId; }

    /**
     * Restituisce la password dell'utente.
     *
     * @return la password dell'utente.
     */
    public String getPassword() { return password; }

     /**
     * Converte l'oggetto `User` in una stringa CSV.
     *
     * @return la rappresentazione CSV dell'utente.
     */
    public String toCsvString() {
        return String.join(",", Arrays.asList(
            name, surname, fiscalCode, email, userId, password
        ));
    }

    /**
     * Crea un oggetto `User` a partire da una stringa CSV.
     *
     * @param csv la stringa CSV contenente i dati dell'utente.
     * @return un nuovo oggetto `User` con i dati estratti dalla stringa CSV.
     */
    public static User fromCsvString(String csv) {
        String[] parts = csv.split(",");
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
}