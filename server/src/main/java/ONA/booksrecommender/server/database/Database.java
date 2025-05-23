package ONA.booksrecommender.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ONA.booksrecommender.server.database.dao.*;
import ONA.booksrecommender.utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/BooksRecommender";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Az-3425";
    
    /*
    Class<? extends DAO> è la chiave della mappa. Ogni DAO ha una classe specifica
    (es. UserDAO.class, BookDAO.class, ecc.), 
    quindi la chiave è un oggetto che rappresenta la classe di ciascun DAO.
    
    DAO è il valore della mappa.
    Rappresenta l'istanza di un DAO (come UserDAO o BookDAO),
    che è associata alla sua classe.
    */ 
    private final Map<Class<? extends DAO>, DAO> daoMap = new HashMap<>();

    public Database(Logger logger) throws SQLException {
        addDAO(new UserDAO(logger));
        addDAO(new BookDAO(logger));
        // altri DAO
    }

    private void addDAO(DAO dao) {
        daoMap.put(dao.getClass(), dao);
    }
    
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /* SPIEGAZIONE PER NICHO E PAKAO
    getDAO(Class<T> clazz)
    Questo metodo è generico (generic method),
    il che significa che può essere usato per ottenere qualsiasi tipo di DAO dalla mappa,
    a patto che tu fornisca la classe del DAO.
        <T extends DAO> significa che T è un tipo che deve estendere DAO (cioè deve essere un tipo che implementa l'interfaccia DAO).
        clazz è il parametro del metodo e rappresenta la classe del DAO che desideri ottenere.
    La mappa ti permette di ottenere il DAO passando la sua classe.
    Esempio d'uso:
    UserDAO userDAO = database.getDAO(UserDAO.class);
    BookDAO bookDAO = database.getDAO(BookDAO.class);
    Questo funziona perché il metodo getDAO utilizza la classe UserDAO.class per cercare e restituire l'istanza associata.
    
    Il tag @SuppressWarnings("unchecked") serve per silenziar gli avvisi del compilatore riguardo al casting non sicuro.
    Il compilatore Java in questo caso ti avvisa che stai facendo un casting di tipo generico,
    il che può essere rischioso se non gestito correttamente, ma in questo caso siamo sicuri che il tipo è corretto.
    Perché serve:
        Quando usiamo il metodo daoMap.get(clazz), la mappa restituirà un DAO generico.
        Tuttavia, vogliamo fare un cast sicuro a UserDAO, BookDAO, ecc., quindi il compilatore segnala un avviso ("unchecked cast"),
        perché non può verificare automaticamente che il tipo sia effettivamente quello giusto.
    Il @SuppressWarnings("unchecked") nasconde questo avviso, dicendo al compilatore "So che cosa sto facendo, non darmi errori".
    */
    @SuppressWarnings("unchecked")
    public <T extends DAO> T getDAO(Class<T> clazz) {
        return (T) daoMap.get(clazz);
    }

    // Da sviluppare i metodi per le query, farò in seguito
    public void testQuery() {
        
    }

    public void close() {
        for (DAO dao : daoMap.values()) {
            dao.close();
        }
    }
}
