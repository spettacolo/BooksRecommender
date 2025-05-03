package ONA.booksrecommender.server.database.dao;

import java.sql.Connection;

public class BookDAO {
    private Connection conn;
    
    public BookDAO (Connection conn){
        this.conn = conn;
    }
}
