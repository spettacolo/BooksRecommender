package ONA.booksrecommender.server.database.dao;

import ONA.booksrecommender.utils.Logger;

public class BookDAO extends BaseDAO {
    
    public BookDAO (Logger logger){
        super(logger);
    }
    
    @Override
    public void close() {
        super.close();
    }
}
