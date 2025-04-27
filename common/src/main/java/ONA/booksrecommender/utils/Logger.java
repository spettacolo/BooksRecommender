package ONA.booksrecommender.utils;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger implements Runnable {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public void log(String message) {
        logQueue.offer(message);
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running || !logQueue.isEmpty()) {
            try {
                String message = logQueue.take();
                System.out.println("[LOG] " + message); // oppure scrivere su file
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // ripristina stato di interrupt
            }
        }
    }
}