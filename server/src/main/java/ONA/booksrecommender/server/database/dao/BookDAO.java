package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.utils.Logger;

import java.sql.Connection;

public class BookDAO extends BaseDAO {
    
    public BookDAO (Logger logger, Connection connection){
        super(logger, connection);
    }
    
    @Override
    public void close() {
        super.close();
    }
}
