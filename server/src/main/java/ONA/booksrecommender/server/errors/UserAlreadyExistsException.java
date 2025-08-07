package ONA.booksrecommender.server.errors;

public class UserAlreadyExistsException extends Exception {

    // TODO: decidere quale eccezione lasciare

    // Costruttore senza argomenti
    public UserAlreadyExistsException() {
        super("L'utente con questo username esiste già.");
    }

    // Costruttore con un messaggio personalizzato
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    // Costruttore con un messaggio e una causa (un'altra Throwable)
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    // Costruttore con una causa
    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
