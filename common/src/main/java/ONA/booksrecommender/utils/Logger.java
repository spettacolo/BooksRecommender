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

/**
 * Classe responsabile della gestione dei log dell'applicazione.
 * Implementa {@link Runnable} per operare su un thread dedicato, processando i messaggi
 * in arrivo tramite una coda bloccante ({@link BlockingQueue}).
 * Supporta la rotazione automatica dei file basata sulla data corrente.
 */
public class Logger implements Runnable {
    private static final String LOGS_DIRECTORY = "logs";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;
    private BufferedWriter writer;
    private String currentLogFileName;

    /**
     * Costruttore della classe Logger.
     * Inizializza la directory dei log e apre il file di log relativo alla data odierna.
     */
    public Logger() {
        try {
            createLogsDirectory();
            openLogFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica l'esistenza della cartella dedicata ai log e la crea se non presente.
     * Utilizza il percorso definito nella costante {@code LOGS_DIRECTORY}.
     * * @throws IOException Se si verifica un errore durante la creazione della directory.
     */
    private void createLogsDirectory() throws IOException {
        Path logDirPath = Path.of(LOGS_DIRECTORY);
        if (Files.notExists(logDirPath)) {
            Files.createDirectories(logDirPath);
        }
    }

    /**
     * Inizializza il file di log per la sessione corrente.
     * Genera il nome del file basandosi sulla data attuale e apre uno stream di scrittura
     * in modalità "append" per non sovrascrivere i log precedentemente salvati nello stesso giorno.
     * * @throws IOException Se il file non può essere creato o aperto in scrittura.
     */
    private void openLogFile() throws IOException {
        String today = LocalDate.now().format(DATE_FORMATTER);
        currentLogFileName = LOGS_DIRECTORY + "/" + today + ".log";
        writer = new BufferedWriter(new FileWriter(currentLogFileName, true)); // Append mode
    }

    /**
     * Aggiunge un messaggio alla coda di logging in modo non bloccante.
     * Il messaggio verrà processato e scritto su file dal thread dedicato del Logger.
     *
     * @param message Il messaggio testuale da registrare nel log.
     */
    public void log(String message) {
        logQueue.offer(message);
    }

    /**
     * Segnala al logger di interrompere l'esecuzione.
     * Il thread terminerà solo dopo aver svuotato completamente la coda dei messaggi pendenti.
     */
    public void stop() {
        running = false;
    }

    /**
     * Ciclo principale del thread di logging.
     * Estrae i messaggi dalla coda, verifica la necessità di ruotare il file (se è cambiato il giorno)
     * e scrive i messaggi sia su console che su file fisico.
     */
    @Override
    public void run() {
        try {
            while (running || !logQueue.isEmpty()) {
                try {
                    String message = logQueue.take();
                    checkDateAndRotateFileIfNeeded();
                    System.out.println(message);
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

    /**
     * Scrive effettivamente il messaggio nel file di log corrente.
     * Aggiunge un timestamp alla riga e forza il flush dello stream per garantire la persistenza dei dati.
     *
     * @param message Il messaggio da scrivere.
     */
    private void writeLog(String message) {
        try {
            writer.write("[" + LocalDate.now() + "] " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se la data corrente corrisponde ancora a quella del file di log aperto.
     * In caso di cambio di data (es. superata la mezzanotte), chiude il file attuale
     * e ne apre uno nuovo con la data aggiornata.
     *
     * @throws IOException Se si verifica un errore durante la rotazione dei file.
     */
    private void checkDateAndRotateFileIfNeeded() throws IOException {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String expectedFileName = LOGS_DIRECTORY + "/" + today + ".log";

        if (!expectedFileName.equals(currentLogFileName)) {
            closeWriter();
            openLogFile();
        }
    }

    /**
     * Chiude in sicurezza lo stream di scrittura sul file di log.
     * Viene invocato durante la rotazione del file o alla chiusura definitiva del Logger
     * per rilasciare le risorse del sistema operativo.
     */
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
