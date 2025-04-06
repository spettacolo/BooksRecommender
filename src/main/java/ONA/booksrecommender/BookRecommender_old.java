package ONA.booksrecommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * La classe {@code BookRecommender} è il controller principale dell'applicazione Book Recommender.
 * Fornisce un'interfaccia a menu per consentire agli utenti di cercare libri, visualizzare dettagli,
 * registrarsi, effettuare il login, gestire le proprie librerie, valutare libri e dare raccomandazioni di libri.
 * Questa classe gestisce l'input e l'interazione dell'utente, richiamando i metodi appropriati delle
 * altre classi di gestione come {@code BookManager}, {@code UserManager}, {@code LibraryManager}, 
 * {@code RatingManager} e {@code RecommendationManager}.
 */
public class BookRecommender_old {
    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    private static final UserManager userManager = new UserManager();
    private static final BookManager bookManager = new BookManager();
    private static final LibraryManager libraryManager = new LibraryManager();
    private static final RatingManager ratingManager = new RatingManager();
    private static final RecommendationManager recommendationManager = new RecommendationManager();

    /**
     * Il punto di ingresso principale dell'applicazione.
     * Carica i dati iniziali e avvia il menu principale dell'applicazione.
     *
     * @param args argomenti della linea di comando (non utilizzati).
     */
    public static void main(String[] args) {
        loadData();
        showMainMenu();
    }

    /**
     * Carica i dati iniziali dei libri, utenti, librerie, valutazioni e raccomandazioni dai file.
     */
    private static void loadData() {
        try {
            bookManager.loadBooks();
            userManager.loadUsers();
            libraryManager.loadLibraries();
            ratingManager.loadRatings();
            recommendationManager.loadRecommendations();
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Mostra il menu principale dell'applicazione e gestisce le scelte dell'utente.
     * Consente la ricerca di libri, la visualizzazione di dettagli, la registrazione, il login e,
     * se l'utente è loggato, la gestione delle librerie, la valutazione dei libri e le raccomandazioni.
     */
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== Book Recommender ===");
            System.out.println("1. Search Books");
            System.out.println("2. View Book Details");
            System.out.println("3. Register");
            System.out.println("4. Login");
            if (currentUser != null) {
                System.out.println("5. Create Library");
                System.out.println("6. Add Book To Library");
                System.out.println("7. Add Book Rating");
                System.out.println("8. Add Book Recommendation");
                System.out.println("9. Logout");
            }
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            try {
                switch (choice) {
                    case 0:
                        System.out.println("Goodbye!");
                        return;
                    case 1:
                        searchBooks();
                        break;
                    case 2:
                        viewBookDetails();
                        break;
                    case 3:
                        register();
                        break;
                    case 4:
                        login();
                        break;
                    case 5:
                        if (currentUser != null) createLibrary();
                        break;
                    case 6:
                        if (currentUser != null) addBookToLibrary();
                        break;
                    case 7:
                        if (currentUser != null) addBookRating();
                        break;
                    case 8:
                        if (currentUser != null) addBookRecommendation();
                        break;
                    case 9:
                        if (currentUser != null) logout();
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Permette all'utente di cercare libri in base a titolo, autore o autore e anno di pubblicazione.
     * Mostra una lista dei risultati corrispondenti.
     */
    private static void searchBooks() {
        System.out.println("\n=== Search Books ===");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by Author");
        System.out.println("3. Search by Author and Year");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        List<Book> results = new ArrayList<>();
        switch (choice) {
            case 1:
                System.out.print("Enter title: ");
                String title = scanner.nextLine();
                results = bookManager.searchByTitle(title);
                break;
            case 2:
                System.out.print("Enter author: ");
                String author = scanner.nextLine();
                results = bookManager.searchByAuthor(author);
                break;
            case 3:
                System.out.print("Enter author: ");
                String authorYear = scanner.nextLine();
                System.out.print("Enter year: ");
                int year = scanner.nextInt();
                results = bookManager.searchByAuthorAndYear(authorYear, year);
                break;
        }
        
        if (results.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        
        System.out.println("\nResults:");
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("%d. %s, %s (%d)\n   ID: %s%n", 
            i + 1,
            results.get(i).getTitle(), 
            String.join(", ", results.get(i).getAuthors()), 
            results.get(i).getPublicationYear(), 
            results.get(i).getId());
        }
    }

    /**
     * Visualizza i dettagli di un libro specifico inserendo il suo ID.
     * Mostra titolo, autori, anno di pubblicazione, editore, categoria e valutazioni.
     * Inoltre, visualizza eventuali raccomandazioni correlate.
     */
    private static void viewBookDetails() {
        System.out.print("Enter book ID: ");
        String bookId = scanner.nextLine();
        
        Book book = bookManager.getBook(bookId);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }
        
        System.out.println("\n=== Book Details ===");
        System.out.println("Title: " + book.getTitle());
        System.out.println("Authors: " + String.join(", ", book.getAuthors()));
        System.out.println("Year: " + book.getPublicationYear());
        System.out.println("Publisher: " + book.getPublisher());
        System.out.println("Category: " + book.getCategory());
        
        // Show ratings
        Map<String, Double> aggregateRatings = ratingManager.getAggregateRatings(bookId);
        if (!aggregateRatings.isEmpty()) {
            System.out.println("\nAverage Ratings:");
           /* System.out.printf("Style: %.1f%n", aggregateRatings.get("style"));
            System.out.printf("Content: %.1f%n", aggregateRatings.get("content"));
            System.out.printf("Enjoyment: %.1f%n", aggregateRatings.get("enjoyment"));
            System.out.printf("Originality: %.1f%n", aggregateRatings.get("originality"));
            System.out.printf("Edition: %.1f%n", aggregateRatings.get("edition"));*/
            System.out.printf("Final Score: %.1f%n", aggregateRatings.get("finalScore"));
        } else {
            System.out.println("\nNo ratings available.");
        }
        
        // Show recommendations
        Map<String, Integer> recommendedBooks = recommendationManager.getTopRecommendedBooks(bookId);
        if (!recommendedBooks.isEmpty()) {
            System.out.println("\nRecommended Books:");
            recommendedBooks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    Book recBook = bookManager.getBook(entry.getKey());
                    System.out.printf("%s (recommended by %d users)%n", 
                        recBook.getTitle(), entry.getValue());
                });
        } else {
            System.out.println("\nNo recommendations available.");
        }
    }

    /**
     * Registra un nuovo utente chiedendo nome, cognome, codice fiscale, email, ID utente e password.
     * Se l'ID utente esiste già, mostra un messaggio di errore.
     *
     * @throws IOException se si verifica un errore durante la registrazione dell'utente.
     */
    private static void register() throws IOException {
        System.out.println("\n=== Register ===");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Surname: ");
        String surname = scanner.nextLine();
        String fiscalCode;
        do {
            System.out.print("Fiscal Code: ");
            fiscalCode = scanner.nextLine();
            if (fiscalCode.length() != 16) {
                System.out.print("Invalid Fiscal Code. Please enter a 16-character Fiscal Code: ");
            }
        } while (fiscalCode.length() != 16);
        String email;
        do {
            System.out.print("Email: ");
            email = scanner.nextLine();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                System.out.println("Invalid email format. Please enter a valid email address.");
            }
        } while (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
        System.out.print("User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        if (userManager.userExists(userId)) {
            System.out.println("User ID already exists.");
            return;
        }
        
        User user = new User(name, surname, fiscalCode, email, userId, password);
        userManager.registerUser(user);
        System.out.println("Registration successful!");
    }

    /**
     * Consente a un utente registrato di accedere al sistema. Chiede l'ID utente e la password.
     * Se le credenziali sono corrette, l'utente viene autenticato e può accedere alle funzionalità
     * per utenti autenticati.
     */
    private static void login() {
        if (currentUser != null) {
            System.out.println("Already logged in.");
            return;
        }

        System.out.println("\n=== Login ===");
        System.out.print("User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userManager.authenticateUser(userId, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + user.getName() + "!");
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    /**
     * Effettua il logout dell'utente corrente.
     * Se nessun utente è loggato, mostra un messaggio di errore.
     */
    private static void logout() {
        if (currentUser == null) {
            System.out.println("Not logged in.");
            return;
        }
        
        System.out.println("Goodbye, " + currentUser.getName() + "!");
        currentUser = null;
    }

    /**
     * Consente all'utente di creare una nuova libreria personale.
     * Richiede il nome della libreria e controlla se l'utente ha già una libreria con lo stesso nome.
     * 
     * @throws IOException se si verifica un errore durante la creazione della libreria.
     */
    private static void createLibrary() throws IOException {
        System.out.println("\n=== Create Library ===");
        System.out.print("Library name: ");
        String name = scanner.nextLine();

        List<Library> libraries = libraryManager.getUserLibraries(currentUser.getUserId());
        boolean libraryExists = libraries.stream()
                        .anyMatch(lib -> lib.getName().equalsIgnoreCase(name));
        if (libraryExists) {
            System.out.println("Library with this name already exists.");
            return;
        }

        Library library = new Library(name, currentUser.getUserId(), new ArrayList<>());
        libraryManager.createLibrary(library);
        System.out.println("Library created successfully!");
    }

    /**
     * Permette di aggiungere un libro a una libreria dell'utente.
     * Richiede il nome della libreria e l'ID del libro. Se il libro o la libreria non esistono,
     * viene mostrato un messaggio di errore.
     * 
     * @throws IOException se si verifica un errore durante l'aggiunta del libro alla libreria.
     */
    private static void addBookToLibrary() throws IOException {
        System.out.println("\n=== Add Book to Library ===");
        System.out.print("Enter library name: ");
        String libraryName = scanner.nextLine();
        System.out.print("Enter book ID: ");
        String bookId = scanner.nextLine();

        List<Library> libraries = libraryManager.getUserLibraries(currentUser.getUserId());
        Library library = libraries.stream()
                   .filter(lib -> lib.getName().equalsIgnoreCase(libraryName))
                   .findFirst()
                   .orElse(null);
        if (library == null) {
            System.out.println("Library not found.");
            return;
        }

        Book book = bookManager.getBook(bookId);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (libraryManager.hasBook(currentUser.getUserId(), bookId)) {
            System.out.println("Book already exists in the library.");
            return;
        }

        libraryManager.addBookToLibrary(currentUser.getUserId(), libraryName, bookId);
        System.out.println("Book added to library successfully!");
    }

    /**
     * Permette di aggiungere un libro a una libreria dell'utente.
     * Richiede il nome della libreria e l'ID del libro. Se il libro o la libreria non esistono,
     * viene mostrato un messaggio di errore.
     * 
     * @throws IOException se si verifica un errore durante l'aggiunta del libro alla libreria.
     */
    private static void addBookRating() throws IOException {
        System.out.println("\n=== Add Book Rating ===");
        System.out.print("Enter book ID: ");
        String bookId = scanner.nextLine();

        Book book = bookManager.getBook(bookId);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (!libraryManager.hasBook(currentUser.getUserId(), bookId)) {
            System.out.println("You must add this book to your library before rating it.");
            return;
        }

        if (ratingManager.hasUserRated(currentUser.getUserId(), bookId)) {
            System.out.println("You have already rated this book. Your rating will be updated.");
        }

        System.out.println("Rate from 1 to 5:");
        int style, content, enjoyment, originality, edition;
        String notes;
        
        do {
            System.out.print("Style (1-5): ");
            style = scanner.nextInt();
            if (style < 1 || style > 5) {
                System.out.println("Invalid rating. Please enter a value between 1 and 5.");
            }
        } while (style < 1 || style > 5);
        
        do {
            System.out.print("Content (1-5): ");
            content = scanner.nextInt();
            if (content < 1 || content > 5) {
                System.out.println("Invalid rating. Please enter a value between 1 and 5.");
            }
        } while (content < 1 || content > 5);
        
        do {
            System.out.print("Enjoyment (1-5): ");
            enjoyment = scanner.nextInt();
            if (enjoyment < 1 || enjoyment > 5) {
                System.out.println("Invalid rating. Please enter a value between 1 and 5.");
            }
        } while (enjoyment < 1 || enjoyment > 5);
        
        do {
            System.out.print("Originality (1-5): ");
            originality = scanner.nextInt();
            if (originality < 1 || originality > 5) {
                System.out.println("Invalid rating. Please enter a value between 1 and 5.");
            }
        } while (originality < 1 || originality > 5);
        
        do {
            System.out.print("Edition (1-5): ");
            edition = scanner.nextInt();
            if (edition < 1 || edition > 5) {
                System.out.println("Invalid rating. Please enter a value between 1 and 5.");
            }
        } while (edition < 1 || edition > 5);
        scanner.nextLine(); // Consume newline
        
        do {
            System.out.print("Notes (optional): ");
            notes = scanner.nextLine();
            if (notes.length() > 256) {
                System.out.println("Notes too long. Please enter up to 256 characters.");
            }
        } while (notes.length() > 256);

        Rating rating = new Rating(currentUser.getUserId(), bookId, style, content, 
                                 enjoyment, originality, edition, notes);
        ratingManager.addRating(rating);
        System.out.println("Rating added successfully!");
    }

    /**
     * Permette all'utente di raccomandare fino a 3 libri correlati a un libro specifico.
     * L'utente deve aver aggiunto il libro alla propria libreria prima di poter fare raccomandazioni.
     * Non è possibile raccomandare lo stesso libro.
     * 
     * @throws IOException se si verifica un errore durante l'aggiunta delle raccomandazioni.
     */
    private static void addBookRecommendation() throws IOException {
        System.out.println("\n=== Add Book Recommendation ===");
        System.out.print("Enter book ID to recommend for: ");
        String bookId = scanner.nextLine();

        Book book = bookManager.getBook(bookId);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (!libraryManager.hasBook(currentUser.getUserId(), bookId)) {
            System.out.println("You must add this book to your library before recommending related books.");
            return;
        }

        if (recommendationManager.hasUserRecommended(currentUser.getUserId(), bookId)) {
            System.out.println("You have already made recommendations for this book. Your recommendations will be updated.");
        }

        List<String> recommendedBookIds = new ArrayList<>();
        System.out.println("Enter up to 3 book IDs to recommend (enter blank line to finish):");
        
        for (int i = 0; i < 3; i++) {
            System.out.print("Book ID " + (i + 1) + ": ");
            String recommendedBookId = scanner.nextLine();
            if (recommendedBookId.isEmpty()) {
                break;
            }
            
            if (bookManager.getBook(recommendedBookId) == null) {
                System.out.println("Invalid book ID. Skipping...");
                continue;
            }
            
            if (recommendedBookId.equals(bookId)) {
                System.out.println("Cannot recommend the same book. Skipping...");
                continue;
            }
            
            recommendedBookIds.add(recommendedBookId);
        }

        if (recommendedBookIds.isEmpty()) {
            System.out.println("No valid recommendations provided.");
            return;
        }

        Recommendation recommendation = new Recommendation(currentUser.getUserId(), 
                                                        bookId, recommendedBookIds);
        recommendationManager.addRecommendation(recommendation);
        System.out.println("Recommendations added successfully!");
    }
}
