package ONA.booksrecommender.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger implements Runnable {
    private static final String LOGS_DIRECTORY = "logs";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;
    private BufferedWriter writer;
    private String currentLogFileName;

    public Logger() {
        try {
            createLogsDirectory();
            openLogFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLogsDirectory() throws IOException {
        Path logDirPath = Path.of(LOGS_DIRECTORY);
        if (Files.notExists(logDirPath)) {
            Files.createDirectories(logDirPath);
        }
    }

    private void openLogFile() throws IOException {
        String today = LocalDate.now().format(DATE_FORMATTER);
        currentLogFileName = LOGS_DIRECTORY + "/" + today + ".log";
        writer = new BufferedWriter(new FileWriter(currentLogFileName, true)); // Append mode
    }

    public void log(String message) {
        logQueue.offer(message);
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running || !logQueue.isEmpty()) {
                try {
                    String message = logQueue.take();
                    checkDateAndRotateFileIfNeeded();
                    writeLog(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            closeWriter();
        }
    }

    private void writeLog(String message) {
        try {
            writer.write("[" + LocalDate.now() + "] " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkDateAndRotateFileIfNeeded() throws IOException {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String expectedFileName = LOGS_DIRECTORY + "/" + today + ".log";

        if (!expectedFileName.equals(currentLogFileName)) {
            closeWriter();
            openLogFile();
        }
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
