package ONA.booksrecommender.client.view;

import ONA.booksrecommender.client.Client;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserAreaView extends VBox {

    private VBox dynamicContent;
    private String username;
    private RootView root;
    private String[] userParts;
    private String currentTab = "activity";

    /**
     * Costruttore della vista Area Utente.
     * Inizializza il profilo recuperando i dati anagrafici dal server e configura
     * il layout principale basato su tab per navigare tra attività e informazioni.
     *
     * @param root     Il riferimento alla {@link RootView} per la gestione globale.
     * @param username Lo username dell'utente loggato di cui mostrare il profilo.
     */
    public UserAreaView(RootView root, String username) {
        this.root = root;
        this.username = username;
        this.setSpacing(0);
        this.setAlignment(Pos.TOP_LEFT);

        Client client = root.getClient();
        String risposta = client.send("get_user;" + username + ";" + true);
        this.userParts = (risposta != null && !risposta.startsWith("ERROR"))
                ? risposta.split(";")
                : new String[]{username, "N/A", "N/A", "N/A"};

        setupUI();
    }

    /**
     * Configura l'interfaccia grafica iniziale dell'area utente.
     * Crea l'header di benvenuto, il menu a tab (Attività/Info) e il contenitore
     * dinamico per il caricamento dei contenuti specifici delle sezioni.
     */
    private void setupUI() {
        HBox header = new HBox();
        header.getStyleClass().add("home-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label welcomeLabel = new Label("Ciao " + username);
        welcomeLabel.getStyleClass().add("header-title");
        header.getChildren().add(welcomeLabel);

        // Menu Tabs
        HBox tabsBox = new HBox(30);
        tabsBox.setPadding(new Insets(0, 40, 0, 40));
        Label tabActivity = new Label("La tua attività");
        Label tabInfo = new Label("Informazioni personali");
        tabActivity.getStyleClass().addAll("tab-label", "tab-label-active");
        tabInfo.getStyleClass().add("tab-label");

        tabActivity.setOnMouseClicked(e -> switchTab("activity", tabActivity, tabInfo));
        tabInfo.setOnMouseClicked(e -> switchTab("info", tabInfo, tabActivity));

        tabsBox.getChildren().addAll(tabActivity, tabInfo);

        // Container dinamico per il contenuto
        dynamicContent = new VBox(20);
        dynamicContent.setPadding(new Insets(30, 40, 30, 40));

        // ScrollPane configurato per essere "invisibile"
        ScrollPane scroll = new ScrollPane(dynamicContent);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        this.getChildren().addAll(header, tabsBox, scroll);
        showActivitySection();
    }

    /**
     * Gestisce il cambio di visualizzazione tra i diversi tab del profilo.
     * Aggiorna gli stili grafici delle etichette per riflettere lo stato attivo
     * e richiama i metodi di popolamento del contenuto dinamico.
     *
     * @param tab      Il nome del tab da attivare ("activity" o "info").
     * @param active   L'etichetta del tab cliccato.
     * @param inactive L'etichetta del tab da disattivare.
     */
    private void switchTab(String tab, Label active, Label inactive) {
        if (currentTab.equals(tab)) return;
        currentTab = tab;
        active.getStyleClass().add("tab-label-active");
        inactive.getStyleClass().remove("tab-label-active");
        if (tab.equals("activity")) showActivitySection(); else showInfoSection();
    }

    /**
     * Carica e visualizza la sezione dedicata all'attività dell'utente.
     * Recupera dal server l'elenco delle recensioni scritte e dei consigli forniti,
     * generando per ciascun libro una "SuperCard" interattiva.
     */
    public void showActivitySection() {
        dynamicContent.getChildren().clear();
        String rispostaRecensioni = root.getClient().send("get_user_reviews;" + username);

        if (rispostaRecensioni == null || rispostaRecensioni.equals("NO_REVIEWS") || rispostaRecensioni.startsWith("ERROR")) {
            Label placeholder = new Label("Non hai ancora effettuato attività.");
            placeholder.getStyleClass().add("default-label");
            dynamicContent.getChildren().add(placeholder);
            return;
        }

        String rispostaAdvices = root.getClient().send("get_advices_made_by_user;" + username);
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(30);
        flowPane.setVgap(30);

        for (String rev : rispostaRecensioni.split("\\|")) {
            if (rev.isBlank()) continue;
            String[] fields = rev.split(";");
            if (fields.length < 9) continue;

            String bookId = fields[1];
            String score = fields[7];
            String note = "";
            try {
                note = new String(Base64.getDecoder().decode(fields[8]), StandardCharsets.UTF_8);
                if (note.equalsIgnoreCase("EMPTY")) note = "";
            } catch (Exception ignored) {}

            List<String> suggestedIds = null;
            if (rispostaAdvices != null && !rispostaAdvices.contains("NO_RECOMMENDATIONS")) {
                for (String block : rispostaAdvices.split("\\|")) {
                    if (block.startsWith(bookId + ";")) {
                        String[] parts = block.split(";");
                        if (parts.length > 1) suggestedIds = Arrays.asList(parts[1].split(","));
                        break;
                    }
                }
            }
            flowPane.getChildren().add(createSuperCard(bookId, score, note, suggestedIds));
        }
        dynamicContent.getChildren().add(flowPane);
    }

    /**
     * Crea una card complessa che riassume l'attività dell'utente su un singolo libro.
     * La card include:
     * <ul>
     * <li>Dettagli del libro (copertina, titolo).</li>
     * <li>Valutazione e commento dell'utente (decodificato da Base64).</li>
     * <li>Elenco dei libri consigliati dall'utente per quel volume.</li>
     * <li>Controlli di eliminazione con sistema di countdown (5 secondi) per recensioni e singoli consigli.</li>
     * </ul>
     *
     * @param mainId       L'ID del libro recensito.
     * @param score        Il voto assegnato dall'utente.
     * @param note         Il testo della recensione (già decodificato).
     * @param suggestedIds Lista di ID dei libri consigliati in associazione.
     * @return Un {@link StackPane} contenente la card e i relativi overlay di eliminazione.
     */
    private StackPane createSuperCard(String mainId, String score, String note, List<String> suggestedIds) {
        String mainBookData = root.getClient().send("get_book;id;" + mainId);
        String[] b = mainBookData.split(";");
        String title = (b.length > 1) ? b[1] : "Titolo N/A";
        String mainCoverUrl = (b.length > 6) ? b[6] : "";

        // --- UI DELLA CARD ---
        VBox cardContent = new VBox(12);
        cardContent.setPadding(new Insets(18));
        cardContent.setPrefWidth(380);
        cardContent.setMinHeight(280);
        // Colore azzurro coerente con il tuo stile, ma con un'ombra più morbida
        cardContent.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f2f2f7); -fx-background-radius: 22; -fx-border-radius: 22; -fx-border-color: rgba(0,0,0,0.08); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 18, 0, 0, 6);");

        // Header Card
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Copertina principale con angoli arrotondati
        ImageView mainCover = new ImageView(new Image(mainCoverUrl, 70, 105, true, true));
        javafx.scene.shape.Rectangle clipMain = new javafx.scene.shape.Rectangle(70, 105);
        clipMain.setArcWidth(15); clipMain.setArcHeight(15);
        mainCover.setClip(clipMain);
        mainCover.setCursor(javafx.scene.Cursor.HAND);
        mainCover.setOnMouseClicked(e -> openBookDetails(Integer.parseInt(mainId)));

        VBox titleBox = new VBox(6);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1c1c1e;");
        lblTitle.setWrapText(true); lblTitle.setMaxWidth(240);

        Label lblScore = new Label("Voto: " + score + "/5");
        lblScore.setStyle("-fx-font-weight: 700; -fx-text-fill: #1c1c1e; -fx-background-color: rgba(28,28,30,0.1); -fx-padding: 4 12; -fx-background-radius: 14; -fx-font-size: 11px;");
        titleBox.getChildren().addAll(lblTitle, lblScore);
        header.getChildren().addAll(mainCover, titleBox);

        // Testo Recensione
        Label lblNote = new Label(note.isEmpty() ? "Nessun commento scritto." : "\"" + note + "\"");
        lblNote.setStyle("-fx-font-style: italic; -fx-text-fill: #1c1c1e; -fx-font-size: 13px; -fx-opacity: 0.85;");
        lblNote.setWrapText(true);
        lblNote.setMinHeight(40);

        // Sezione Consigliati
        VBox adviceSection = new VBox(8);
        if (suggestedIds != null && !suggestedIds.isEmpty()) {
            Label lblHint = new Label("LIBRI CONSIGLIATI:");
            lblHint.setStyle("-fx-font-size: 10px; -fx-font-weight: 900; -fx-text-fill: #2d3436; -fx-opacity: 0.8;");

            HBox imagesBox = new HBox(12);
            imagesBox.setAlignment(Pos.CENTER_LEFT);

            for (String sId : suggestedIds) {
                if (sId == null || sId.isBlank()) continue;
                String sData = root.getClient().send("get_book;id;" + sId);
                String[] sb = sData.split(";");
                if (sb.length > 6) {
                    // --- CONTENITORE DELLA MINI-COPERTINA ---
                    StackPane miniCardRoot = new StackPane();

                    // 1. Immagine del libro
                    ImageView iv = new ImageView(new Image(sb[6], 48, 72, true, true));
                    javafx.scene.shape.Rectangle clipSmall = new javafx.scene.shape.Rectangle(48, 72);
                    clipSmall.setArcWidth(10); clipSmall.setArcHeight(10);
                    iv.setClip(clipSmall);
                    iv.setCursor(javafx.scene.Cursor.HAND);

                    // 2. Overlay Rosso per l'eliminazione
                    VBox miniDeleteOverlay = new VBox();
                    miniDeleteOverlay.setAlignment(Pos.CENTER);
                    miniDeleteOverlay.setStyle("-fx-background-color: rgba(226, 26, 26, 0.9); -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 3);");
                    miniDeleteOverlay.setVisible(false);

                    Label miniTimerLabel = new Label("5");
                    miniTimerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                    Label undoLabel = new Label("annulla");
                    undoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px; -fx-underline: true; -fx-cursor: hand;");
                    miniDeleteOverlay.getChildren().addAll(miniTimerLabel, undoLabel);

                    miniCardRoot.getChildren().addAll(iv, miniDeleteOverlay);

                    // --- LOGICA DEL TIMER ---
                    AtomicInteger miniSecondsLeft = new AtomicInteger(5);
                    Timeline miniTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                        miniSecondsLeft.getAndDecrement();
                        miniTimerLabel.setText(String.valueOf(miniSecondsLeft.get()));
                        if (miniSecondsLeft.get() <= 0) {
                            root.getClient().send("remove_book_advice;" + username + ";" + mainId + ";" + sId);
                            showActivitySection();
                        }
                    }));
                    miniTimeline.setCycleCount(5);

                    // Click sulla copertina: avvia il countdown
                    iv.setOnMouseClicked(e -> {
                        miniDeleteOverlay.setVisible(true);
                        miniTimeline.playFromStart();
                    });

                    // Click su "annulla": ferma tutto
                    undoLabel.setOnMouseClicked(e -> {
                        e.consume(); // Impedisce al click di passare alla copertina sotto
                        miniTimeline.stop();
                        miniSecondsLeft.set(5);
                        miniTimerLabel.setText("5");
                        miniDeleteOverlay.setVisible(false);
                    });

                    Tooltip.install(miniCardRoot, new Tooltip("Clicca per rimuovere: " + sb[1]));

                    imagesBox.getChildren().add(miniCardRoot);
                }
            }
            adviceSection.getChildren().addAll(lblHint, imagesBox);
        }

        cardContent.getChildren().addAll(header, lblNote, adviceSection);

        // --- UI ELIMINAZIONE (Overlay Countdown) ---
        VBox deleteOverlay = new VBox(10);
        deleteOverlay.setAlignment(Pos.CENTER);
        deleteOverlay.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(226,26,26,0.95), rgba(180,20,20,0.95)); -fx-background-radius: 22;");
        deleteOverlay.setVisible(false);

        Label lblTimer = new Label("Eliminazione in 5s...");
        lblTimer.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Button btnUndo = new Button("Annulla");
        btnUndo.setStyle("-fx-text-fill: #E21A1A; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");

        deleteOverlay.getChildren().addAll(lblTimer, btnUndo);

        // Tasto "-" di eliminazione
        StackPane deleteIcon = new StackPane();
        Label minus = new Label("-");
        minus.setStyle("-fx-text-fill: #E21A1A; -fx-font-weight: bold; -fx-font-size: 22px; -fx-padding: 0 0 2 0;");
        deleteIcon.getChildren().addAll(minus);

        // Posizionamento relativo allo StackPane
        deleteIcon.setTranslateX(175);
        deleteIcon.setTranslateY(-125);
        deleteIcon.setCursor(javafx.scene.Cursor.HAND);

        StackPane rootCard = new StackPane(cardContent, deleteOverlay, deleteIcon);

        // LOGICA COUNTDOWN
        AtomicInteger secondsLeft = new AtomicInteger(5);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            secondsLeft.getAndDecrement();
            lblTimer.setText("Eliminazione in " + secondsLeft.get() + "s...");
            if (secondsLeft.get() <= 0) {
                root.getClient().send("remove_book_review;" + mainId + ";" + username);
                root.getClient().send("remove_book_advice;" + username + ";" + mainId);
                showActivitySection();
            }
        }));
        timeline.setCycleCount(5);

        deleteIcon.setOnMouseClicked(e -> {
            deleteOverlay.setVisible(true);
            deleteIcon.setVisible(false);
            timeline.playFromStart();
        });

        btnUndo.setOnMouseClicked(e -> {
            timeline.stop();
            secondsLeft.set(5);
            lblTimer.setText("Eliminazione in 5s...");
            deleteOverlay.setVisible(false);
            deleteIcon.setVisible(true);
        });

        return rootCard;
    }

    /**
     * Apre l'overlay dei dettagli di un libro partendo dal suo identificativo.
     *
     * @param id L'identificativo numerico del libro da visualizzare.
     */
    private void openBookDetails(int id) {
        BookDetails details = new BookDetails(id, username);
        StackPane overlay = details.createOverlay();
        Platform.runLater(() -> root.getMainContentContainer().getChildren().add(overlay));
    }

    /**
     * Carica e visualizza la sezione delle informazioni personali.
     * Mostra i dati anagrafici dell'utente recuperati durante l'inizializzazione
     * e fornisce l'opzione di logout per terminare la sessione corrente.
     */
    private void showInfoSection() {
        dynamicContent.getChildren().clear();
        VBox infoContainer = new VBox(15);
        infoContainer.getChildren().addAll(
                createInfoRow("Username", userParts[0]),
                createInfoRow("Nome", userParts[1]),
                createInfoRow("Cognome", userParts[2]),
                createInfoRow("Email", userParts[3])
        );
        Label logout = new Label("Esci dal tuo account");
        logout.setStyle("-fx-text-fill: #E21A1A; -fx-cursor: hand; -fx-padding: 25 0 0 0; -fx-font-weight: bold;");
        logout.setOnMouseClicked(e -> {
            root.setUsername("");
            root.showUnloggedSidebar();
            root.showHome();
        });
        dynamicContent.getChildren().addAll(infoContainer, logout);
    }

    /**
     * Crea una riga formattata per la visualizzazione di una coppia etichetta-valore
     * all'interno della sezione informazioni.
     *
     * @param label L'etichetta del campo (es. "Email").
     * @param value Il valore corrispondente recuperato dal profilo.
     * @return Un {@link HBox} contenente le informazioni stilizzate.
     */
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(15);
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-min-width: 120; -fx-text-fill: #8E8E93FF;");
        Label val = new Label(value != null ? value : "N/D");
        val.setStyle("-fx-text-fill: #f2f2f7;");
        row.getChildren().addAll(lbl, val);
        return row;
    }
}