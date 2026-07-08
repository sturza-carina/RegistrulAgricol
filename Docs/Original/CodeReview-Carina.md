# Code Review — Registrul Agricol
> Analiză completă a codului Spring Boot + Angular.

---

## 🔴 Probleme Critice

### 1. Izolarea între tenanți poate fi ocolită prin header-ul `X-Tenant-ID`
**Fișiere:** `TenantFilter.java:25`, `JwtAuthenticationFilter.java:33`, `jwt.interceptor.ts:16`

**Problema:** Backend-ul are încredere în header-ul `X-Tenant-ID` trimis de client. Tenant-ul din JWT este citit, dar nu este verificat față de header. Orice utilizator autentificat poate trimite un alt tenant ID și poate accesa schema altui tenant.

**Fix:** Derivă tenant-ul din JWT pe server, nu din header. Permite schimbarea tenant-ului doar pentru super admin, printr-un endpoint auditat.

```java
String tokenTenant = jwtUtils.getTenantIdFromJwtToken(jwt);
String requestedTenant = request.getHeader("X-Tenant-ID");

if (!"ROLE_SUPER_ADMIN".equals(role) &&
    requestedTenant != null &&
    !requestedTenant.equals(tokenTenant)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid tenant");
    return;
}

TenantContext.setCurrentTenant(
    "ROLE_SUPER_ADMIN".equals(role) && requestedTenant != null
        ? requestedTenant
        : tokenTenant
);
```

---

### 2. Numele schemei este concatenat direct în SQL — SQL Injection
**Fișiere:** `MultiTenantConnectionProviderImpl.java:36`, `MultiTenantConnectionProviderImpl.java:39`

**Problema:** Codul face `SET search_path TO uat_` + tenant ID nechecked. Un tenant ID malițios poate injecta SQL în connection state.

**Fix:** Validează tenant ID-ul cu un regex strict și folosește quote pentru identificatori.

```java
if (!tenantIdentifier.matches("[0-9A-Za-z_-]{1,32}")) {
    throw new SQLException("Invalid tenant identifier");
}
String schemaName = "uat_" + tenantIdentifier;
try (Statement st = connection.createStatement()) {
    st.execute("SET search_path TO " + PgConnection.escapeIdentifier(schemaName) + ", public");
}
```

---

## 🟠 Probleme High

### 3. `ROLE_USER` poate crea, modifica și șterge date din registru
**Fișiere:** `GospodarieController.java:29`, `ParcelaController.java:38`, `AnimalController.java:55`, `PersoanaController.java:57`

**Problema:** Un utilizator simplu are acces complet la operații distructive (DELETE, PUT) pe datele registrului agricol.

**Fix:** Separă autoritățile: `REGISTRU_READ`, `REGISTRU_WRITE`, `REGISTRU_DELETE` și aplică `@PreAuthorize` la nivel de metodă.

---

### 4. `CladireController` și `CulturaParcelaController` nu au autorizare pe metode
**Fișiere:** `CladireController.java:13`, `CulturaParcelaController.java:14`

**Problema:** Deși securitatea globală cere autentificare, aceste controller-e nu au nicio politică de rol și nicio verificare a contextului de tenant.

**Fix:** Adaugă `@PreAuthorize` pe fiecare endpoint și respinge requesturile cu tenant context `public`.

---

### 5. Adminii de tenant pot crea utilizatori cu roluri superioare
**Fișiere:** `UserController.java:41`, `UserController.java:65`

**Problema:** Un admin de tenant poate trimite `role: ROLE_SUPER_ADMIN` la creare user. Backend-ul forțează doar `tenantId`, nu și rolul maxim permis.

**Fix:** Whitelistează rolurile care pot fi atribuite în funcție de rolul caller-ului.

```java
if (!isSuperAdmin(currentUser) && "ROLE_SUPER_ADMIN".equals(user.getRole())) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot assign super admin role");
}
```

---

### 6. API-ul de utilizatori expune hash-uri de parole
**Fișiere:** `UserController.java:74`, `User.java:19`

**Problema:** Controller-ele returnează entitatea `User` direct. Hash-ul parolei este serializat în JSON și trimis clientului.

**Fix:** Folosește un `UserResponseDTO` fără câmpul `password`, sau adaugă cel puțin `@JsonIgnore` pe câmpul `password` din entitate.

---

### 7. Crearea unui tenant poate lăsa tenanți orfani
**Fișiere:** `TenantService.java:41`, `TenantService.java:53`

**Problema:** Tenant-ul este salvat în baza de date *înainte* ca migrarea Flyway să ruleze. Dacă Flyway eșuează, metadata publică spune că tenant-ul există, dar schema poate fi incompletă sau inexistentă.

**Fix:** Rulează Flyway *înainte* de a salva tenant-ul în DB, sau compensează la eroare prin ștergerea schemei și a înregistrării din `public`.

---

### 8. Configurația Flyway dezactivează garanțiile de producție
**Fișiere:** `application.yml:29`, `application.yml:30`, `TenantService.java:51`

**Problema:** `validate-on-migrate: false`, `out-of-order: true`, și `repair()` automat pot ascunde diferențe între schema reală și migrările așteptate.

**Fix:** Activează validarea în producție, elimină `repair()` automat și lasă-l doar ca operație manuală de admin.

---

## 🟡 Probleme Medium

### 9. Migrările de tenant creează tabele `uat` și `users` neutilizate per tenant
**Fișiere:** `V1__init_tenant_schema.sql:1`, `V1__init_tenant_schema.sql:11`, `User.java:8`, `Uat.java:13`

**Problema:** Entitățile Java mapează `users` și `uat` pe schema `public`, dar migrările de tenant creează versiuni locale în fiecare schemă de tenant. Cu `search_path` activ, asta poate crea confuzie și bug-uri greu de depistat.

**Fix:** Șterge tabelele `users`/`uat` locale din migrările de tenant. Schema de tenant trebuie să conțină doar date de registru.

---

### 10. Lipsesc Bean Validation și DTO-uri la input
**Fișiere:** `TerenController.java:54`, `PersoanaController.java:23`, `ParcelaController.java:39`

**Problema:** Controller-ele acceptă entități sau DTO-uri fără `@Valid`. Inputuri invalide ajung direct la constrângerile bazei de date sau la excepții runtime necatched.

**Fix:** Adaugă `spring-boot-starter-validation`, folosește request DTO-uri dedicate, și adaugă `@Valid` pe parametrii de request.

---

### 11. Relații eager și serializare directă de entități — risc N+1
**Fișiere:** `AnimalIndividual.java:24`, `AnimalIndividual.java:34`, `Gospodarie.java:31`, `Machinery.java:32`

**Problema:** Endpoint-urile `findAll()` returnează entități cu relații `EAGER`. Asta poate genera zeci de query-uri pentru o singură cerere și JSON instabil (referințe circulare etc.).

**Fix:** Setează relațiile pe `LAZY` by default, returnează DTO-uri în loc de entități, și folosește `@EntityGraph` sau fetch join-uri doar unde e nevoie.

---

### 12. `@Data` de la Lombok pe entități JPA este periculos
**Fișiere:** `Parcela.java:9`, `Persoana.java:23`, `AnimalIndividual.java:17`

**Problema:** `@Data` generează `equals()`, `hashCode()` și `toString()` pe baza tuturor câmpurilor, inclusiv relații lazy. Asta poate declanșa `LazyInitializationException` sau bucle infinite la serializare.

**Fix:** Înlocuiește `@Data` cu `@Getter`/`@Setter` și implementează `equals`/`hashCode` doar pe baza `id`-ului, după ce entitatea e persistată.

---

### 13. Lipsesc indexuri pe foreign key-uri și filtrele comune
**Fișiere:** `V15__create_eveniment_animal_table.sql:14`

**Problema:** Doar timeline-ul de animale are un index. Query-urile comune filtrează după `gospodarie_id`, `teren_id`, `parcela_id`, `proprietar_id`, `cod_gospodarie` și coloane de căutare pe persoane — fără indexuri.

**Fix:** Adaugă indexuri în migrările Flyway pentru toate path-urile de FK folosite de repository-uri.

---

### 14. Frontend-ul nu are route guards
**Fișiere:** `app.routes.ts:24`

**Problema:** Paginile protejate sunt accesibile prin URL direct fără nicio verificare. Securitatea depinde de erorile API sau de redirecturi din componente, nu de guards.

**Fix:** Adaugă `authGuard` și role guards pe rutele protejate.

---

### 15. Subscripțiile din `generic-form` nu sunt dezabonate — memory leak
**Fișiere:** `generic-form.component.ts:67`

**Problema:** La fiecare apel `initForm()` se adaugă o nouă subscripție la `valueChanges`. Dacă formularul este reconstruit, subscripțiile vechi nu sunt anulate și se acumulează.

**Fix:** Folosește `DestroyRef`/`takeUntilDestroyed()` sau salvează și dezabonează subscripția anterioară înainte de a crea una nouă.

---

### 16. `ContractUtilizare` este legat de `Teren` în loc de `Parcela`
**Fișiere:** `ContractUtilizare.java:17`, `V19__create_contract_utilizare_table.sql:3`

**Problema:** Contractul se referă la unitatea cadastrală concretă, dar în arhitectura actuală `Teren` este un container legat de `Gospodarie`, iar `Parcela` este unitatea cu suprafață/geometrie. Dacă un `Teren` are mai multe parcele, contractul nu poate indica exact ce parcelă este arendată/concesionată.

**Fix:** Mută relația pe `Parcela`, sau dacă un contract poate acoperi mai multe parcele, creează tabel de legătură `contract_utilizare_parcele`.

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "parcela_id", nullable = false)
private Parcela parcela;
```

---

### 17. Lipsește regula de exclusivitate pentru contractele active
**Fișiere:** `ContractUtilizareService.java:44`

**Problema:** O parcelă nu trebuie să aibă contracte active suprapuse conform modelului de domeniu, dar codul permite oricâte contracte active pentru același `terenId`, inclusiv pe aceleași date.

**Fix:** Adaugă validare la create/update.

```java
boolean overlaps = repository.existsActiveOverlap(
    dto.getParcelaId(),
    dto.getDataInceput(),
    dto.getDataSfarsit(),
    existingId
);
if (overlaps) {
    throw new ResponseStatusException(HttpStatus.CONFLICT,
        "Există deja un contract activ suprapus pentru această parcelă.");
}
```

---

### 18. `utilizatorOperare` este controlat de client
**Fișiere:** `ContractUtilizareDTO.java:13`, `ContractUtilizareService.java:127`, `contract-management.component.ts:304`

**Problema:** Frontend trimite `utilizatorOperareId: null`, iar backend acceptă valoarea din DTO. Auditul nu este de încredere și nu reflectă userul autentificat.

**Fix:** Scoate `utilizatorOperareId` din request și setează server-side din `SecurityContextHolder`.

---

### 19. `ContractUtilizare` folosește `EAGER` și este returnată direct în API
**Fișiere:** `ContractUtilizare.java:17`, `ContractUtilizare.java:21`, `ContractUtilizareController.java:30`

**Problema:** `Teren`, `Persoana`, `locatorProprietar`, `locatorUtilizator`, `utilizatorOperare` sunt încărcate eager și serializează obiecte JPA direct. Risc de N+1, payload mare și instabilitate JSON.

**Fix:** `LAZY` pe relații + `ContractUtilizareResponseDTO`.

---

### 20. `SursaApa.tipSursa` este string liber fără enum sau validare backend
**Fișiere:** `SursaApa.java:16`, `SursaApaDTO.java:11`, `teren-parcele.component.ts:84`

**Problema:** Frontend are o listă hardcodată de tipuri, dar backend acceptă orice string. Se pot salva valori invalide sau diferite față de ce afișează UI-ul. Corelat cu problema #16 (valorile hardcodate pot derapa).

**Fix:** Creează `TipSursaApa` enum și folosește `@Enumerated(EnumType.STRING)`. De coordonat cu decizia de grup privind schema `public` pentru tipuri de referință.

```java
public enum TipSursaApa {
    PUT_FORAT, FANTANA, RETEA_IRIGATII, RAU_CANAL, ACUMULARE
}
```

---

### 21. `tip_sursa` este nullable în DB și în entitate
**Fișiere:** `V20__create_surse_apa_table.sql:4`, `SursaApa.java:16`

**Problema:** O sursă de apă fără tip nu are valoare funcțională în registru. UI validează doar superficial.

**Fix:**

```sql
ALTER TABLE surse_apa
  ALTER COLUMN tip_sursa SET NOT NULL;
```

```java
@Column(name = "tip_sursa", nullable = false, length = 50)
```

---

### 22. Modelul Angular pentru contracte este definit în service, nu în `models/`
**Fișiere:** `contract-utilizare.service.ts:7`

**Problema:** În restul aplicației există folder `models/`. `ContractUtilizare` și `ContractUtilizareRequest` sunt definite direct în service, rupând consistența.

**Fix:** Mută interfețele în `models/contract-utilizare.model.ts`.

---

### 23. Valorile hardcodate pe frontend diferă de backend
**Fișiere:** `animal-list.component.ts:72`, `SpecieAnimal.java:3`

**Problema:** Frontend-ul folosește `ALBINE`, backend-ul are enum `APICOLE`. Problema similară există și pentru roluri, tipuri de persoane, tipuri UAT și tipuri de teren — valorile pot derapa în timp.

**Fix:** Expune endpoint-uri de lookup din backend sau generează modele TypeScript din OpenAPI.

---

## 🔵 Probleme Low

### 17. Configurația de producție expune comportament de debug
**Fișiere:** `application.yml:4`, `application.yml:22`, `application.yml:33`

**Problema:** Mesaje de eroare detaliate, logging SQL, și valori default pentru secretul JWT sunt nesigure în producție.

**Fix:** Mută secretele în variabile de mediu, dezactivează SQL logging și erorile detaliate în afara mediului de dev.

---

### 18. Frontend-ul are console.log-uri de debug și `alert()`-uri
**Fișiere:** `uat-context.service.ts:37`, `parcela-map.component.ts:295`, `teren-form.component.ts:269`

**Problema:** Log-urile includ stack trace-uri și detalii de stare internă. `alert()`-urile blochează UI-ul și oferă o experiență proastă utilizatorului.

**Fix:** Implementează un serviciu de toast/notificări și elimină log-urile de debug din build-urile de producție.

---

## 📦 Avertismente Build Angular

- **Bundle size depășit:** Bundle-ul inițial depășește bugetul de 512 KB cu **361.25 KB** — necesită lazy loading pe rute
- **Leaflet este CommonJS:** Cauzează un bailout la optimizare în Angular build — importă doar ce ai nevoie sau folosește un wrapper ESM

---

## Rezumat

| Severitate | Nr. probleme |
|------------|:------------:|
| 🔴 Critice | 2 |
| 🟠 High | 6 |
| 🟡 Medium | 13 |
| 🔵 Low | 2 |
| **Total** | **23** |

**Prioritățile imediate** (înainte de orice demo sau deploy): problema #1 (tenant bypass) și #2 (SQL injection) sunt vulnerabilități de securitate reale care trebuie rezolvate primele. Problema #6 (parole expuse în API) e a treia prioritate + Problemele #17 (suprapunere contracte active) și #18 (audit utilizator controlat de client).
