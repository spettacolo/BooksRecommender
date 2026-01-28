package ONA.booksrecommender.client.controller;

import ONA.booksrecommender.client.Client;
import ONA.booksrecommender.client.view.UserAreaView;
import ONA.booksrecommender.objects.Book;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecommendationHandler {

    public static void addRecommendation(int bookId, String username, Client client, StackPane parentOverlay, UserAreaView userArea) {
        // 1. Sfondo scuro semi-trasparente
        StackPane selectionOverlay = new StackPane();
        selectionOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);"); // Un po' più scuro per contrasto

        // 2. Contenitore centrale
        VBox container = new VBox(20);
        container.setMaxSize(640, 500); // Alzato un po' per scroll migliore
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: rgb(241, 237, 229); " +
                "-fx-background-radius: 25; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 20, 0, 0, 10);"
        );
        container.setAlignment(Pos.TOP_CENTER);

        // HEADER
        VBox titleArea = new VBox(5);
        titleArea.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Cosa consiglieresti?");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c2c2e;");
        Label subtitle = new Label("Scegli fino a 3 libri per questo volume");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #636e72;");
        titleArea.getChildren().addAll(title, subtitle);

        // GRID CONTENITORE (FlowPane dentro ScrollPane)
        FlowPane booksGrid = new FlowPane();
        booksGrid.setHgap(15);
        booksGrid.setVgap(15);
        booksGrid.setAlignment(Pos.TOP_CENTER);
        booksGrid.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(booksGrid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // RECUPERO DATI
        List<Book> userBooks = fetchUserBooks(username, client);
        userBooks.removeIf(b -> b.getId() == bookId);

        Set<Integer> selectedIds = new HashSet<>();
        Button confirmBtn = new Button("Conferma Consigli (0/3)");
        confirmBtn.setDisable(true);
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle("-fx-background-color: #2d3436; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");

        if (userBooks.isEmpty()) {
            Label empty = new Label("Non hai altri libri nelle tue librerie da consigliare.");
            empty.setStyle("-fx-font-style: italic; -fx-padding: 40 0 0 0;");
            booksGrid.getChildren().add(empty);
        } else {
            for (Book b : userBooks) {
                booksGrid.getChildren().add(createStyledCard(b, selectedIds, confirmBtn));
            }
        }

        // AZIONE CONFERMA
        confirmBtn.setOnAction(e -> {
            if (selectedIds.isEmpty()) {
                confirmBtn.setText("Seleziona almeno un libro");
                return;
            }

            // 1. PULIZIA: Rimuoviamo i vecchi consigli una volta sola prima di iniziare
            client.send("remove_book_advice;" + username + ";" + bookId);

            boolean allOk = true;

            // 2. CICLO DI INVIO: Inviamo una richiesta distinta per ogni libro selezionato
            for (Integer sId : selectedIds) {
                String response = client.send("add_book_advice;" + username + ";" + bookId + ";" + sId);

                if (response == null || !response.contains("OK")) {
                    allOk = false;
                }
            }

            // 3. GESTIONE RISPOSTA FINALE
            if (allOk) {
                parentOverlay.getChildren().remove(selectionOverlay);
                if (userArea != null) {
                    Platform.runLater(userArea::showActivitySection);
                }
            } else {
                confirmBtn.setText("Errore (Limite DB rilevato)");
                confirmBtn.setStyle("-fx-background-color: #d63031; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 15;");

                Timeline pause = new Timeline(new KeyFrame(Duration.seconds(2), ae -> {
                    confirmBtn.setText("Conferma Consigli (" + selectedIds.size() + "/3)");
                    confirmBtn.setStyle("-fx-background-color: #2d3436; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 15;");
                }));
                pause.play();
            }
        });

        container.getChildren().addAll(titleArea, scroll, confirmBtn);
        selectionOverlay.getChildren().add(container);

        // Chiusura se si clicca fuori dal box
        selectionOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == selectionOverlay) parentOverlay.getChildren().remove(selectionOverlay);
        });

        parentOverlay.getChildren().add(selectionOverlay);
    }

    private static StackPane createStyledCard(Book b, Set<Integer> selectedIds, Button confirmBtn) {
        StackPane card = new StackPane();
        card.setPrefSize(100, 150);
        card.setCursor(javafx.scene.Cursor.HAND);

        ImageView iv = new ImageView();
        iv.setFitWidth(100);
        iv.setFitHeight(150);

        // PROTEZIONE IMMAGINE
        String url = b.getCoverImageUrl();
        if (url != null && !url.isBlank()) {
            try {
                iv.setImage(new Image(url, 100, 150, true, true));
            } catch (Exception e) {
                iv.setStyle("-fx-background-color: #dfe6e9;");
            }
        } else {
            iv.setStyle("-fx-background-color: #dfe6e9;");
        }

        Rectangle clip = new Rectangle(100, 150);
        clip.setArcWidth(15); clip.setArcHeight(15);
        iv.setClip(clip);

        clip.setArcWidth(15); clip.setArcHeight(15);
        iv.setClip(clip);

        // Overlay di selezione
        VBox selOverlay = new VBox();
        selOverlay.setAlignment(Pos.CENTER);
        selOverlay.setStyle("-fx-background-color: rgba(23,232,65,0.7); -fx-background-radius: 8;");
        selOverlay.setVisible(false);
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");
        selOverlay.getChildren().add(check);

        card.getChildren().addAll(iv, selOverlay);

        card.setOnMouseClicked(e -> {
            if (selectedIds.contains(b.getId())) {
                // Se il libro è già selezionato, lo rimuovo
                selectedIds.remove(b.getId());
                selOverlay.setVisible(false);
            } else {
                // Se non è selezionato, controllo se ho ancora posto (max 3)
                if (selectedIds.size() < 3) {
                    selectedIds.add(b.getId());
                    selOverlay.setVisible(true);
                } else {
                    // Alert se l'utente prova a selezionare il quarto
                    System.out.println("Puoi selezionare al massimo 3 libri.");
                }
            }
            confirmBtn.setDisable(selectedIds.isEmpty());
            confirmBtn.setText("Conferma Consigli (" + selectedIds.size() + "/3)");
        });

        return card;
    }

    private static List<Book> fetchUserBooks(String username, Client client) {
        List<Book> books = new ArrayList<>();
        String libsResp = client.send("get_user_libraries;" + username);
        if (libsResp == null || libsResp.isBlank() || libsResp.startsWith("ERROR")) return books;

        Set<Integer> allBookIds = new HashSet<>();
        for (String libId : libsResp.split(",")) {
            if (libId.isBlank()) continue;
            String libDetail = client.send("get_user_library;id;" + libId.trim());
            if (libDetail != null && !libDetail.startsWith("ERROR")) {
                String[] parts = libDetail.split(";");
                if (parts.length > 3 && !parts[3].isBlank()) {
                    for (String bid : parts[3].split(",")) {
                        bid = bid.trim();
                        if (!bid.isEmpty() && bid.matches("\\d+")) allBookIds.add(Integer.parseInt(bid));
                    }
                }
            }
        }

        for (Integer id : allBookIds) {
            String bResp = client.send("get_book;id;" + id);
            if (bResp != null && !bResp.startsWith("ERROR")) {
                String[] p = bResp.split(";");
                if (p.length > 6) {
                    books.add(new Book(id, p[1], List.of(p[2]), 0, "", "", p[6], ""));
                }
            }
        }
        return books;
    }
}