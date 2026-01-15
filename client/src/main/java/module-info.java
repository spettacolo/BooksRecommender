module booksrecommender.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires booksrecommender.common;
    requires java.desktop;
    requires javafx.graphics;
    //requires booksrecommender.client;

    exports ONA.booksrecommender.client;
    exports ONA.booksrecommender.client.view;
}
