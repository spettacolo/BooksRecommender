package ONA.booksrecommender.client;
        
public class Main {
    /**
     * Punto di ingresso principale dell'eseguibile.
     * Questa classe funge da wrapper per l'avvio dell'applicazione JavaFX,
     * delegando l'esecuzione al metodo {@code main} della classe {@link App}.
     * È necessaria in alcuni contesti di deployment (come la creazione di JAR)
     * per evitare problemi di configurazione del toolkit JavaFX all'avvio.
     *
     * @param args Argomenti passati da riga di comando.
     */
    public static void main(String[] args) {
        App.main(args);
    }
}

