

Requisiti
- Java 17 o superiore (OpenJDK)
- JavaFX 17 SDK (necessario per il client)
- Sistema operativo: macOS (Intel o Apple Silicon) o Linux

Per Mac Apple Silicon scaricare la versione AARCH64 di JavaFX.
Per Windows


-------------------------------------------------------------------------------------------


Struttura del progetto:
BooksRecommender/
├── autori.txt
├── bin/
│   ├── client.jar
│   ├── logs/
│   ├── server.jar
│   ├── start_client_UNIX.sh
│   ├── start_server_UNIX.sh
│   └── start_server_WIN.bat
├── doc/
├── lib/
├── pom.xml
├── README.txt
└── src/


-------------------------------------------------------------------------------------------


AVVIARE L'APPLICAZIONE PER MACOS
Comandi da inserire nel terminale:

1. Naviga la cartella bin: 
	cd /percorso/del/progetto/BooksRecommender/bin

2. Rendi lo script eseguibile: 
	chmod +x start_server_UNIX.sh

3. Avvia del server: 
	./start_server_UNIX.sh

4. Segui i comandi del menu interattivo:
	=== Menu ===
	1. Avvia Server
	2. Ferma Server
	3. Esci
5. Clicca 1 e inviare per avviare il server sulla porta 1234

6. Naviga nella cartella bin:
	cd /percorso/del/progetto/BooksRecommender/bin

7. Rendi eseguibile lo script:
	chmod +x start_client_UNIX.sh

8. Avvia il client:
	./start_client_UNIX.sh


-------------------------------------------------------------------------------------------


AVVIARE L'APPLICAZIONE PER WINDOWS
Comandi

1. Avvia il server cliccando due volta lo start_server_WIN.bat in BooksRecommender/bin

2. Avvia il client cliccado due volte client.jar
