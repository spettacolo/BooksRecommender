package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class BookDAO extends BaseDAO implements AutoCloseable {
    private HttpClient client;
    
    public BookDAO (Logger logger, Connection connection){
        super(logger, connection);
        this.client = HttpClient.newHttpClient();
    }

    public Book getBook(int id) {
        String query = "SELECT * FROM books WHERE book_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null; // nessun libro trovato
                }

                List<String> authors = getBookAuthors(rs.getInt("book_id"));

                String rawQueryData = "intitle:\"" + rs.getString("title") + "\"";
                String encodedQueryData = URLEncoder.encode(rawQueryData, StandardCharsets.UTF_8);
                String imageUrl = getBookImageUrl(encodedQueryData);
                //logger.log(imageUrl);

                return new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        authors,
                        rs.getInt("publish_year"),
                        rs.getString("publishers"),
                        rs.getString("category"),
                        imageUrl
                );
            }
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    /*public Book getBook(String title) {
        //String query = "SELECT * FROM books WHERE title = ?";
        String query = "SELECT * FROM books WHERE title ILIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, title);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            List<String> authors = getBookAuthors(rs.getInt("book_id"));

            String rawQueryData = "intitle:\"" + rs.getString("title") + "\"";
            String encodedQueryData = URLEncoder.encode(rawQueryData, StandardCharsets.UTF_8);
            String imageUrl = getBookImageUrl(encodedQueryData);
            //logger.log(imageUrl);

            return new Book(rs.getInt("book_id"), rs.getString("title"), authors, rs.getInt("publish_year"), rs.getString("publishers"), rs.getString("category"), imageUrl);
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }*/

    public List<Book> getBooks(String title) {
        //String query = "SELECT * FROM books WHERE title = ?";
        String query = "SELECT * FROM books WHERE title ILIKE ? ORDER BY publish_year ASC LIMIT 20";
        logger.log("invio la richiesta ora");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // 💡 Modifica Aggiunta: avvolgi il titolo con i caratteri wildcard
            // Se title è "harry potter", searchPattern sarà "%harry potter%"
            String searchPattern = "%" + title + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/
            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                List<String> authors = getBookAuthors(rs.getInt("book_id"));

                String rawQueryData = "intitle:\"" + rs.getString("title") + "\"";
                String encodedQueryData = URLEncoder.encode(rawQueryData, StandardCharsets.UTF_8);
                // String imageUrl = getBookImageUrl(encodedQueryData);
                String imageUrl;
                try {
                    imageUrl = getBookImageUrl(encodedQueryData);
                } catch (Exception e) {
                    imageUrl = "null";
                }
                // imageUrl = imageUrl==null ? imageUrl : "null";
                // logger.log(imageUrl);
                books.add(new Book(rs.getInt("book_id"), rs.getString("title"), authors, rs.getInt("publish_year"), rs.getString("publishers"), rs.getString("category"), imageUrl));
            }

            return books;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }
    // Proposta che forse mi dimenticherò di proporre: usare le immagini thumbnail (recuperabili nella prima query) nel caso non ci siano quelle HQ
    public List<Book> getBooks(String category, int limit) {
        // se limit è 0 significa che si vuole applicare il filtro di default di 20 risultati per request
        if (limit == 0) limit = 20;
        // String query = "SELECT * FROM books WHERE category ILIKE ? ORDER BY publish_year ASC LIMIT ?" ;
        String query = "SELECT b.book_id, b.title, b.publish_year, b.publishers, b.category, COUNT(lb.book_id) AS frequency_count FROM books b INNER JOIN library_books lb ON b.book_id = lb.book_id WHERE b.category ILIKE ? GROUP BY b.book_id, b.title, b.publish_year, b.publishers, b.category ORDER BY frequency_count DESC LIMIT ?";
        logger.log("invio la richiesta ora");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + category + "%";
            stmt.setString(1, searchPattern);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/
            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                List<String> authors = getBookAuthors(rs.getInt("book_id"));

                String rawQueryData = "intitle:\"" + rs.getString("title") + "\"";
                String encodedQueryData = URLEncoder.encode(rawQueryData, StandardCharsets.UTF_8);
                String imageUrl = getBookImageUrl(encodedQueryData);
                //logger.log(imageUrl);
                books.add(new Book(rs.getInt("book_id"), rs.getString("title"), authors, rs.getInt("publish_year"), rs.getString("publishers"), rs.getString("category"), imageUrl));
            }

            return books;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return null;
        }
    }

    public String getBookImageUrl(int id) {
        // qui ci sarà un passaggio in più: ottenere il titolo del libro con getBook(int id)
        // così da poter cercare l'url immagine con getBookImageUrl(String title)
        Book book = getBook(id);
        String title = book.getTitle();
        return getBookImageUrl(title);
    }

    public String getBookImageUrl(String title) {
        String req = "https://www.googleapis.com/books/v1/volumes?q=" + title;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(req))
                .GET() // Metodo GET
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Decommentare se servono i log
            // System.out.println("Status Code: " + response.statusCode());
            // System.out.println("Response Body:\n" + response.body());

            try {
                // 1. Creare l'ObjectMapper
                ObjectMapper mapper = new ObjectMapper();

                String jsonResponse = response.body();
                // 2. Parsare la stringa JSON in un nodo (albero) di oggetti
                JsonNode rootNode = mapper.readTree(jsonResponse);

                // 3. Navigare fino all'array "items"
                // Se l'array "items" non esiste, itemsNode sarà 'null'.
                JsonNode itemsNode = rootNode.get("items");

                String selfLinkValue = null;

                if (itemsNode != null && itemsNode.isArray() && !itemsNode.isEmpty()) {
                    // 4. Accedere al primo elemento dell'array (indice 0)
                    JsonNode firstItem = itemsNode.get(0);

                    // 5. Estrarre il valore del campo "selfLink"
                    // Se il campo non esiste, selfLinkNode sarà 'null'.
                    JsonNode selfLinkNode = firstItem.get("selfLink");

                    if (selfLinkNode != null) {
                        // 6. Ottenere il valore come stringa
                        selfLinkValue = selfLinkNode.asText();
                        //System.out.println("Valore di selfLink recuperato: " + selfLinkValue);

                        // Seconda request per recuperare l'url dell'immagine
                        HttpRequest secondRequest = HttpRequest.newBuilder()
                                .uri(URI.create(selfLinkValue))
                                .GET()
                                .build();
                        HttpResponse<String> secondResponse = client.send(secondRequest, HttpResponse.BodyHandlers.ofString());
                        String detailedJson = secondResponse.body();
                        ObjectMapper mapper2 = new ObjectMapper();
                        JsonNode detailedRootNode = mapper2.readTree(detailedJson);

                        JsonNode imageLinksNode = detailedRootNode
                                .path("volumeInfo")
                                .path("imageLinks");

                        JsonNode imageNode = imageLinksNode.path("extraLarge");

                        if (imageNode.isMissingNode()) {
                            imageNode = imageLinksNode.path("large");
                        }

                        if (imageNode.isMissingNode()) {
                            imageNode = imageLinksNode.path("medium");
                        }

                        if (imageNode.isMissingNode()) {
                            imageNode = imageLinksNode.path("small");
                        }

                        if (imageNode.isMissingNode()) {
                            imageNode = imageLinksNode.path("thumbnail");
                        }

                        if (imageNode.isMissingNode()) {
                            imageNode = imageLinksNode.path("smallThumbnail");
                        }

                        /*
                        JsonNode imageNode = detailedRootNode
                                .path("volumeInfo")
                                .path("imageLinks")
                                .get("extraLarge");*/

                        if (imageNode != null) {
                            return imageNode.asText();
                        } else {
                            System.out.println("URL immagine non trovato nel dettaglio del volume.");
                        }
                    } else {
                        System.out.println("Il campo 'selfLink' non è stato trovato nel primo elemento.");
                    }
                } else {
                    System.out.println("L'array 'items' non esiste o è vuoto.");
                }
            } catch (IOException e) {
                System.err.println("Errore durante il parsing JSON: " + e.getMessage());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Book> getAuthorBooks(String author) {
        String query = "SELECT ba.book_id " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "WHERE a.author_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, author);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                Book book = getBook(rs.getInt("book_id"));
                books.add(book);
            }

            return books;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Book> getAuthorBooks(String author, int year) {
        String query = "SELECT ba.book_id " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "JOIN books b ON ba.book_id = b.book_id " +
                "WHERE a.author_name = ? AND b.publish_year = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, author);

            ResultSet rs = stmt.executeQuery();

            /*if (!rs.next()) {
                return null;
            }*/

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                Book book = getBook(rs.getInt("book_id"));
                books.add(book);
            }

            return books;
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /*public Book getBook(List<String> authors) {
        String query = "";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            //
        } catch (SQLException e) {
            logger.log("Error during book retrieval: " + e.getMessage());
        }

        return new Book(1, "test", null, 1, "test", "test");
    }*/

    public List<String> getBookAuthors(int id) {
        String authorsQuery = "SELECT a.author_name " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.author_id = ba.author_id " +
                "WHERE ba.book_id = ?";

        List<String> authors = new ArrayList<>();

        try (PreparedStatement authorsStmt = connection.prepareStatement(authorsQuery)) {
            authorsStmt.setInt(1, id);

            ResultSet authorsRs = authorsStmt.executeQuery();

            while (authorsRs.next()) {
                authors.add(authorsRs.getString("author_name"));
            }

            return authors;
        } catch (SQLException e) {
            logger.log("Error during book's authors retrieval: " + e.getMessage());
            //return null;
            return new ArrayList<>(); // TODO: applicare questo metodo di gestione su tutti i metodi DAO e migliorare i controlli per prevenire errori inaspettati
        }
    }

    @Override
    public void close() {
        super.close();
    }
}


/*
https://www.googleapis.com/books/v1/volumes?q=intitle:!Trato%hecho!:%Spanish%for%Real%Life,%Combined%Edition
https://www.googleapis.com/books/v1/volumes?q=intitle:harry+intitle:potter+intitle:e+intitle:il+intitle:calice+intitle:di+intitle:fuoco
https://www.googleapis.com/books/v1/volumes?q=intitle:harry%potter+intitle:e+intitle:il+intitle:calice+intitle:di+intitle:fuoco

https://www.googleapis.com/books/v1/volumes/4dvptppOG8MC


https://developers.google.com/books/docs/v1/using?utm_source=chatgpt.com&hl=it


https://www.digitalocean.com/community/tutorials/java-socket-programming-server-client
 */