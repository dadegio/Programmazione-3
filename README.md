#  Progetto Mail Client / Mail Server (JavaFX + Socket)

Questo progetto di laboratorio consiste in **due applicazioni distinte**, sviluppate in Java e basate su **JavaFX + pattern MVC**, che comunicano tra loro tramite **socket Java** scambiando **solo dati testuali**.

La repository contiene **sia il Mail Server che il Mail Client** *solo per motivi organizzativi e di consegna*:  
 **per eseguire correttamente il progetto, i due progetti devono essere separati in due cartelle differenti** (vedi sezione “Esecuzione”).

---

##  Componenti del progetto

### 1) Mail Server
Applicazione JavaFX che gestisce:

- una lista di caselle di posta precompilate (es. 3 account per la demo);
- la persistenza dei messaggi tramite file (senza database);
- la consegna delle email ai destinatari;
- un’interfaccia grafica con **log degli eventi** lato server (connessioni, invii, errori di consegna, ecc.).

 Il server è responsabile della **verifica dell’esistenza** degli indirizzi email (autenticazione e destinatari).

---

### 2) Mail Client
Applicazione JavaFX che permette ad un utente di:

- inserire il proprio indirizzo email come unica forma di autenticazione;
- visualizzare la **Inbox** (non sono gestiti cestino/outbox);
- leggere i dettagli di un messaggio;
- cancellare un messaggio dalla inbox;
- comporre e inviare email a uno o più destinatari;
- Reply e Reply-All;
- Forward verso uno o più destinatari;
- visualizzare lo stato di connessione con il server (connesso/non connesso);
- aggiornare automaticamente la inbox (refresh “passivo”) e notificare l’arrivo di nuove email.

 Il client valida la **correttezza sintattica** degli indirizzi email tramite **Regex**.

---

##  Comunicazione Client ↔ Server

- Client e Server girano su **JVM separate**.
- Comunicazione esclusivamente tramite **socket Java** e **testo**.
- **Niente socket permanenti**: il comportamento è simile ad HTTP  
  → il client apre una connessione **solo quando deve fare un’operazione**.

### Scalabilità
Il client **non scarica mai l’intera inbox**: quando richiede aggiornamenti, il server invia **solo i messaggi non ancora distribuiti** al client.

---

##  Parallelismo e concorrenza

Entrambe le applicazioni:

- parallelizzano attività non sequenziali (es. networking / aggiornamenti);
- gestiscono correttamente la mutua esclusione su risorse condivise;
- mantengono GUI reattiva evitando blocchi sul thread JavaFX.

---

##  Architettura (MVC + Observer)

Sia Client che Server sono sviluppati seguendo:

- **Pattern MVC**
- Nessuna comunicazione diretta tra View e Model  
  → tutte le interazioni passano dal Controller o tramite Observer/Observable.

 Non vengono utilizzate le classi deprecate `Observer.java` e `Observable.java`.  
Sono usati invece i meccanismi JavaFX (Properties / ObservableList / Binding).

---

##  Struttura della repository

La repo contiene entrambi i progetti:
- **MailServer**
- **MailClient**


 **IMPORTANTE:** la presenza nella stessa repository è solo organizzativa.  
Per l’esecuzione corretta è necessario separare i due progetti in due cartelle differenti (es. fuori dalla repo o in due workspace diversi).

---

##  Esecuzione

### Prerequisiti
- Java (versione consigliata: 17 o superiore)
- JavaFX configurato correttamente nel proprio IDE

### 1) Separare i progetti
Copiare/estrarre:

- `MailServer/` in una cartella dedicata (es. `MailServerProject/`)
- `MailClient/` in una cartella dedicata (es. `MailClientProject/`)

Aprire i due progetti separatamente nel proprio IDE.

### 2) Avviare il server
Eseguire prima il progetto **MailServer**.

- Il server mostra una GUI con log degli eventi.
- Gli account disponibili sono preconfigurati.

### 3) Avviare uno o più client
Eseguire il progetto **MailClient**.

- Inserire un indirizzo email valido (sintassi corretta).
- Il client contatta il server per verificare l’esistenza dell’account.

 Per la demo è possibile aprire più istanze del client (es. 3 utenti) per simulare lo scambio di email.

---

##  Gestione errori e disconnessioni

- Il client **non va in crash** se il server viene spento.
- In caso di disconnessione:
  - la GUI mostra lo stato “non connesso”;
  - le operazioni fallite vengono segnalate all’utente;
  - il client tenta di riconnettersi automaticamente quando il server torna attivo.

---

##  Note

- Il progetto non implementa:
  - registrazione nuovi utenti dal client;
  - cestino;
  - outbox;
  - database.

- Gli account sul server sono fissi (demo con 3 utenti), ma la struttura è progettata per essere estendibile.

