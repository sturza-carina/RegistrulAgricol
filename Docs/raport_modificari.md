# Raport Modificări: Inițializare Automată Bază de Date (Dezvoltare)

Acest document prezintă centralizat toate fișierele create, modificate sau actualizate în cadrul proiectului **Registru Agricol** pentru a facilita înțelegerea arhitecturii și a implementării realizate.

---

## 1. Fișiere Create (Adăugate în Proiect)

### A. Enumerări și Entități JPA (`src/backend/src/main/java/com/multitenant/model/`)
* **[TipGospodarie.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/TipGospodarie.java)**:
  Enumerație ce definește formele de organizare ale gospodăriilor.
  * Valori: `INDIVIDUALA`, `COLECTIVA`, `ASOCIATIE`.
* **[Uat.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/Uat.java)**:
  Entitate JPA mapată pe tabela `uat` care stochează unitățile administrativ-teritoriale.
  * Proprietăți: `id` (PK, Serial), `codSiruta` (codul oficial SIRUTA), `denumire` (ex: Cluj-Napoca), `judet`, `tipUat` (ex: MUNICIPIU).
* **[Gospodarie.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/Gospodarie.java)**:
  Entitate JPA mapată pe tabela `gospodarie` reprezentând gospodăriile înregistrate.
  * Proprietăți: `id` (PK, Serial), `codGospodarie`, `adresa`, `tipGospodarie` (Enum), `activa` (boolean). Relație `@ManyToOne` către `Uat` (prin FK `uat_id`).

### B. Repositories (`src/backend/src/main/java/com/multitenant/repository/`)
* **[UatRepository.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/repository/UatRepository.java)**:
  Interfață Spring Data JPA pentru operațiuni CRUD pe tabela `uat`.
* **[GospodarieRepository.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/repository/GospodarieRepository.java)**:
  Interfață Spring Data JPA pentru operațiuni CRUD pe tabela `gospodarie`.

### C. Logică Seeder (`src/backend/src/main/java/com/multitenant/config/`)
* **[DatabaseSeeder.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/config/DatabaseSeeder.java)**:
  Componentă executabilă la startup (`CommandLineRunner`). Dacă baza de date este goală, ea:
  1. Înregistrează automat un chiriaș (`cluj`) în tabela publică.
  2. Creează datele de login în schema publică pentru `cluj_admin` și `cluj_user`.
  3. Comută contextul bazei de date pe schema tenant-ului `cluj`.
  4. Inserează un UAT implicit („Cluj-Napoca”).
  5. Inserează utilizatorii locali în schema tenant-ului, asociați cu UAT-ul.
  6. Inserează gospodării de test în schema tenant-ului, asociate cu UAT-ul.
  7. Eliberează contextul înapoi în `public`.

---

## 2. Fișiere Modificate

### A. Scripturi de Migrare SQL (`src/backend/src/main/resources/`)
* **[V1__init_public_schema.sql](file:///d:/RADU/Registru-Agricol/src/backend/src/main/resources/db/migration/V1__init_public_schema.sql)**:
  * Adăugat în structura tabelei `public.users` coloanele: `nume`, `email`, `activ` și `uat_id` (pentru suportul noilor câmpuri de profil).
  * Actualizat inserarea de test a utilizatorului `superadmin` cu noile câmpuri.
* **[V1__init_tenant_schema.sql](file:///d:/RADU/Registru-Agricol/src/backend/src/main/resources/db/tenant/V1__init_tenant_schema.sql)**:
  * Adăugat tabelele `uat` și `gospodarie` conform cerințelor.
  * Modificat tabela `users` din schemă pentru a include noile coloane și cheia externă către `uat`.

### B. Modele și Servicii (`src/backend/src/main/java/com/multitenant/`)
* **[User.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/User.java)**:
  * Adăugat atributele în Java: `nume`, `email`, `activ` și `@ManyToOne private Uat uat`.
* **[TenantService.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/service/TenantService.java)**:
  * **Corectat bug de routing**: ID-ul generat anterior ca UUID (`UUID.randomUUID().toString()`) împiedica rezolvarea schemelor în conexiunea JDBC (deoarece conexiunea rula `SET SCHEMA '<tenant_id>'`). Am schimbat ID-ul salvat să fie exact egal cu numele schemei (ex: `cluj`).

---

## 3. Fluxul de Funcționare (Cum merg datele?)

```
[Utilizator/Parolă] -> Autentificare în schema 'public'
                           |
                           v
              Identificare 'tenant_id' (ex: cluj)
                           |
                           v
              Generare JWT Token pentru Frontend
                           |
                           v
   Toate cererile viitoare trimit header-ul 'X-Tenant-ID: cluj'
                           |
                           v
  Conexiunile de Backend comută automat schema: SET SCHEMA 'cluj'
                           |
                           v
  Interogările SQL accesează tabelele: cluj.uat, cluj.gospodarie, cluj.users
```
