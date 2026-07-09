# Documentație Tehnică & Use Cases: Mențiuni Succesiuni și Urmărire Notificări Notari (SPN/BIN)

Acest document descrie arhitectura, schema bazei de date, fluxurile de date și cazurile de utilizare (**Use Cases**) operaționale, axate pe utilizator, pentru modulul **„Mențiuni succesiuni și urmărirea notificărilor către notari publici (SPN/BIN)”**, integrat în sistemul multi-tenant Registru Agricol.

---

## 1. Arhitectură & Tehnologii Utilizate

Sistemul utilizează o arhitectură asincronă, decuplată și securizată prin mecanisme de criptare avansate:
* **Backend**: Spring Boot 3.3.0, Java 21, JPA / Hibernate, Spring Security.
* **Securitate Date (Blind Index)**: Protejarea CNP-urilor persoanelor fizice se face prin hashing SHA-256 (`CryptoUtils.hashSha256(cnp)`), permițând căutarea securizată și exactă a notificărilor fără decriptarea datelor sensibile în query logs.
* **Auditabilitate (Hibernate Envers)**: Istoricul modificărilor stării de deces este salvat automat în tabelele de audit (Aud).
* **Asincronism (Apache Kafka)**: Evenimentele de deces și înregistrările de succesiuni sunt distribuite asincron prin topicul `notificari-succesiuni`.
* **Notificări Live (WebSockets)**: Consumatorul Kafka preia evenimentele și le transmite în timp real prin WebSocket (`SimpMessagingTemplate`) către frontend pe canalul `/topic/notificari/general`.
* **Frontend**: Angular 18 (Componente independente / Standalone), interfață premium cu glassmorphism, formulare reactive și tabele dinamice adaptive.

---

## 2. Schema Bazei de Date & Entități

### 2.1 Modificări în tabelele existente (Flyway Migration V39)
* **`persoane_fizice`** & **`persoane_fizice_aud` (Audit)**:
  * `este_decedat` (boolean, implicit `false`) - Indică dacă persoana fizică este decedată.
  * `data_decesului` (date, nullable) - Data decesului.
  * `numar_certificat_deces` (varchar, nullable) - Numărul certificatului de deces.

### 2.2 Tabela nouă: `notificari_succesiuni`
Această tabelă reține istoricul adreselor trimise către notari publici pentru fiecare defunct:

| Coloană | Tip Date | Constrângeri | Descriere |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Identificatorul unic al notificării |
| `defunct_id` | `BIGINT` | `FOREIGN KEY` către `persoane_fizice` | Legătura directă cu persoana decedată |
| `defunct_cnp_hash` | `VARCHAR(64)` | `NOT NULL` | Hash-ul CNP-ului defunctului (Blind Index pentru căutare rapidă) |
| `nume_notar_spn_bin`| `VARCHAR(255)`| `NULL` | Numele notarului public sau al camerei SPN/BIN |
| `numar_adresa_oficiala`| `VARCHAR(100)`| `NOT NULL` | Numărul adresei oficiale trimise |
| `data_trimitere` | `DATE` | `NOT NULL` | Data trimiterii fizice a notificării |
| `stadiu_notificare`| `VARCHAR(50)` | `NOT NULL` | Stadiul curent: `TRIMIS`, `IN_LUCRU`, `FINALIZAT` |
| `observatii` | `TEXT` | `NULL` | Observații suplimentare sau detalii din dosar |
| `utilizator_operare`| `VARCHAR(100)`| `NOT NULL` | Numele de utilizator al operatorului care a înregistrat datele |
| `data_inregistrare`| `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Data și ora exactă la care s-a făcut salvarea în sistem |
| `tenant_id` | `VARCHAR(50)` | `NOT NULL` | Identificatorul tenantului (pentru izolare multi-tenant) |

---

## 3. Cazuri de Utilizare Axate pe Utilizator (Use Cases)

### 📌 USE CASE 1: Înregistrarea decesului și trimiterea notificării de succesiune către notar (SPN/BIN)
* **Actor Principal**: Operator Primărie (utilizator înregistrat în cadrul unui Tenant specific).
* **Scop**: Înregistrarea oficială a decesului unui cetățean în sistem, actualizarea automată a statutului civil al acestuia în Registrul Agricol și transmiterea adresei către biroul notarial.
* **Precondiții**: Cetățeanul trebuie să fie înregistrat ca persoană fizică în sistem pe tenantul curent.
* **Pași Efectuați de Utilizator (Operator)**:
  1. Operatorul accesează secțiunea **Succesiuni** din meniul lateral.
  2. În panoul din stânga (**Marcare ca Decedat și Inițiere Notificare**), operatorul caută persoana decedată tastându-i numele sau CNP-ul în câmpul de căutare.
  3. Selectează persoana corectă din lista de sugestii asigurate de sistem.
  4. Completează datele de deces: **Data Decesului** (folosind datepicker) și **Număr Certificat Deces** (text).
  5. Completează datele pentru biroul notarial: **Nume Notar (SPN/BIN)**, **Număr Adresă Oficială**, **Data Trimitere** și eventuale **Observații**.
  6. Apasat butonul **„Înregistrează și Trimite Notificare”**.
* **Reacția / Fluxul Automat al Sistemului**:
  * Sistemul actualizează instantaneu fișa persoanei fizice selectate: marchează `esteDecedat = true` și salvează data și numărul certificatului de deces.
  * Creează un nou dosar de succesiune în tabela `notificari_succesiuni`, completat cu stadiul implicit `TRIMIS`, data înregistrării și numele operatorului autentificat.
  * Publică automat un mesaj asincron pe topicul de **Kafka** (`notificari-succesiuni`).
  * Consumatorul Kafka preia mesajul și îl distribuie în timp real prin **WebSockets** către ceilalți operatori pentru a asigura sincronizarea instantă a datelor.
  * Golește formularul și afișează un mesaj de confirmare verde de tip Toast/Banner pe ecranul operatorului.

---

### 📌 USE CASE 2: Monitorizarea centralizată a dosarelor de succesiune transmise
* **Actor Principal**: Operator Primărie / Administrator (Super Admin)
* **Scop**: Urmărirea stadiului tuturor adreselor transmise către notarii publici în cadrul comunei/orașului (ex: Cluj).
* **Precondiții**: Utilizatorul are acces la modulul de Succesiuni.
* **Pași Efectuați de Utilizator (Operator)**:
  1. Operatorul accesează pagina **Succesiuni** din meniul principal.
  2. Consultă lista de monitorizare afișată în panoul din dreapta (**Monitorizare și Urmărire Notificări Notari**).
* **Reacția / Fluxul Automat al Sistemului**:
  * Sistemul încarcă asincron și randează un tabel premium cu toate înregistrările aferente tenantului activ.
  * Pentru fiecare înregistrare, sistemul afișează dinamic:
    * Numele complet al defunctului,
    * Numele notarului/camerei SPN/BIN,
    * Detaliile oficiale ale adresei (Număr, Data trimiterii),
    * Stadiul curent evidențiat vizual prin culori specifice: Galben (`TRIMIS`), Albastru (`IN_LUCRU`), Verde (`FINALIZAT`),
    * Data adăugării și semnătura digitală a operatorului care a înregistrat documentul.

---

### 📌 USE CASE 3: Căutarea securizată a unui dosar după CNP (Blind Index)
* **Actor Principal**: Operator Primărie / Inspector de Audit GDPR
* **Scop**: Identificarea rapidă și sigură a istoricului succesiunii pentru un anumit cetățean, fără stocarea sau scrierea CNP-ului în clar în logurile de baze de date.
* **Precondiții**: Operatorul cunoaște CNP-ul de 13 cifre al defunctului.
* **Pași Efectuați de Utilizator (Operator)**:
  1. În secțiunea **Succesiuni**, în bara de căutare din panoul din dreapta, operatorul introduce CNP-ul exact de 13 cifre al cetățeanului.
  2. Apasă butonul **„Caută Dosar”** (pictograma lupă).
* **Reacția / Fluxul Automat al Sistemului**:
  * Sistemul trimite o cerere asincronă securizată către backend cu CNP-ul în clar.
  * Backend-ul transformă deterministic CNP-ul într-un hash SHA-256 (Blind Index).
  * Realizează interogarea securizată direct în baza de date: `SELECT * FROM notificari_succesiuni WHERE defunct_cnp_hash = :hash AND tenant_id = :tenant`.
  * Filtrează și returnează în interfață exclusiv dosarul/dosarele asociate acelui defunct. *CNP-ul în clar nu este stocat sau jurnalizat în niciun log secundar de audit.*

---

### 📌 USE CASE 4: Actualizarea stadiului unui dosar direct din tabelul de monitorizare
* **Actor Principal**: Operator Primărie
* **Scop**: Schimbarea stării unei notificări pe măsură ce biroul notarial procesează dosarul (ex: când dosarul este preluat sau când succesiunea este finalizată).
* **Precondiții**: Dosarul de succesiune este listat în tabelul de monitorizare.
* **Pași Efectuați de Utilizator (Operator)**:
  1. În tabelul din pagina **Succesiuni**, operatorul localizează dosarul corespunzător defunctului.
  2. În coloana **Stadiu**, dă click pe badge-ul colorat care funcționează ca un selector (dropdown asincron).
  3. Selectează noul stadiu din listă (ex: schimbă din `TRIMIS` în `IN LUCRU` sau `FINALIZAT`).
* **Reacția / Fluxul Automat al Sistemului**:
  * Sistemul trimite asincron o cerere `PATCH` către server cu noul stadiu selectat.
  * Baza de date actualizează stadiul notificării în timp util.
  * Interfața Angular actualizează vizual culoarea și textul badge-ului în timp real, oferind un feedback vizual instant operatorului, fără reîncărcarea paginii.

---

### 📌 USE CASE 5: Vizualizarea automată a stării de deces în Registrul General de Persoane
* **Actor Principal**: Operator Primărie / Primar / Inspector Agricol
* **Scop**: Identificarea vizuală rapidă a statutului persoanelor fizice din comună direct în registrul general, prevenind operarea greșită de date pe persoane defuncte.
* **Precondiții**: Persoana a fost marcată ca decedată în urma USE CASE 1.
* **Pași Efectuați de Utilizator (Operator)**:
  1. Operatorul accesează meniul principal **Persoane**.
  2. Parcurge sau filtrează lista completă a cetățenilor înregistrați în UAT.
* **Reacția / Fluxul Automat al Sistemului**:
  * În momentul randării tabelului general, sistemul verifică proprietatea `esteDecedat` a fiecărui cetățean.
  * În coloana **„Stare deces”**, sistemul randează automat:
    * Un **badge verde cu textul „În viață”** pentru cetățenii activi.
    * Un **badge roșu aprins cu textul „Decedat”** pentru cetățenii pentru care s-a înregistrat decesul.
  * Operatorul vede direct și securizat starea persoanei, eliminând necesitatea de a verifica manual dosarele de succesiune în alte ferestre.
