# Tutorial per inviare richieste al server

### Come inviare le richieste
#### Codice
``` java
package ONA.booksrecommender.client;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String host = "localhost"; // oppure l'IP del server, tipo "192.168.1.100"
        int porta = 1234;

        try (Socket socket = new Socket(host, porta)) {
            System.out.println("Connesso al server su " + host + ":" + porta);
            
            // QUI VIENE INVIATA LA REQUEST
            String risposta = getString(socket, "get_book;title;harry potter");
            System.out.println("Server response: " + risposta);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getString(Socket socket, String richiesta) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println(richiesta);
        return in.readLine();
    }
}
```
#### Esempi di query
- "get_user;luigi"
- "get_book;title;1 is one"
- "add_library;test;luigi"
- "get_book;title;harry potter"

## Utenti
### get_user 
Params: username (inteso come userId)
### login
Params: username, password
### sign_up
Params: username, name, surname, fiscalCode. email, password

## Libri
### get_book
Params: type, value
#### Types
- id (restituisce un solo libro)
- title (restituisce tutti i risultati associati alla query di ricerca del titolo)
- author (da completare)
- year (da completare)

## Librerie
### get_user_library
Params: type, value
#### Types
- id
- name
### get_user_libraries
Params: id (id dell'utente)
### add_library
Params: library (inteso come nome), username


## Recensioni
### get_book_reviews
TODO
### add_book_review
TODO

## Consigli
### get_book_advices
TODO
### add_book_advice
TODO