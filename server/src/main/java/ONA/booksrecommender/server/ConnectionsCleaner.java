package ONA.booksrecommender.server;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsCleaner implements Runnable {
    private final Map<Socket, Long> activeConnections;
    private final long timeoutMillis;
    private volatile boolean running = true;

    public ConnectionsCleaner(Map<Socket, Long> activeConnections, long timeoutMillis) {
        this.activeConnections = activeConnections;
        this.timeoutMillis = timeoutMillis;
    }

    public void stop() {
        running = false;
    }

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
