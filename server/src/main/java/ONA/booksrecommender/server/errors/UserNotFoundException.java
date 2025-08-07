package ONA.booksrecommender.server.errors;

public class UserNotFoundException extends Exception {

    // Costruttore senza argomenti
    public UserNotFoundException() {
        super("L'utente con questo username non esiste.");
    }

    // Costruttore con un messaggio personalizzato
    public UserNotFoundException(String message) {
        super(message);
    }

    // Costruttore con un messaggio e una causa (un'altra Throwable)
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Costruttore con una causa
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}
