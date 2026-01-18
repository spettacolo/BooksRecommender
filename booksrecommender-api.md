# API JSON - Books Recommender Server (ServerFacadeTestJson)

Formati delle richieste e risposte attese

## Struttura generale delle richieste
Tutte le richieste devono essere oggetti JSON con almeno questi campi:

```json
{
  "action": "nome_azione",
  "data": { ... parametri specifici ... }
}
```

## Elenco richieste

| Azione                                 | Richiesta JSON esempio                                                                                  | Risposta attesa (successo)                                                                                     | Note / Errori comuni                                  |
|----------------------------------------|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| `login`                                | `{"action":"login", "data":{"username":"mario","password":"abc123"}}`                                    | `{"status":"ok","type":"login","data":{"code":0,"success":true,"message":"Login effettuato"}}`               | code < 0 → credenziali errate                         |
| `signup` / `sign_up` / `register`      | `{"action":"signup","data":{"username":"new","name":"Mario","surname":"Rossi","fiscalCode":"...","email":"...","password":"..."}}` | `{"status":"ok","type":"signup","data":{"success":true}}`                                                     | success:false se fallisce                             |
| `get_user`                             | `{"action":"get_user","data":{"username":"mario"}}`                                                     | Oggetto `User` completo (serializzato direttamente)                                                            | Utente non trovato → error                            |
| `get_book`                             | `{"action":"get_book","data":{"id":12345}}`                                                             | Oggetto `Book` completo (serializzato direttamente)                                                            | Libro non trovato → error                             |
| `search_books` (per titolo)            | `{"action":"search_books","data":{"type":"title","title":"harry potter"}}`                              | `{"books":[...],"count":n,"type":"title"}`                                                                     | Ricerca LIKE sulla colonna **category** (ereditato dal metodo getBooks) |
| `search_books` (per autore)            | `{"action":"search_books","data":{"type":"author","author":"Rowling","order":"DESC"}}`                  | `{"books":[...],"count":n,"type":"author"}`                                                                    | `order` opzionale (default: ASC)                      |
| `search_books` (per categoria/genere)  | `{"action":"search_books","data":{"type":"category","category":"Romance","limit":20}}`                  | `{"books":[...],"count":n,"type":"category","category":"Romance","limit_applied":20}`                         | **Ricerca sulla colonna `category`** (ILIKE '%Romance%') – ordinamento RANDOM() – limit opzionale |
| `search_books` (top globali)           | `{"action":"search_books","data":{"type":"top","limit":20}}`                                            | `{"books":[...],"count":n,"type":"top","query_type":"top","limit_applied":20}`                                | Top **globali** (ordinati per frequenza nelle librerie) – limit 1–100 (clippato) |
| `get_book_authors`                     | `{"action":"get_book_authors","data":{"bookId":12345}}`                                                 | Array di stringhe (nomi autori)                                                                                |                                                       |
| `get_user_libraries`                   | `{"action":"get_user_libraries","data":{"username":"mario"}}`                                           | Array di oggetti `Library`                                                                                     |                                                       |
| `get_library`                          | `{"action":"get_library","data":{"id":7}}`                                                              | Oggetto `Library` completo                                                                                     | Libreria non trovata → error                          |
| `add_library`                          | `{"action":"add_library","data":{"name":"Da leggere","username":"mario"}}`                              | `{"status":"ok","type":"add_library","data":{"success":true}}`                                                |                                                       |
| `remove_library`                       | `{"action":"remove_library","data":{"id":7}}`                                                           | `{"status":"ok","type":"remove_library","data":{"success":true}}`                                             | Libreria non trovata → error                          |
| `add_book_to_library`                  | `{"action":"add_book_to_library","data":{"libraryId":7,"bookId":12345}}`                                | `{"status":"ok","type":"add_book_to_library","data":{"success":true}}`                                         | Libreria o libro non trovato → error                  |
| `remove_book_from_library`             | `{"action":"remove_book_from_library","data":{"libraryId":7,"bookId":12345}}`                           | `{"status":"ok","type":"remove_book_from_library","data":{"success":true}}`                                    | Libreria o libro non trovato → error                  |
| `add_book_review` / `add_rating`       | `{"action":"add_book_review","data":{"bookId":"12345","userId":"mario","style":4,"content":5,"enjoyment":5,"originality":4,"edition":5,"notes":"Ottimo!"}}` | `{"status":"ok","type":"add_review","data":{"success":true}}`                                                  | Deserializza direttamente in oggetto `Rating`         |
| `get_book_ratings`                     | `{"action":"get_book_ratings","data":{"bookId":12345}}`                                                 | Array di oggetti `Rating`                                                                                      |                                                       |
| `get_user_recommendations`             | `{"action":"get_user_recommendations","data":{"username":"mario"}}`                                     | Array di oggetti `Recommendation`                                                                              | Sinonimo: `get_received_recommendations`              |
| `get_recommendations_made_by_user`     | `{"action":"get_recommendations_made_by_user","data":{"username":"mario"}}`                             | Array di oggetti `Recommendation`                                                                              | Sinonimo: `get_made_recommendations`                  |
| `add_recommendation`                   | `{"action":"add_recommendation","data":{"username":"mario","bookId":"12345","recommendedBookIds":["234","567","890"]}}` | `{"status":"ok","type":"add_recommendation","data":{"success":true}}`                                          | Deserializza in oggetto `Recommendation`              |
| `remove_recommendation`                | `{"action":"remove_recommendation","data":{"username":"mario","bookId":"12345"}}`                       | `{"status":"ok","type":"remove_recommendation","data":{"success":true}}`                                       | Rimuove tutti i consigli di quell'utente per quel libro |

## Pattern risposte più frequenti

1. **Risposta diretta** (get_...)  
   → oggetto o array JSON puro

2. **Risposta standard operazione** (add/remove...)  
   ```json
   {"status":"ok","type":"nome_operazione","data":{"success":true/false, ...altri campi...}}
   ```

3. **Errore**  
   ```json
   {"status":"error","message":"descrizione"}
   ```

4. **Ricerca libri** (`search_books`)  
   ```json
   {
     "books": [...],
     "count": n,
     "type": "title|author|category|top",
     ...metadati opzionali...
   }
   ```

## Note importanti sul sistema di ricerca libri

- **Ricerca per categoria/genere**: usa `"type":"category"` + campo `"category"` → chiama `getBooks(category, limit)` → ricerca **esclusivamente sulla colonna `category`** (ILIKE '%valore%') → ordinamento RANDOM()  
- **Ricerca per titolo**: usa `"type":"title"` + campo `"title"` → chiama `getBooks(title)` → ricerca **sulla colonna `title`**
- **Top**: `"type":"top"` → top **globali** (ordinati per frequenza nelle librerie di tutti gli utenti)  
- Non esiste (per ora) un "top per categoria specifica" e non esisterà caro Nikolai
