package ONA.booksrecommender.server;

/**
 * Classe di avvio (Entry Point) per l'applicazione lato Server.
 * Questa classe funge da wrapper statico per delegare l'esecuzione al metodo
 * {@code main} della classe {@link App}, garantendo una separazione tra
 * il bootstrap del sistema e la logica di gestione dell'interfaccia.
 */
public class Main {
    /**
     * Metodo di ingresso principale che avvia il ciclo di vita dell'applicazione.
     * Richiama il punto di ingresso della classe App per inizializzare il menu
     * di controllo del server.
     *
     * @param args Argomenti opzionali passati da riga di comando.
     */
    public static void main(String[] args) {
        App.main(args);
    }
}
