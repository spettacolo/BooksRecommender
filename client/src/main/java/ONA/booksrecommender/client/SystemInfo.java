package ONA.booksrecommender.client;

public class SystemInfo {

    /**
     * Restituisce la versione del Java Runtime Environment (JRE) attualmente in uso.
     * Utilizza la proprietà di sistema "java.version".
     *
     * @return Una stringa contenente la versione di Java (es. "17.0.1").
     */
    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Restituisce la versione della libreria JavaFX caricata nel sistema.
     * Utilizza la proprietà di sistema "javafx.version".
     *
     * @return Una stringa contenente la versione di JavaFX (es. "17.0.1"),
     * oppure {@code null} se le librerie JavaFX non sono correttamente caricate.
     */
    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }

}
