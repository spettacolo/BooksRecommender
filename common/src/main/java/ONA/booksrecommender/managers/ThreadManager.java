package ONA.booksrecommender.managers;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager implements ThreadFactory {
    private final boolean daemon;
    private final String baseName;
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Costruttore della classe ThreadManager.
     * Inizializza la factory con un nome di base per i thread e specifica se i thread
     * creati debbano essere di tipo daemon.
     *
     * @param baseName Il prefisso testuale che verrà assegnato al nome di ogni thread creato.
     * @param daemon   Se impostato a {@code true}, i thread creati non impediranno
     * l'arresto della JVM al termine del thread principale.
     */
    public ThreadManager(String baseName, boolean daemon) {
        this.baseName = baseName;
        this.daemon = daemon;
    }

    /**
     * Crea e configura un nuovo {@link Thread} per l'esecuzione di un {@link Runnable}.
     * A ogni nuovo thread viene assegnato un nome univoco composto dal {@code baseName}
     * seguito da un identificatore numerico incrementale gestito in modo thread-safe.
     * Il thread viene inoltre configurato con lo stato daemon specificato nel costruttore.
     *
     * @param r Il {@link Runnable} da eseguire nel nuovo thread.
     * @return Un'istanza di {@link Thread} configurata e pronta per essere avviata.
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, baseName + "-" + counter.incrementAndGet());
        t.setDaemon(daemon);
        return t;
    }
}