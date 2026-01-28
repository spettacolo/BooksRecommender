package ONA.booksrecommender.server.database.dao;

/**
 * Interfaccia radice per tutti gli oggetti di accesso ai dati (DAO).
 * Definisce il contratto per la gestione delle risorse, assicurando che ogni
 * componente che interagisce con il database fornisca un meccanismo per
 * rilasciare le connessioni e gli statement aperti.
 */
public interface DAO {
    /**
     * Chiude le risorse aperte dal DAO (connessioni, {@link java.sql.PreparedStatement},
     * {@link java.sql.ResultSet}).
     * Deve essere invocato al termine del ciclo di vita del database per prevenire
     * leak di memoria o saturazione del pool di connessioni.
     */
    void close();
}
