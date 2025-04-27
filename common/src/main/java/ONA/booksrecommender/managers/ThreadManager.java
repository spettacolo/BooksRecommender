package ONA.booksrecommender.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager implements ThreadFactory {
    private final boolean daemon;
    private final String baseName;
    private final AtomicInteger counter = new AtomicInteger(0);

    public ThreadManager(String baseName, boolean daemon) {
        this.baseName = baseName;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, baseName + "-" + counter.incrementAndGet());
        t.setDaemon(daemon);
        return t;
    }
}