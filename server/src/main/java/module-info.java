module booksrecommender.server {
    requires java.sql;
    requires booksrecommender.common;
    requires java.xml.crypto;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    exports ONA.booksrecommender.server;
}
