package ONA.booksrecommender.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Classe di utilità per la gestione dei file. Include metodi per la lettura e la scrittura di file
 * sia dal filesystem che dal JAR, la scrittura di righe aggiuntive a file esistenti, e la gestione
 * di formati CSV.
*/
public class FileUtils {
    /**
     * Legge tutte le righe di un file e le restituisce come una lista di stringhe.
     * Se il file non esiste nel filesystem, tenta di leggerlo dal JAR.
     *
     * @param filename il nome del file da leggere.
     * @return una lista di stringhe, ciascuna rappresentante una riga del file.
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public static List<String> readLines(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // Prova prima a leggere dal filesystem in data
        File file = new File(filename);
        InputStream inputStream;
        
        if (file.exists()) {
            inputStream = new FileInputStream(file);
        } else {
            // Se non esiste in data, prova a leggere dal JAR
            String jarPath = filename.startsWith("../data/") ? filename.substring(5) : filename;
            inputStream = FileUtils.class.getClassLoader().getResourceAsStream(jarPath);
            if (inputStream == null) {
                throw new IOException("File non trovato: " + filename);
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
    /**
     * Scrive una lista di stringhe in un file, creando il file se non esiste.
     * Se la directory non esiste, viene creata automaticamente.
     *
     * @param filename il nome del file in cui scrivere le righe.
     * @param lines la lista di righe da scrivere nel file.
     * @throws IOException se si verifica un errore durante la scrittura nel file.
     */

    public static void writeLines(String filename, List<String> lines) throws IOException {
        // Scrittura sempre in data
        String filePath = filename;
        ensureDataDirectoryExists(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Aggiunge una riga a un file esistente. Se il file non esiste, viene creato.
     * Se la directory non esiste, viene creata automaticamente.
     *
     * @param filename il nome del file a cui aggiungere la riga.
     * @param line la riga da aggiungere al file.
     * @throws IOException se si verifica un errore durante l'aggiunta della riga.
     */
    public static void appendLine(String filename, String line) throws IOException {
        // Append sempre in data
        String filePath = filename;
        ensureDataDirectoryExists(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8, true))) {
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Verifica che la directory di un file esista, creando le directory necessarie se non esistono.
     *
     * @param filePath il percorso del file.
     */
    private static void ensureDataDirectoryExists(String filePath) {
        File file = new File(filePath);
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Converte un array di stringhe in una rappresentazione CSV, separando i valori con una virgola.
     * Se un valore contiene una virgola, viene racchiuso tra virgolette.
     *
     * @param values gli oggetti da convertire in formato CSV.
     * @return una stringa contenente i valori in formato CSV.
     */
    public static String toCSV(String... values) {
        return String.join(",", Arrays.stream(values)
                .map(v -> v.contains(",") ? "\"" + v + "\"" : v)
                .toArray(String[]::new));
    }

    /**
     * Converte una riga di testo CSV in un array di stringhe. Gestisce correttamente i valori racchiusi
     * tra virgolette.
     *
     * @param line la riga di testo in formato CSV.
     * @return un array di stringhe rappresentante i valori della riga.
     */
    public static String[] fromCSV(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString());
        
        return values.toArray(new String[0]);
    }
}
