package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.objects.Book;
import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;

public class BookDAO extends BaseDAO {
    
    public BookDAO (Logger logger, Connection connection){
        super(logger, connection);
    }

    public Book getBook(String title, String authors) {
        return new Book(1, "test", null, 1, "test", "test");
    }
    
    @Override
    public void close() {
        super.close();
    }
}
