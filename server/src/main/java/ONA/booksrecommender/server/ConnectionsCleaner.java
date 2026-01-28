package ONA.booksrecommender.server;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitora e gestisce la pulizia delle connessioni Socket inattive.
 * Implementa {@link Runnable} per operare in background, scansionando periodicamente
 * una mappa di connessioni attive e chiudendo forzatamente quelle che hanno
 * superato il tempo di inattività (timeout) prestabilito.
 */
public class ConnectionsCleaner implements Runnable {
    private final Map<Socket, Long> activeConnections;
    private final long timeoutMillis;
    private volatile boolean running = true;

    /**
     * Costruttore della classe ConnectionsCleaner.
     *
     * @param activeConnections Una mappa thread-safe (solitamente {@link ConcurrentHashMap})
     * che associa i socket attivi al timestamp dell'ultima attività.
     * @param timeoutMillis     Il tempo massimo di inattività espresso in millisecondi
     * prima che una connessione venga considerata orfana.
     */
    public ConnectionsCleaner(Map<Socket, Long> activeConnections, long timeoutMillis) {
        this.activeConnections = activeConnections;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Termina il ciclo di monitoraggio del cleaner.
     * Imposta il flag {@code running} a false, permettendo al thread di completare
     * l'ultima iterazione e chiudersi correttamente.
     */
    public void stop() {
        running = false;
    }

    /**
     * Ciclo di esecuzione principale del cleaner.
     * Ogni 5 secondi esegue una scansione della mappa delle connessioni:
     * <ol>
     * <li>Calcola il tempo trascorso dall'ultima attività per ogni socket.</li>
     * <li>Se il tempo supera {@code timeoutMillis}, chiude il socket e lo rimuove dalla mappa.</li>
     * </ol>
     * Gestisce l'interruzione del thread in conformità con lo standard Java sui thread.
     */
    @Override
    public void run() {
        while (running) {
            long now = System.currentTimeMillis();
            for (Map.Entry<Socket, Long> entry : activeConnections.entrySet()) {
                if (now - entry.getValue() > timeoutMillis) {
                    try {
                        System.out.println("Chiudo connessione inattiva: " + entry.getKey().getInetAddress());
                        entry.getKey().close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    activeConnections.remove(entry.getKey());
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
