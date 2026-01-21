module booksrecommender.server {
    requires java.sql;
    requires booksrecommender.common;
    requires java.xml.crypto;
    requires java.net.http;
    requires spring.security.crypto;
    requires javafx.graphics;
    requires javafx.base;
    requires com.google.gson;

    exports ONA.booksrecommender.server;
}