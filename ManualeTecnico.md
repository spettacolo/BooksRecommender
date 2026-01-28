# Books Recommender

## Manuale Tecnico

**Università degli Studi dell’Insubria**  
**Laurea Triennale in Informatica**  

**Anno Accademico:** 2025

**Autori:** Fasolo Alex, Como Nicholas Maria

---

## 1. Introduzione

Il progetto **BooksRecommender** è un sistema client-server per la gestione e raccomandazione di libri.  
La soluzione è strutturata in tre moduli principali:

- **server**: gestione della logica applicativa, database e API testuali via socket.
- **common**: classi condivise (oggetti dominio, utility, manager).
- **client**: interfaccia utente (placeholder nel presente manuale).

Il sistema comunica tramite richieste testuali con separatore `;`, interpretate dal server tramite una **facade** centrale.

---

## 2. Architettura generale

### 2.1 Struttura moduli

- **booksrecommender.server**  
  Modulo server, dipende da `booksrecommender.common` e librerie standard Java (SQL, HTTP, crypto, JavaFX).
- **booksrecommender.common**  
  Modulo condiviso con oggetti dominio e utility.
- **booksrecommender.client**  
  Modulo client (placeholder).

### 2.2 Tecnologia

- Linguaggio: **Java**
- Comunicazione: **Socket TCP**
- DB: **PostgreSQL** (connessione remota)
- Pattern principali: **Facade**, **DAO**, **Layered Architecture**

---

## 3. Modulo Server

### 3.1 Componenti principali

#### `App`
Classe con menu CLI per avvio/arresto del server.

Funzioni:
- avvio thread server
- gestione stop e join
- interfaccia di controllo locale

#### `Server`
Classe che implementa `Runnable`.  
Gestisce:
- ascolto sulla porta **1234**
- accettazione richieste
- instanziazione `Database`, `ServerFacade`, logger

#### `ServerFacade`
Punto centrale per tutte le richieste client.
Riceve stringhe del tipo:

```
command;param1;param2;...
```

Interpreta il comando e utilizza i DAO per accedere ai dati.  
È responsabile anche della formattazione delle risposte.

Comandi:
- `get_user;username`
- `login;username;password`
- `sign_up;username;name;surname;fiscalCode;email;password`
- `get_book;type;value` (type: `id`, `list`, `title`, `author`, `authors`, `top`)
- `get_user_library;type;value` (type: `id`, `name`)
- `get_user_libraries;userId`
- `add_library;libraryName;username`
- `remove_library;libraryId`
- `add_book_to_library;libraryId;bookId` (o `libraryName;username;bookId`)
- `remove_book_from_library;libraryId;bookId` (o `libraryName;username;bookId`)
- `get_book_reviews;bookId`
- `get_user_reviews;username`
- `add_book_review;bookId;username;style;content;liking;originality;edition;encoded_notes`
- `remove_book_review;bookId;username`
- `get_book_advices;bookId`
- `get_advices_made_by_user;username`
- `add_book_advice;username;bookId;rec_id1,rec_id2,rec_id3`
- `remove_book_advice;username;bookId`

---

### 3.2 Persistenza dati (Database + DAO)

#### `Database`
Gestisce:
- connessione a PostgreSQL (URL, user, password)
- registrazione e caching DAO
- chiusura centralizzata dei DAO

Istanzia:
- `UserDAO`
- `BookDAO`
- `LibraryDAO`
- `RatingDAO`
- `RecommendationDAO`

#### DAO principali

- **BookDAO**: recupero e ricerca libri.
- **UserDAO**: login, signup, recupero profili.
- **LibraryDAO**: librerie personali (collezioni di libri).
- **RatingDAO**: gestione valutazioni (parziale).
- **RecommendationDAO**: consigli (parziale).

#### Schema relazionale

```sql
CREATE TABLE public.authors (
  author_id integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  author_name character varying NOT NULL UNIQUE,
  CONSTRAINT authors_pkey PRIMARY KEY (author_id)
);

CREATE TABLE public.book_authors (
  book_id integer NOT NULL,
  author_id integer NOT NULL,
  CONSTRAINT book_authors_pkey PRIMARY KEY (book_id, author_id),
  CONSTRAINT fk_book_id FOREIGN KEY (book_id) REFERENCES public.books(book_id),
  CONSTRAINT fk_author_id FOREIGN KEY (author_id) REFERENCES public.authors(author_id)
);

CREATE TABLE public.book_images (
  book_id integer NOT NULL,
  image_url text,
  CONSTRAINT book_images_pkey PRIMARY KEY (book_id),
  CONSTRAINT book_images_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.books(book_id)
);

CREATE TABLE public.books (
  title text NOT NULL,
  authors text NOT NULL,
  description text,
  category text,
  publishers text,
  price numeric,
  publish_month text,
  publish_year integer,
  book_id integer GENERATED ALWAYS AS IDENTITY NOT NULL UNIQUE,
  CONSTRAINT books_pkey PRIMARY KEY (book_id)
);

CREATE TABLE public.libraries (
  library_id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  library_name text,
  username text NOT NULL,
  CONSTRAINT libraries_pkey PRIMARY KEY (library_id),
  CONSTRAINT fk_libraries_username FOREIGN KEY (username) REFERENCES public.users(username)
);

CREATE TABLE public.library_books (
  book_id integer NOT NULL,
  library_id bigint NOT NULL,
  CONSTRAINT library_books_pkey PRIMARY KEY (book_id, library_id),
  CONSTRAINT library_books_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.books(book_id),
  CONSTRAINT library_books_library_id_fkey FOREIGN KEY (library_id) REFERENCES public.libraries(library_id)
);

CREATE TABLE public.ratings (
  username text NOT NULL,
  style integer CHECK (style >= 1 AND style <= 5),
  content integer CHECK (content >= 1 AND content <= 5),
  liking integer CHECK (liking >= 1 AND liking <= 5),
  originality integer CHECK (originality >= 1 AND originality <= 5),
  edition integer CHECK (edition >= 1 AND edition <= 5),
  book_id integer NOT NULL,
  notes text,
  CONSTRAINT ratings_pkey PRIMARY KEY (username, book_id),
  CONSTRAINT rating_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.books(book_id),
  CONSTRAINT rating_username_fkey FOREIGN KEY (username) REFERENCES public.users(username)
);

CREATE TABLE public.recommendations (
  username text NOT NULL,
  book_id integer NOT NULL,
  book_recommended_id integer GENERATED ALWAYS AS IDENTITY NOT NULL UNIQUE,
  CONSTRAINT recommendations_pkey PRIMARY KEY (username, book_id, book_recommended_id),
  CONSTRAINT recommendations_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.books(book_id),
  CONSTRAINT recommendations_username_fkey FOREIGN KEY (username) REFERENCES public.users(username)
);

CREATE TABLE public.users (
  username text NOT NULL,
  name text NOT NULL,
  surname text NOT NULL,
  tax_code text NOT NULL,
  email text NOT NULL,
  password text NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY (username)
);
```

#### Entity-Relationship Diagram
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" contentStyleType="text/css" data-diagram-type="DESCRIPTION" height="695px" preserveAspectRatio="none" style="width:1320px;height:695px;background:#FFFFFF;" version="1.1" viewBox="0 0 1320 695" width="1320px" zoomAndPan="magnify"><defs/><g><!--cluster Sistema Gestione Libri--><g class="cluster" data-qualified-name="Sistema Gestione Libri" data-source-line="10" id="ent0006"><rect fill="none" height="573" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:1;" width="841.45" x="354.04" y="7"/><text fill="#000000" font-family="sans-serif" font-size="14" font-weight="bold" lengthAdjust="spacing" textLength="178.5205" x="685.5047" y="21.9951">Sistema Gestione Libri</text></g><!--entity LI--><g class="entity" data-qualified-name="Sistema Gestione Libri.LI" data-source-line="11" id="ent0007"><ellipse cx="465.8504" cy="57.0036" fill="#F1F1F1" rx="29.8804" ry="14.5236" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="38.0146" x="446.8431" y="61.6521">Login</text></g><!--entity REG--><g class="entity" data-qualified-name="Sistema Gestione Libri.REG" data-source-line="12" id="ent0008"><ellipse cx="465.849" cy="121.9958" fill="#F1F1F1" rx="65.779" ry="15.5558" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="95.5254" x="418.0863" y="126.6442">Registrazione</text></g><!--entity VH--><g class="entity" data-qualified-name="Sistema Gestione Libri.VH" data-source-line="13" id="ent0009"><ellipse cx="465.8466" cy="189.9993" fill="#F1F1F1" rx="73.4966" ry="17.0993" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="115.0625" x="408.3153" y="194.6478">Visualizza Home</text></g><!--entity RL--><g class="entity" data-qualified-name="Sistema Gestione Libri.RL" data-source-line="14" id="ent0010"><ellipse cx="774.9059" cy="301.0032" fill="#F1F1F1" rx="62.4659" ry="14.8932" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="86.6318" x="731.59" y="305.6516">Ricerca Libri</text></g><!--entity RT--><g class="entity" data-qualified-name="Sistema Gestione Libri.RT" data-source-line="15" id="ent0011"><ellipse cx="465.8492" cy="331.9998" fill="#F1F1F1" rx="76.7992" ry="17.7598" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="123.0674" x="404.3155" y="336.6483">Ricerca per Titolo</text></g><!--entity RA--><g class="entity" data-qualified-name="Sistema Gestione Libri.RA" data-source-line="16" id="ent0012"><ellipse cx="465.8532" cy="260.9966" fill="#F1F1F1" rx="80.1332" ry="18.4266" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="130.9902" x="400.3581" y="265.6451">Ricerca per Autore</text></g><!--entity VD--><g class="entity" data-qualified-name="Sistema Gestione Libri.VD" data-source-line="17" id="ent0013"><ellipse cx="1082.8168" cy="518.0034" fill="#F1F1F1" rx="96.6668" ry="21.7334" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="168.6836" x="998.475" y="522.6518">Visualizza Dettagli Libro</text></g><!--entity VR--><g class="entity" data-qualified-name="Sistema Gestione Libri.VR" data-source-line="18" id="ent0014"><ellipse cx="774.9141" cy="528.9988" fill="#F1F1F1" rx="88.2441" ry="20.0488" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="149.7549" x="700.0367" y="533.6473">Visualizza Recensioni</text></g><!--entity VC--><g class="entity" data-qualified-name="Sistema Gestione Libri.VC" data-source-line="19" id="ent0015"><ellipse cx="774.908" cy="368.9996" fill="#F1F1F1" rx="79.198" ry="18.2396" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="128.7822" x="710.5169" y="373.648">Visualizza Consigli</text></g><!--entity AP--><g class="entity" data-qualified-name="Sistema Gestione Libri.AP" data-source-line="21" id="ent0016"><ellipse cx="465.8452" cy="542.003" fill="#F1F1F1" rx="95.8152" ry="21.563" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="166.79" x="382.4501" y="546.6515">Accesso Area Personale</text></g><!--entity CL--><g class="entity" data-qualified-name="Sistema Gestione Libri.CL" data-source-line="22" id="ent0017"><ellipse cx="465.8533" cy="401.0027" fill="#F1F1F1" rx="68.6633" ry="16.1327" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="102.9902" x="414.3582" y="405.6511">Consiglia Libro</text></g><!--entity REC--><g class="entity" data-qualified-name="Sistema Gestione Libri.REC" data-source-line="23" id="ent0018"><ellipse cx="465.8518" cy="468.9964" fill="#F1F1F1" rx="72.6318" ry="16.9264" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="112.9365" x="409.3836" y="473.6448">Recensisci Libro</text></g><!--entity CRB--><g class="entity" data-qualified-name="Sistema Gestione Libri.CRB" data-source-line="24" id="ent0019"><ellipse cx="774.9111" cy="436.9962" fill="#F1F1F1" rx="63.8311" ry="15.1662" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="90.3438" x="729.7392" y="441.6447">Crea Libreria</text></g><!--entity UNL--><g class="entity" data-qualified-name="UNL" data-source-line="4" id="ent0002"><ellipse cx="73.6997" cy="355.35" fill="#F1F1F1" rx="8" ry="8" style="stroke:#181818;stroke-width:0.5;"/><path d="M73.6997,363.35 L73.6997,390.35 M60.6997,371.35 L86.6997,371.35 M73.6997,390.35 L60.6997,405.35 M73.6997,390.35 L86.6997,405.35" fill="none" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="135.3994" x="6" y="419.8451">Utente non loggato</text></g><!--entity UL--><g class="entity" data-qualified-name="UL" data-source-line="5" id="ent0003"><ellipse cx="254.7188" cy="427.35" fill="#F1F1F1" rx="8" ry="8" style="stroke:#181818;stroke-width:0.5;"/><path d="M254.7188,435.35 L254.7188,462.35 M241.7188,443.35 L267.7188,443.35 M254.7188,462.35 L241.7188,477.35 M254.7188,462.35 L267.7188,477.35" fill="none" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="104.6377" x="202.4" y="491.8451">Utente loggato</text></g><!--entity DB--><g class="entity" data-qualified-name="DB" data-source-line="6" id="ent0004"><ellipse cx="1278.1585" cy="410.4969" fill="#F1F1F1" rx="8" ry="8" style="stroke:#181818;stroke-width:0.5;"/><path d="M1278.1585,418.4969 L1278.1585,445.4969 M1265.1585,426.4969 L1291.1585,426.4969 M1278.1585,445.4969 L1265.1585,460.4969 M1278.1585,445.4969 L1291.1585,460.4969" fill="none" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="66.8008" x="1244.7581" y="474.992">Database</text><text fill="#000000" font-family="sans-serif" font-size="14" font-style="italic" lengthAdjust="spacing" textLength="69.3369" x="1243.49" y="398.6951">«System»</text></g><!--reverse link UNL to UL--><g class="link" data-entity-1="ent0002" data-entity-2="ent0003" data-link-type="extension" data-source-line="8" id="lnk5"><path d="M158.5669,418.734 C178.5069,426.764 183.33,428.69 202.12,436.25" fill="none" id="UNL-backto-UL" style="stroke:#181818;stroke-width:1;"/><polygon fill="none" points="141.87,412.01,156.3256,424.2996,160.8083,413.1683,141.87,412.01" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to LI--><g class="link" data-entity-1="ent0002" data-entity-2="ent0007" data-link-type="association" data-source-line="27" id="lnk20"><path d="M93.01,346.73 C114.82,303.87 154.41,234.01 202.4,185 C256.12,130.14 276.12,120.74 346.04,89 C375.8,75.49 412.43,66.7 437.08,61.81" fill="none" id="UNL-LI" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to REG--><g class="link" data-entity-1="ent0002" data-entity-2="ent0008" data-link-type="association" data-source-line="28" id="lnk21"><path d="M105.69,346.5 C130.21,317.34 166.23,277.27 202.4,247 C260.53,198.34 276.82,185.93 346.04,155 C367.93,145.22 393.41,137.68 415.14,132.29" fill="none" id="UNL-REG" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to VH--><g class="link" data-entity-1="ent0002" data-entity-2="ent0009" data-link-type="association" data-source-line="29" id="lnk22"><path d="M130.74,346.38 C184.14,310.96 267.78,258.98 346.04,225 C367.34,215.75 391.82,208.08 412.99,202.29" fill="none" id="UNL-VH" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to RL--><g class="link" data-entity-1="ent0002" data-entity-2="ent0010" data-link-type="association" data-source-line="30" id="lnk23"><path d="M106.7,423.55 C130.92,450.99 166.1,487.4 202.4,513 C335.19,606.68 404.89,623.77 561.67,581 C608.95,568.1 629.13,565.55 656.67,525 C705.19,453.55 631.88,399.77 686.67,333 C695.98,321.65 709.7,314.33 723.51,309.6" fill="none" id="UNL-RL" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to VD--><g class="link" data-entity-1="ent0002" data-entity-2="ent0013" data-link-type="association" data-source-line="31" id="lnk24"><path d="M79.29,423.38 C91.4,504.91 132.13,688 253.72,688 C253.72,688 253.72,688 775.91,688 C896.82,688 1014.61,585.28 1061.06,539.53" fill="none" id="UNL-VD" style="stroke:#181818;stroke-width:1;"/></g><!--link RT to RL--><g class="link" data-entity-1="ent0011" data-entity-2="ent0010" data-link-type="dependency" data-source-line="34" id="lnk25"><path d="M536.91,324.93 C591.43,319.42 660.1503,312.4821 711.0103,307.3521" fill="none" id="RT-to-RL" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="716.98,306.75,707.624,303.6734,712.0052,307.2518,708.4269,311.633,716.98,306.75" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="310.0669">«extend»</text></g><!--link RA to RL--><g class="link" data-entity-1="ent0012" data-entity-2="ent0010" data-link-type="dependency" data-source-line="35" id="lnk26"><path d="M536.53,270.08 C591.91,277.29 662.3503,286.4644 713.3603,293.1144" fill="none" id="RA-to-RL" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="719.31,293.89,710.9026,288.7601,714.352,293.2436,709.8684,296.693,719.31,293.89" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="274.0669">«extend»</text></g><!--link VR to VD--><g class="link" data-entity-1="ent0014" data-entity-2="ent0013" data-link-type="dependency" data-source-line="38" id="lnk27"><path d="M862.98,531.48 C873.15,531.69 883.38,531.87 893.16,532 C921.15,532.36 928.2,533.55 956.16,532 C969.64,531.25 977.9194,530.6436 991.7994,529.2636" fill="none" id="VR-to-VD" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="997.77,528.67,988.4184,525.58,992.7945,529.1647,989.2099,533.5408,997.77,528.67" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="894.16" y="528.0669">«extend»</text></g><!--link VC to VD--><g class="link" data-entity-1="ent0015" data-entity-2="ent0013" data-link-type="dependency" data-source-line="39" id="lnk28"><path d="M817.54,384.73 C832.1,390.49 848.48,397.25 863.16,404 C928.5,434.05 996.9976,470.8595 1039.9876,494.5995" fill="none" id="VC-to-VD" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="1045.24,497.5,1039.2951,489.6477,1040.863,495.0829,1035.4278,496.6509,1045.24,497.5" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="894.16" y="415.0669">«extend»</text></g><!--link UL to AP--><g class="link" data-entity-1="ent0003" data-entity-2="ent0016" data-link-type="association" data-source-line="42" id="lnk29"><path d="M307.13,484.91 C319.69,491.24 333.2,497.66 346.04,503 C365.28,511 386.8,518.49 406.18,524.72" fill="none" id="UL-AP" style="stroke:#181818;stroke-width:1;"/></g><!--link UL to CL--><g class="link" data-entity-1="ent0003" data-entity-2="ent0017" data-link-type="association" data-source-line="43" id="lnk30"><path d="M307.41,443.16 C341.77,433.96 386.45,421.99 419.43,413.16" fill="none" id="UL-CL" style="stroke:#181818;stroke-width:1;"/></g><!--link UL to REC--><g class="link" data-entity-1="ent0003" data-entity-2="ent0018" data-link-type="association" data-source-line="44" id="lnk31"><path d="M307.41,459.97 C333.78,461.48 366.24,463.34 394.82,464.98" fill="none" id="UL-REC" style="stroke:#181818;stroke-width:1;"/></g><!--link CL to VC--><g class="link" data-entity-1="ent0017" data-entity-2="ent0015" data-link-type="dependency" data-source-line="47" id="lnk32"><path d="M529.36,394.49 C579.42,389.27 643.7223,382.5721 696.1023,377.1121" fill="none" id="CL-to-VC" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="702.07,376.49,692.7038,373.4446,697.0969,377.0084,693.5332,381.4015,702.07,376.49" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="63.0068" x="592.67" y="379.0669">«include»</text></g><!--link REC to VR--><g class="link" data-entity-1="ent0018" data-entity-2="ent0014" data-link-type="dependency" data-source-line="48" id="lnk33"><path d="M509.2,482.93 C533.18,490.38 563.85,499.19 591.67,505 C624.62,511.88 655.413,516.4528 687.223,520.2828" fill="none" id="REC-to-VR" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="693.18,521,684.7227,515.9528,688.2159,520.4023,683.7664,523.8955,693.18,521" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="63.0068" x="592.67" y="501.0669">«include»</text></g><!--link CL to CRB--><g class="link" data-entity-1="ent0017" data-entity-2="ent0019" data-link-type="dependency" data-source-line="49" id="lnk34"><path d="M527.88,408.16 C582.89,414.6 657.1908,423.3113 710.9208,429.6113" fill="none" id="CL-to-CRB" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="716.88,430.31,708.4071,425.2891,711.914,429.7277,707.4754,433.2347,716.88,430.31" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="412.0669">«extend»</text></g><!--link REC to CRB--><g class="link" data-entity-1="ent0018" data-entity-2="ent0019" data-link-type="dependency" data-source-line="50" id="lnk35"><path d="M530.5,476.94 C567.53,480.09 614.98,481.59 656.67,475 C686.47,470.29 713.4139,461.4042 736.6339,452.4242" fill="none" id="REC-to-CRB" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="742.23,450.26,732.3931,449.7756,737.5666,452.0635,735.2787,457.237,742.23,450.26" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="471.0669">«extend»</text></g><!--link RL to DB--><g class="link" data-entity-1="ent0010" data-entity-2="ent0004" data-link-type="association" data-source-line="53" id="lnk36"><path d="M823.95,310.52 C904.96,327.04 1073.49,363.46 1212.49,407 C1222.25,410.06 1232.66,413.84 1242.21,417.53" fill="none" id="RL-DB" style="stroke:#181818;stroke-width:1;"/></g><!--link VD to DB--><g class="link" data-entity-1="ent0013" data-entity-2="ent0004" data-link-type="association" data-source-line="54" id="lnk37"><path d="M1127.69,498.49 C1162.34,483.07 1210.04,461.85 1242.16,447.57" fill="none" id="VD-DB" style="stroke:#181818;stroke-width:1;"/></g><!--link CRB to DB--><g class="link" data-entity-1="ent0019" data-entity-2="ent0004" data-link-type="association" data-source-line="55" id="lnk38"><path d="M839.14,436.37 C945.86,435.3 1158.29,433.19 1242.04,432.35" fill="none" id="CRB-DB" style="stroke:#181818;stroke-width:1;"/></g><?plantuml-src RLF1Rjim3BtxAuXSsXxQ3nY208uTj0MChN0Itrc5SuKgKY0fm9fj_pvAT0BFoKtcUtma7sb3FoD41rxt7n5ssfEAsjaYV6fxG8zxEA3wn9xMySiG91XjRwWeK4NdORQDP2E1THQCwtkCRWOOOFkzcJ3-GozWZH7VCL0ErqkOpzTV8T9-iIWAre3vdyV7H2ykbM6srec1y4mXzKhGw7UlP_2x0BXlNA_jGr9hNjDtIptJFPuICyVRrNC2EXsEQFJfXF3YzW9rBpb5A_8AHLPIcd7yG1usEZhZ1DnCmVB8imt5oacjccB4tkYiBW9TFU6qfCW6RbYgT-q4hnoZh25enMWYb4fH20vAJmXlv8EpQAIByYqHpcaugbt9P58mg75RxQhAMKakHSbhb9nsMJ1ontsQxAk6wojWGjeVRo_lONYBzyD7TliCDADuBV8hNhFO08Ex76utyFIqO3vyusEXNqpPyR4mKDu49esUxUTmr-Q4hfugTTLDuFR8et5pyzbgUMPljyFSmvRkuDKgSvIfE5Uh3-9mrKZ9AfVKYR8Z6MirTDDUGKCIh-FAYo7f1fHxu4kmV2lYoF07mlsFGvgCRmpuvrBEU_QA7f9Sdg5U9c_gopE9o_llFm00?></g></svg>

---

### 3.3 Comunicazione

Il protocollo è **testuale** con separatore `;`.

Esempio:
```
login;utente;password
```

Risposta:
```
LOGIN;0
```

---

### 3.4 Logging

Il logging è gestito da `common/utils/Logger`:

- coda `BlockingQueue`
- file di log in `/logs/YYYY-MM-DD.log`
- rotazione automatica giornaliera
- output su console + file

---

## 4. Modulo Common

Contiene classi condivise:

### 4.1 Oggetti dominio

#### `Book`
Rappresenta un libro:
- `id`
- `title`
- `authors` (lista)
- `publicationYear`
- `publisher`
- `category`
- `coverImageUrl`
- `description`

Include metodi per:
- parsing e serializzazione CSV
- gestione dati complessi (lista autori)

#### `User`
Rappresenta un utente:
- `userId`
- `name`
- `surname`
- `fiscalCode`
- `email`
- `password`

#### `Library`
Rappresenta una libreria:
- `id`
- `name`
- `userId`
- `books` (lista)

#### `Rating`
Rappresenta una valutazione (recensione):
- `userId`
- `bookId`
- `style`
- `content`
- `enjoyment`
- `originality`
- `edition`
- `finalScore`
- `notes`

#### `Recommendation`
Rappresenta un consiglio di un libro:
- `userId`
- `bookId`
- `recommendedBookIds` (lista)

### 4.2 Utility

#### `Logger`
Gestione asincrona log.

### 4.3 Managers

Il package `booksrecommender.managers` contiene i componenti responsabili della gestione delle risorse e dell'infrastruttura di basso livello dell'applicazione.

#### `ThreadManager`
Questa classe implementa l'interfaccia **`ThreadFactory`** di Java. Viene utilizzata per centralizzare la creazione dei thread, garantendo che ogni thread generato dal sistema segua standard precisi di configurazione.

**Caratteristiche principali:**
* **Naming Strategico**: Assegna a ogni thread un nome identificativo basato su un `baseName` e un contatore atomico (`AtomicInteger`). Questo facilita il debugging permettendo di distinguere i thread nei log (es. `Server-1`, `Logger-2`).
* **Gestione Daemon**: Tramite il flag `daemon`, permette di definire se i thread debbano essere "di servizio". I thread daemon vengono terminati automaticamente dalla JVM al termine dei thread utente, garantendo una chiusura pulita del software.
* **Thread Safety**: L'incremento del contatore è gestito tramite `AtomicInteger`, garantendo l'univocità dei nomi anche in scenari di creazione thread simultanea.

**Dettaglio implementativo:**
Il metodo `newThread(Runnable r)` esegue i seguenti passaggi:
1. Incrementa il contatore atomico per ottenere un ID univoco.
2. Crea l'oggetto `Thread` assegnando il nome risultante dalla concatenazione `baseName + "-" + counter`.
3. Configura lo stato di daemon (true/false) in base alle impostazioni del costruttore.
4. Restituisce il thread pronto per l'avvio.

---

## 5. Modulo Client (placeholder)

Il client è un modulo JavaFX che comunica con il server via socket TCP.  
Nel repository è presente una classe `Client` con metodo `send(String request)` per inviare richieste.

**Nota:** in questo manuale la parte client è volutamente sintetica, come richiesto.

---

## 6. Complessità computazionale (server e common)

Le complessità sono stime asintotiche basate sui metodi effettivamente presenti nel codice e sulle query SQL mostrate.  
Si assume la presenza di indici sulle chiavi primarie; in caso contrario le ricerche su DB diventano O(n).

### Modulo **server**

#### `Server` (gestione socket)
- **Accettazione connessioni**: O(1) per singola accept.
- **Gestione richiesta**: dipende dal comando inoltrato a `ServerFacade`.
- **Spazio**: O(1) per client attivo (stream e buffer).

#### `ServerFacade`
Smista le richieste ai DAO.  
La complessità è dominata dalle operazioni su DB.  
Esempi concreti dai DAO:

#### `BookDAO`
- `getBook(id)`: **O(1)** tempo (lookup per PK), **O(1)** spazio.
- `getBooks(ids)`: **O(k)** tempo (k id), **O(k)** spazio.
- `getBooks(title)`: **O(n)** tempo (ricerca `ILIKE`), **O(k)** spazio per i risultati (max 20).

#### `UserDAO`
- `getUser(userId, login)`: **O(1)** tempo, **O(1)** spazio.

#### `LibraryDAO`
- `getLibrary(id)`:  
  - lettura lista book_id: **O(k)**  
  - recupero dettagli di ciascun libro: **O(k)**  
  - totale: **O(k)** tempo, **O(k)** spazio.
- `getLibraries(username)`:  
  - lettura librerie utente: **O(m)**  
  - per ciascuna libreria chiama `getLibrary`: **O(m·k)**  
  - totale: **O(m·k)** tempo, **O(m·k)** spazio.

#### `RatingDAO`
- `getRating(bookId, username)`: **O(1)** tempo, **O(1)** spazio.
- `getRatings(bookId)` o `getRatings(username)`: **O(r)** tempo, **O(r)** spazio (r = recensioni restituite).

#### `RecommendationDAO`
- `getRecommendations(bookId)`: **O(r)** tempo, **O(r)** spazio (r = consigli per libro).
- `getRecommendationsMadeBy(username)`: **O(r)** tempo, **O(r)** spazio (r = consigli generati dall’utente).
- `addRecommendation(rec)`: **O(1)** tempo, **O(1)** spazio (inserimento singolo).
- `removeRecommendations(username, bookId)`: **O(r)** tempo, **O(1)** spazio (r = record rimossi, dipende da query).

#### `Database`
- Inizializzazione e caching DAO: **O(1)** tempo/spazio rispetto al volume dati.

---

### Modulo **common**

#### Oggetti dominio (`Book`, `User`, `Library`, `Rating`, `Recommendation`)
- Metodi getter e costruzione oggetti: **O(1)** tempo, **O(1)** spazio.
- Parsing e serializzazione CSV (es. in `Book`): **O(a)** tempo dove *a* = numero autori, **O(a)** spazio.

#### `ThreadManager`
- `newThread(Runnable r)`: **O(1)** tempo, **O(1)** spazio.

#### `Logger` (asynchronous logging)
- Inserimento in coda: **O(1)** tempo.
- Scrittura su file: **O(1)** per singola riga.
- Spazio: **O(q)** dove *q* è la dimensione della `BlockingQueue`.

---

## 7. Use Case Diagram

<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" contentStyleType="text/css" data-diagram-type="DESCRIPTION" height="695px" preserveAspectRatio="none" style="width:1320px;height:695px;background:#FFFFFF;" version="1.1" viewBox="0 0 1320 695" width="1320px" zoomAndPan="magnify"><defs/><g><!--cluster Sistema Gestione Libri--><g class="cluster" data-qualified-name="Sistema Gestione Libri" data-source-line="10" id="ent0006"><rect fill="none" height="573" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:1;" width="841.45" x="354.04" y="7"/><text fill="#000000" font-family="sans-serif" font-size="14" font-weight="bold" lengthAdjust="spacing" textLength="178.5205" x="685.5047" y="21.9951">Sistema Gestione Libri</text></g><!--entity LI--><g class="entity" data-qualified-name="Sistema Gestione Libri.LI" data-source-line="11" id="ent0007"><ellipse cx="465.8504" cy="57.0036" fill="#F1F1F1" rx="29.8804" ry="14.5236" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="38.0146" x="446.8431" y="61.6521">Login</text></g><!--entity REG--><g class="entity" data-qualified-name="Sistema Gestione Libri.REG" data-source-line="12" id="ent0008"><ellipse cx="465.849" cy="121.9958" fill="#F1F1F1" rx="65.779" ry="15.5558" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="95.5254" x="418.0863" y="126.6442">Registrazione</text></g><!--entity VH--><g class="entity" data-qualified-name="Sistema Gestione Libri.VH" data-source-line="13" id="ent0009"><ellipse cx="465.8466" cy="189.9993" fill="#F1F1F1" rx="73.4966" ry="17.0993" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="115.0625" x="408.3153" y="194.6478">Visualizza Home</text></g><!--entity RL--><g class="entity" data-qualified-name="Sistema Gestione Libri.RL" data-source-line="14" id="ent0010"><ellipse cx="774.9059" cy="301.0032" fill="#F1F1F1" rx="62.4659" ry="14.8932" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="86.6318" x="731.59" y="305.6516">Ricerca Libri</text></g><!--entity RT--><g class="entity" data-qualified-name="Sistema Gestione Libri.RT" data-source-line="15" id="ent0011"><ellipse cx="465.8492" cy="331.9998" fill="#F1F1F1" rx="76.7992" ry="17.7598" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="123.0674" x="404.3155" y="336.6483">Ricerca per Titolo</text></g><!--entity RA--><g class="entity" data-qualified-name="Sistema Gestione Libri.RA" data-source-line="16" id="ent0012"><ellipse cx="465.8532" cy="260.9966" fill="#F1F1F1" rx="80.1332" ry="18.4266" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="130.9902" x="400.3581" y="265.6451">Ricerca per Autore</text></g><!--entity VD--><g class="entity" data-qualified-name="Sistema Gestione Libri.VD" data-source-line="17" id="ent0013"><ellipse cx="1082.8168" cy="518.0034" fill="#F1F1F1" rx="96.6668" ry="21.7334" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="168.6836" x="998.475" y="522.6518">Visualizza Dettagli Libro</text></g><!--entity VR--><g class="entity" data-qualified-name="Sistema Gestione Libri.VR" data-source-line="18" id="ent0014"><ellipse cx="774.9141" cy="528.9988" fill="#F1F1F1" rx="88.2441" ry="20.0488" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="149.7549" x="700.0367" y="533.6473">Visualizza Recensioni</text></g><!--entity VC--><g class="entity" data-qualified-name="Sistema Gestione Libri.VC" data-source-line="19" id="ent0015"><ellipse cx="774.908" cy="368.9996" fill="#F1F1F1" rx="79.198" ry="18.2396" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="128.7822" x="710.5169" y="373.648">Visualizza Consigli</text></g><!--entity AP--><g class="entity" data-qualified-name="Sistema Gestione Libri.AP" data-source-line="21" id="ent0016"><ellipse cx="465.8452" cy="542.003" fill="#F1F1F1" rx="95.8152" ry="21.563" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="166.79" x="382.4501" y="546.6515">Accesso Area Personale</text></g><!--entity CL--><g class="entity" data-qualified-name="Sistema Gestione Libri.CL" data-source-line="22" id="ent0017"><ellipse cx="465.8533" cy="401.0027" fill="#F1F1F1" rx="68.6633" ry="16.1327" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="102.9902" x="414.3582" y="405.6511">Consiglia Libro</text></g><!--entity REC--><g class="entity" data-qualified-name="Sistema Gestione Libri.REC" data-source-line="23" id="ent0018"><ellipse cx="465.8518" cy="468.9964" fill="#F1F1F1" rx="72.6318" ry="16.9264" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="112.9365" x="409.3836" y="473.6448">Recensisci Libro</text></g><!--entity CRB--><g class="entity" data-qualified-name="Sistema Gestione Libri.CRB" data-source-line="24" id="ent0019"><ellipse cx="774.9111" cy="436.9962" fill="#F1F1F1" rx="63.8311" ry="15.1662" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="90.3438" x="729.7392" y="441.6447">Crea Libreria</text></g><!--entity UNL--><g class="entity" data-qualified-name="UNL" data-source-line="4" id="ent0002"><ellipse cx="73.6997" cy="355.35" fill="#F1F1F1" rx="8" ry="8" style="stroke:#181818;stroke-width:0.5;"/><path d="M73.6997,363.35 L73.6997,390.35 M60.6997,371.35 L86.6997,371.35 M73.6997,390.35 L60.6997,405.35 M73.6997,390.35 L86.6997,405.35" fill="none" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="135.3994" x="6" y="419.8451">Utente non loggato</text></g><!--entity UL--><g class="entity" data-qualified-name="UL" data-source-line="5" id="ent0003"><ellipse cx="254.7188" cy="427.35" fill="#F1F1F1" rx="8" ry="8" style="stroke:#181818;stroke-width:0.5;"/><path d="M254.7188,435.35 L254.7188,462.35 M241.7188,443.35 L267.7188,443.35 M254.7188,462.35 L241.7188,477.35 M254.7188,462.35 L267.7188,477.35" fill="none" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="104.6377" x="202.4" y="491.8451">Utente loggato</text></g><!--entity DB--><g class="entity" data-qualified-name="DB" data-source-line="6" id="ent0004"><ellipse cx="1278.1585" cy="410.4969" fill="#F1F1F1" rx="8" ry="8" style="stroke:#181818;stroke-width:0.5;"/><path d="M1278.1585,418.4969 L1278.1585,445.4969 M1265.1585,426.4969 L1291.1585,426.4969 M1278.1585,445.4969 L1265.1585,460.4969 M1278.1585,445.4969 L1291.1585,460.4969" fill="none" style="stroke:#181818;stroke-width:0.5;"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="66.8008" x="1244.7581" y="474.992">Database</text><text fill="#000000" font-family="sans-serif" font-size="14" font-style="italic" lengthAdjust="spacing" textLength="69.3369" x="1243.49" y="398.6951">«System»</text></g><!--reverse link UNL to UL--><g class="link" data-entity-1="ent0002" data-entity-2="ent0003" data-link-type="extension" data-source-line="8" id="lnk5"><path d="M158.5669,418.734 C178.5069,426.764 183.33,428.69 202.12,436.25" fill="none" id="UNL-backto-UL" style="stroke:#181818;stroke-width:1;"/><polygon fill="none" points="141.87,412.01,156.3256,424.2996,160.8083,413.1683,141.87,412.01" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to LI--><g class="link" data-entity-1="ent0002" data-entity-2="ent0007" data-link-type="association" data-source-line="27" id="lnk20"><path d="M93.01,346.73 C114.82,303.87 154.41,234.01 202.4,185 C256.12,130.14 276.12,120.74 346.04,89 C375.8,75.49 412.43,66.7 437.08,61.81" fill="none" id="UNL-LI" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to REG--><g class="link" data-entity-1="ent0002" data-entity-2="ent0008" data-link-type="association" data-source-line="28" id="lnk21"><path d="M105.69,346.5 C130.21,317.34 166.23,277.27 202.4,247 C260.53,198.34 276.82,185.93 346.04,155 C367.93,145.22 393.41,137.68 415.14,132.29" fill="none" id="UNL-REG" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to VH--><g class="link" data-entity-1="ent0002" data-entity-2="ent0009" data-link-type="association" data-source-line="29" id="lnk22"><path d="M130.74,346.38 C184.14,310.96 267.78,258.98 346.04,225 C367.34,215.75 391.82,208.08 412.99,202.29" fill="none" id="UNL-VH" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to RL--><g class="link" data-entity-1="ent0002" data-entity-2="ent0010" data-link-type="association" data-source-line="30" id="lnk23"><path d="M106.7,423.55 C130.92,450.99 166.1,487.4 202.4,513 C335.19,606.68 404.89,623.77 561.67,581 C608.95,568.1 629.13,565.55 656.67,525 C705.19,453.55 631.88,399.77 686.67,333 C695.98,321.65 709.7,314.33 723.51,309.6" fill="none" id="UNL-RL" style="stroke:#181818;stroke-width:1;"/></g><!--link UNL to VD--><g class="link" data-entity-1="ent0002" data-entity-2="ent0013" data-link-type="association" data-source-line="31" id="lnk24"><path d="M79.29,423.38 C91.4,504.91 132.13,688 253.72,688 C253.72,688 253.72,688 775.91,688 C896.82,688 1014.61,585.28 1061.06,539.53" fill="none" id="UNL-VD" style="stroke:#181818;stroke-width:1;"/></g><!--link RT to RL--><g class="link" data-entity-1="ent0011" data-entity-2="ent0010" data-link-type="dependency" data-source-line="34" id="lnk25"><path d="M536.91,324.93 C591.43,319.42 660.1503,312.4821 711.0103,307.3521" fill="none" id="RT-to-RL" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="716.98,306.75,707.624,303.6734,712.0052,307.2518,708.4269,311.633,716.98,306.75" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="310.0669">«extend»</text></g><!--link RA to RL--><g class="link" data-entity-1="ent0012" data-entity-2="ent0010" data-link-type="dependency" data-source-line="35" id="lnk26"><path d="M536.53,270.08 C591.91,277.29 662.3503,286.4644 713.3603,293.1144" fill="none" id="RA-to-RL" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="719.31,293.89,710.9026,288.7601,714.352,293.2436,709.8684,296.693,719.31,293.89" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="274.0669">«extend»</text></g><!--link VR to VD--><g class="link" data-entity-1="ent0014" data-entity-2="ent0013" data-link-type="dependency" data-source-line="38" id="lnk27"><path d="M862.98,531.48 C873.15,531.69 883.38,531.87 893.16,532 C921.15,532.36 928.2,533.55 956.16,532 C969.64,531.25 977.9194,530.6436 991.7994,529.2636" fill="none" id="VR-to-VD" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="997.77,528.67,988.4184,525.58,992.7945,529.1647,989.2099,533.5408,997.77,528.67" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="894.16" y="528.0669">«extend»</text></g><!--link VC to VD--><g class="link" data-entity-1="ent0015" data-entity-2="ent0013" data-link-type="dependency" data-source-line="39" id="lnk28"><path d="M817.54,384.73 C832.1,390.49 848.48,397.25 863.16,404 C928.5,434.05 996.9976,470.8595 1039.9876,494.5995" fill="none" id="VC-to-VD" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="1045.24,497.5,1039.2951,489.6477,1040.863,495.0829,1035.4278,496.6509,1045.24,497.5" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="894.16" y="415.0669">«extend»</text></g><!--link UL to AP--><g class="link" data-entity-1="ent0003" data-entity-2="ent0016" data-link-type="association" data-source-line="42" id="lnk29"><path d="M307.13,484.91 C319.69,491.24 333.2,497.66 346.04,503 C365.28,511 386.8,518.49 406.18,524.72" fill="none" id="UL-AP" style="stroke:#181818;stroke-width:1;"/></g><!--link UL to CL--><g class="link" data-entity-1="ent0003" data-entity-2="ent0017" data-link-type="association" data-source-line="43" id="lnk30"><path d="M307.41,443.16 C341.77,433.96 386.45,421.99 419.43,413.16" fill="none" id="UL-CL" style="stroke:#181818;stroke-width:1;"/></g><!--link UL to REC--><g class="link" data-entity-1="ent0003" data-entity-2="ent0018" data-link-type="association" data-source-line="44" id="lnk31"><path d="M307.41,459.97 C333.78,461.48 366.24,463.34 394.82,464.98" fill="none" id="UL-REC" style="stroke:#181818;stroke-width:1;"/></g><!--link CL to VC--><g class="link" data-entity-1="ent0017" data-entity-2="ent0015" data-link-type="dependency" data-source-line="47" id="lnk32"><path d="M529.36,394.49 C579.42,389.27 643.7223,382.5721 696.1023,377.1121" fill="none" id="CL-to-VC" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="702.07,376.49,692.7038,373.4446,697.0969,377.0084,693.5332,381.4015,702.07,376.49" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="63.0068" x="592.67" y="379.0669">«include»</text></g><!--link REC to VR--><g class="link" data-entity-1="ent0018" data-entity-2="ent0014" data-link-type="dependency" data-source-line="48" id="lnk33"><path d="M509.2,482.93 C533.18,490.38 563.85,499.19 591.67,505 C624.62,511.88 655.413,516.4528 687.223,520.2828" fill="none" id="REC-to-VR" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="693.18,521,684.7227,515.9528,688.2159,520.4023,683.7664,523.8955,693.18,521" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="63.0068" x="592.67" y="501.0669">«include»</text></g><!--link CL to CRB--><g class="link" data-entity-1="ent0017" data-entity-2="ent0019" data-link-type="dependency" data-source-line="49" id="lnk34"><path d="M527.88,408.16 C582.89,414.6 657.1908,423.3113 710.9208,429.6113" fill="none" id="CL-to-CRB" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="716.88,430.31,708.4071,425.2891,711.914,429.7277,707.4754,433.2347,716.88,430.31" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="412.0669">«extend»</text></g><!--link REC to CRB--><g class="link" data-entity-1="ent0018" data-entity-2="ent0019" data-link-type="dependency" data-source-line="50" id="lnk35"><path d="M530.5,476.94 C567.53,480.09 614.98,481.59 656.67,475 C686.47,470.29 713.4139,461.4042 736.6339,452.4242" fill="none" id="REC-to-CRB" style="stroke:#181818;stroke-width:1;stroke-dasharray:7,7;"/><polygon fill="#181818" points="742.23,450.26,732.3931,449.7756,737.5666,452.0635,735.2787,457.237,742.23,450.26" style="stroke:#181818;stroke-width:1;"/><text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacing" textLength="61.1851" x="593.67" y="471.0669">«extend»</text></g><!--link RL to DB--><g class="link" data-entity-1="ent0010" data-entity-2="ent0004" data-link-type="association" data-source-line="53" id="lnk36"><path d="M823.95,310.52 C904.96,327.04 1073.49,363.46 1212.49,407 C1222.25,410.06 1232.66,413.84 1242.21,417.53" fill="none" id="RL-DB" style="stroke:#181818;stroke-width:1;"/></g><!--link VD to DB--><g class="link" data-entity-1="ent0013" data-entity-2="ent0004" data-link-type="association" data-source-line="54" id="lnk37"><path d="M1127.69,498.49 C1162.34,483.07 1210.04,461.85 1242.16,447.57" fill="none" id="VD-DB" style="stroke:#181818;stroke-width:1;"/></g><!--link CRB to DB--><g class="link" data-entity-1="ent0019" data-entity-2="ent0004" data-link-type="association" data-source-line="55" id="lnk38"><path d="M839.14,436.37 C945.86,435.3 1158.29,433.19 1242.04,432.35" fill="none" id="CRB-DB" style="stroke:#181818;stroke-width:1;"/></g><?plantuml-src RLF1Rjim3BtxAuXSsXxQ3nY208uTj0MChN0Itrc5SuKgKY0fm9fj_pvAT0BFoKtcUtma7sb3FoD41rxt7n5ssfEAsjaYV6fxG8zxEA3wn9xMySiG91XjRwWeK4NdORQDP2E1THQCwtkCRWOOOFkzcJ3-GozWZH7VCL0ErqkOpzTV8T9-iIWAre3vdyV7H2ykbM6srec1y4mXzKhGw7UlP_2x0BXlNA_jGr9hNjDtIptJFPuICyVRrNC2EXsEQFJfXF3YzW9rBpb5A_8AHLPIcd7yG1usEZhZ1DnCmVB8imt5oacjccB4tkYiBW9TFU6qfCW6RbYgT-q4hnoZh25enMWYb4fH20vAJmXlv8EpQAIByYqHpcaugbt9P58mg75RxQhAMKakHSbhb9nsMJ1ontsQxAk6wojWGjeVRo_lONYBzyD7TliCDADuBV8hNhFO08Ex76utyFIqO3vyusEXNqpPyR4mKDu49esUxUTmr-Q4hfugTTLDuFR8et5pyzbgUMPljyFSmvRkuDKgSvIfE5Uh3-9mrKZ9AfVKYR8Z6MirTDDUGKCIh-FAYo7f1fHxu4kmV2lYoF07mlsFGvgCRmpuvrBEU_QA7f9Sdg5U9c_gopE9o_llFm00?></g></svg>

---

## 8. Autori

- Fasolo Alex  
- Como Nicholas Maria

---

## 9. Riferimenti

- Repository: `spettacolo/BooksRecommender`
- Moduli: `server`, `common`, `client`
- Protocollo: richieste testuali via socket