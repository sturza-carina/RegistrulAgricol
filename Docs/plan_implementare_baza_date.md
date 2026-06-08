# Plan de Implementare: Inițializare Rapidă Bază de Date pentru Dezvoltare

Acest document descrie planul tehnic pentru configurarea automată a bazei de date la pornirea aplicației în mediu de dezvoltare. Scopul este ca, la prima descărcare a proiectului, să existe deja un chiriaș (tenant), un UAT implicit, utilizatori și gospodării de test pentru a putea începe lucrul imediat.

---

## 1. Modificări Structură Bază de Date (SQL / Migrări)

Pentru a nu intra în conflict cu cuvintele rezervate din PostgreSQL, tabela utilizatorilor va rămâne cu numele `users`.

### A. Schema Publică (`db/migration`)
* **Modificare [V1__init_public_schema.sql](file:///d:/RADU/Registru-Agricol/src/backend/src/main/resources/db/migration/V1__init_public_schema.sql)**:
  Se adaugă următoarele coloane în tabela globală `public.users`:
  * `nume` (VARCHAR)
  * `email` (VARCHAR)
  * `activ` (BOOLEAN, implicit `true`)
  * `uat_id` (INTEGER, nullable, pentru cazurile când utilizatorul este legat de un UAT)

### B. Schema specifică Tenant-ului (`db/tenant`)
* **Modificare [V1__init_tenant_schema.sql](file:///d:/RADU/Registru-Agricol/src/backend/src/main/resources/db/tenant/V1__init_tenant_schema.sql)**:
  * Se adaugă aceleași noi coloane (`nume`, `email`, `activ`, `uat_id`) în tabela `users` din schema tenant-ului.
  * Se creează tabela `uat` (Unitate Administrativ-Teritorială):
    ```sql
    CREATE TABLE uat (
        id SERIAL PRIMARY KEY,
        cod_siruta VARCHAR(50) NOT NULL,
        denumire VARCHAR(255) NOT NULL,
        judet VARCHAR(100) NOT NULL,
        tip_uat VARCHAR(50) NOT NULL
    );
    ```
  * Se creează tabela `gospodarie` (Gospodărie):
    ```sql
    CREATE TABLE gospodarie (
        id SERIAL PRIMARY KEY,
        cod_gospodarie VARCHAR(100) NOT NULL,
        adresa VARCHAR(255) NOT NULL,
        tip_gospodarie VARCHAR(50) NOT NULL,
        activa BOOLEAN DEFAULT TRUE,
        uat_id INTEGER REFERENCES uat(id)
    );
    ```

---

## 2. Modificări în Codul Backend (Modele Java & Repositories)

### A. Modele / Entități JPA (`com.multitenant.model`)
1. **[User.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/User.java)**:
   Se adaugă atributele `nume`, `email`, `activ` și relația `@ManyToOne` către entitatea `Uat`.
2. **`TipGospodarie` (Enum)**:
   Valori: `INDIVIDUALA`, `COLECTIVA`, `ASOCIATIE`.
3. **`Uat` (Clasă nouă)**:
   Mapată pe tabela `uat` cu atributele: `id`, `codSiruta`, `denumire`, `judet`, `tipUat`.
4. **`Gospodarie` (Clasă nouă)**:
   Mapată pe tabela `gospodarie` cu atributele: `id`, `codGospodarie`, `adresa`, `tipGospodarie` (enum), `activa` (boolean) și relația `@ManyToOne` către `Uat`.

### B. Repositories (`com.multitenant.repository`)
Se creează două interfețe noi ce extind `JpaRepository`:
* `UatRepository`
* `GospodarieRepository`

---

## 3. Logica de Populare Date la Pornire (Startup Seeding)

Se va implementa clasa **[DatabaseSeeder.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/config/DatabaseSeeder.java)** implementing `CommandLineRunner`. La pornirea aplicației Spring Boot:
1. Verifică dacă există deja chiriași în tabela `public.tenants`.
2. Dacă tabela este goală:
   * Apelează `TenantService.createTenant` pentru a inițializa un tenant implicit numit `cluj` (creează schema `cluj` și execută migrările de tenant în ea).
   * Schimbă contextul multi-tenant pe schema `cluj`.
   * Inserează un UAT implicit: Cluj-Napoca (Siruta: `54975`, Județ: Cluj, Tip: `MUNICIPIU`).
   * Creează un utilizator de test legat de acest UAT în tabela `users` a tenant-ului (ex: username: `cluj_admin`, rol: `ROLE_ADMIN`).
   * Creează 2-3 gospodării de test asociate UAT-ului (ex: cod gospodărie `GOSP-001`, adresă `Str. Observatorului Nr. 2`, tip `INDIVIDUALA`).
   * Resetează contextul multi-tenant înapoi pe schema `public`.
