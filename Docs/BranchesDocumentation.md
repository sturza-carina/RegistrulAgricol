# Documentație Branch-uri (Yours) — Registru Agricol

Documentație tehnică detaliată pentru cele 9 branch-uri proprii vizibile în tab-ul **Yours** din GitHub (`Patrik1339/Registru-Agricol`), generată la 2026-07-08. Pentru fiecare branch: scop, flux pas-cu-pas, schema DB completă, exemple reale de cod și puncte de atenție.

## Sumar

| Branch | Stare | PR | Funcționalitate |
|---|---|---|---|
| `feature/semnatura-signnow` | ✅ Merged în `main` | [#66](https://github.com/Patrik1339/Registru-Agricol/pull/66) | Semnătură electronică contracte (SignNow) |
| `feature/evidenta-pasuni-fanete` | ✅ Merged în `main` | [#68](https://github.com/Patrik1339/Registru-Agricol/pull/68) | Modul evidență pășuni/fânețe |
| `feature/evidenta-vita-de-vie` | ✅ Merged în `main` | [#67](https://github.com/Patrik1339/Registru-Agricol/pull/67) | Modul evidență viță-de-vie |
| `origin/fix/some-bugs` | 🔶 Deschis (3 commits ahead) | — | Hardening securitate, indexuri DB, toast notifications |
| `origin/fix/authorization-integrity` | 🔶 Deschis (2 commits ahead) | #45 (referință internă) | Autorizare pe controllere + integritate date contracte |
| `origin/integrareGoogleMaps` | ✅ Merged în `main` | [#30](https://github.com/Patrik1339/Registru-Agricol/pull/30) | Integrare Google Maps (hărți, autocomplete adresă) |
| `origin/CategorieFolosinta` | ✅ Merged în `main` | [#16](https://github.com/Patrik1339/Registru-Agricol/pull/16) | CRUD Categorie de Folosință |
| `origin/documentation` | ✅ Merged în `main` | [#10](https://github.com/Patrik1339/Registru-Agricol/pull/10) | Refactor module timpurii + walkthrough |
| `uat_crud` | ✅ Merged în `main` (fără PR) | — | Pagină management UAT-uri (CRUD) |

Notă: branch-urile marcate „Merged" au deja tot conținutul integrat în `main` (Ahead = 0 în GitHub), dar rămân listate pentru că nu au fost șterse după merge.

**Legătură importantă între branch-uri:** `uat_crud` (#9) a introdus `UatController` fără nicio verificare de rol pe `POST/PUT/DELETE`; exact acest gol e reparat de `origin/fix/authorization-integrity` (#5), care adaugă `@PreAuthorize` pe cele trei endpointuri. Vezi secțiunile 5 și 9.

---

## 1. `feature/semnatura-signnow` — Semnătură electronică contracte (SignNow)

**Status:** merged (PR #66, commit `eea8895`)

Adaugă posibilitatea de a trimite un contract de utilizare spre semnare electronică prin serviciul extern **SignNow** (API REST, https://docs.signnow.com), de a verifica statusul semnării și de a descărca documentul semnat.

### Flux complet

1. **Frontend** — utilizatorul apasă „Trimite spre semnare" în `contract-management.component.ts` (`startSemnare()` → popup email → `confirmTrimiteSpreSemnare()`), care apelează `contractService.trimiteSpreSemnare(contractId, email)`.
2. **`POST /api/contracte/{id}/trimite-semnare`** (`ContractUtilizareController`) primește `{ emailSemnatar }` (`TrimiteSpreSemnareRequest`) și deleagă la `ContractSemnaturaService.trimiteSpreSemnare()`.
3. **`ContractSemnaturaService.trimiteSpreSemnare(contractId, emailSemnatar)`**:
   - validează email-ul și că fișierul nu e deja semnat (`IllegalStateException` altfel);
   - generează PDF-ul contractului din date (`genereazaPdfContract`, folosind biblioteca **OpenPDF/iText** — tabel cu părți, parcelă, date, arendă, status);
   - urcă PDF-ul în SignNow: `SignNowClient.uploadDocument(pdfBytes, "contract-{id}.pdf")` → `POST {base-url}/document` (multipart) → primește `documentId`;
   - trimite invitația: `SignNowClient.sendFreeFormInvite(documentId, email)` → `POST /document/{id}/invite` cu `{ to, from }` — o invitație **"free form"**, adică semnatarul poate pune semnătura oriunde în document (planurile SignNow free/trial nu permit personalizarea completă a invitației);
   - salvează pe contract: `signNowDocumentId`, `signNowStatus = "trimis"`, `signNowTrimisLa = now()`, `signNowEmailSemnatar`.
4. Utilizatorul apasă „Verifică status" → **`POST /api/contracte/{id}/status-semnare`** → `ContractSemnaturaService.verificaStatusSemnare()`:
   - interoghează `SignNowClient.getDocumentDetails(documentId)` → `GET /document/{id}`;
   - `SignNowClient.isSigned(details)` verifică dacă răspunsul conține un array `signatures` nevid;
   - dacă e semnat: descarcă documentul „aplatizat" (`GET /document/{id}/download?type=collapsed`), calculează hash SHA-256, îl salvează pe disc la `app.signature.storage-path` (implicit `/data/signed-contracts`), marchează `semnatElectronic = true`, `dataSemnaturiiElectronice`, `caleDocumentSemnat`, `hashDocumentSemnat`.
5. **`GET /api/contracte/{id}/document-semnat`** — descarcă PDF-ul final ca `FileSystemResource`, `Content-Type: application/pdf`, `Content-Disposition: attachment`.
6. Token-ul SignNow (`getAccessToken()`) e obținut prin OAuth2 password grant (`POST /oauth2/token` cu `grant_type=password`) și cache-uit în memorie (`cachedAccessToken`/`tokenExpiry`), reînnoit automat cu ~60s marjă înainte de expirare.

### Schemă DB (migrări Flyway, tenant schema)

```sql
-- V47__add_semnatura_electronica_contract_utilizare.sql
ALTER TABLE contracte_utilizare
    ADD COLUMN IF NOT EXISTS semnat_electronic BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS data_semnaturii_electronice TIMESTAMP,
    ADD COLUMN IF NOT EXISTS cale_document_semnat VARCHAR(500),
    ADD COLUMN IF NOT EXISTS hash_document_semnat VARCHAR(128),
    ADD COLUMN IF NOT EXISTS semnat_de_utilizator_id INT;

-- V49__add_signnow_contract_utilizare.sql
ALTER TABLE contracte_utilizare
    ADD COLUMN IF NOT EXISTS signnow_document_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS signnow_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS signnow_trimis_la TIMESTAMP,
    ADD COLUMN IF NOT EXISTS signnow_email_semnatar VARCHAR(255);
```

`V48__redrop_utilizator_operare_fk.sql` e o migrare de reparație: elimină din nou o constrângere FK greșită pe `utilizator_operare_id` (coloana ține un `public.users(id)`, nu un id local de tenant) care fusese reintrodusă accidental din cauza `out-of-order=true` la Flyway.

Config nouă în `application.yml`:
```yaml
app:
  signature:
    storage-path: ${APP_SIGNATURE_STORAGE_PATH:/data/signed-contracts}
  signnow:
    base-url: ${SIGNNOW_BASE_URL:https://api.signnow.com}
    basic-token: ${SIGNNOW_BASIC_TOKEN:}
    username: ${SIGNNOW_USERNAME:}
    password: ${SIGNNOW_PASSWORD:}
```

### Endpointuri noi

| Metodă | Rută | Descriere |
|---|---|---|
| `POST` | `/api/contracte/{id}/trimite-semnare` | Generează PDF, îl urcă în SignNow, trimite invitația |
| `POST` | `/api/contracte/{id}/status-semnare` | Verifică dacă a fost semnat, descarcă și salvează dacă da |
| `GET` | `/api/contracte/{id}/document-semnat` | Descarcă PDF-ul final semnat |

Toate trei moștenesc `@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")` de la nivelul clasei `ContractUtilizareController` — nu e nevoie de adnotare separată.

### Riscuri / puncte de atenție

- **Diacritice pierdute în PDF** — `asciiSafe()` normalizează textul eliminând diacriticele (ă/â/î/ș/ț) înainte de a-l scrie în PDF, pentru că `FontFactory.HELVETICA` folosește encoding WinAnsi/Cp1252 fără fonturi Unicode embedate. Contractele generate vor arăta „Ilesti" în loc de „Iliești" etc. — de rezolvat prin embedding a unui font TTF cu diacritice, dacă contează pentru documente oficiale.
- **Traversal check prezent** — la salvarea documentului semnat, codul verifică explicit `targetPath.startsWith(targetDir)` înainte de scriere, ca să prevină path traversal; e o bună practică deja aplicată, nu un risc.
- **Secrete SignNow** — `basic-token`, `username`, `password` vin din variabile de mediu fără fallback hardcodat (spre deosebire de JWT secret, vezi secțiunea 4) — configurare corectă, dar necesită ca aceste variabile să fie setate în producție altfel serviciul eșuează silențios la primul apel (excepție prinsă și convertită în `400 Bad Request` cu mesajul erorii brute către frontend).
- **Fără retry/backoff** pe apelurile către SignNow — orice eroare HTTP tranzitorie (rate limit, timeout) e propagată direct utilizatorului ca eroare 400.

---

## 2. `feature/evidenta-pasuni-fanete` — Evidență pășuni/fânețe

**Status:** merged (PR #68, commit `49aed8e`)

Modul CRUD complet (backend + frontend) pentru înregistrarea pășunilor și fânețelor asociate unei parcele.

### Flux

`teren-parcele.component.ts` afișează secțiunea „Pășuni/Fânețe" pentru parcela selectată → `pasune-faneata.service.ts` apelează `GET/POST/PUT/DELETE /api/parcele/{parcelaId}/pasuni-fanete` → `PasuneFaneataController` (adnotat `@TenantRequired`, `@PreAuthorize` la nivel de clasă) → `PasuneFaneataService` → `PasuneFaneataRepository` (Spring Data JPA, paginat cu `Pageable`). Filtrare opțională după `tip` (`?tip=PASUNAT` sau `?tip=COSIT`) folosind query param-ul `TipFolosintaPasune`.

### Schemă DB

```sql
CREATE TABLE pasuni_fanete (
    id BIGSERIAL PRIMARY KEY,
    tip_folosinta VARCHAR(20) NOT NULL,
    suprafata_ha DOUBLE PRECISION NOT NULL,
    specii_dominante VARCHAR(255),
    numar_animale_pasunat INTEGER,
    numar_cosiri_anuale INTEGER,
    productie_estimata_kg_ha DOUBLE PRECISION,
    stare_vegetatie VARCHAR(50),
    sistem_intretinere VARCHAR(100),
    sistem_irigare VARCHAR(100),
    observatii VARCHAR(500),
    parcela_id BIGINT NOT NULL,
    CONSTRAINT fk_pasuni_fanete_parcela FOREIGN KEY (parcela_id) REFERENCES parcele(id)
);

CREATE INDEX idx_pasuni_fanete_parcela ON pasuni_fanete(parcela_id);
```

`tip_folosinta` ține enumul `TipFolosintaPasune` (**PASUNAT** / **COSIT**) — `numarAnimalePasunat` e relevant doar pentru PASUNAT, `numarCosiriAnuale` doar pentru COSIT (validare logică, nu constrângere DB).

### Cod cheie

```java
@RestController
@RequestMapping("/api/parcele/{parcelaId}/pasuni-fanete")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class PasuneFaneataController {
    @GetMapping
    public ResponseEntity<?> getAll(@PathVariable Long parcelaId,
            @RequestParam(required = false) TipFolosintaPasune tip, Pageable pageable) { ... }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable Long parcelaId, @Valid @RequestBody PasuneFaneataDTO dto) { ... }
}
```

### Riscuri / puncte de atenție

- Niciun risc de securitate identificat — `@TenantRequired` + `@PreAuthorize` prezente, `@Valid` pe DTO la creare/actualizare, FK cu index dedicat.
- `numarAnimalePasunat`/`numarCosiriAnuale` nu au validare încrucișată cu `tipFolosinta` la nivel de backend — un request poate seta ambele câmpuri simultan sau niciunul, fără eroare.

---

## 3. `feature/evidenta-vita-de-vie` — Evidență viță-de-vie

**Status:** merged (PR #67, commit `dd24c0f`)

Analog structural cu modulul de pășuni/fânețe (secțiunea 2), dar pentru plantații/înregistrări individuale de viță-de-vie.

### Flux

Identic cu #2: `teren-parcele.component.ts` → `vita-de-vie.service.ts` → `GET/POST/PUT/DELETE /api/parcele/{parcelaId}/vita-de-vie` → `VitaDeVieController` → `VitaDeVieService` → `VitaDeVieRepository`. `VitaDeVieService.updateVita()`/`deleteVita()` verifică explicit că înregistrarea aparține parcelei din URL (`if (!vita.getParcela().getId().equals(parcelaId)) throw new RuntimeException("Înregistrarea nu aparține parcelei specificate")`) — o protecție simplă împotriva modificării unei înregistrări dintr-o altă parcelă prin manipularea ID-ului din payload.

### Schemă DB

```sql
CREATE TABLE vita_de_vie (
    id BIGSERIAL PRIMARY KEY,
    tip_inregistrare VARCHAR(20) NOT NULL,
    specie VARCHAR(100) NOT NULL,
    soi VARCHAR(100),
    an_plantare INTEGER,
    numar_vite INTEGER,
    suprafata_ha DOUBLE PRECISION,
    densitate_vite_ha INTEGER,
    stare_vita VARCHAR(50),
    sistem_intretinere VARCHAR(100),
    sistem_irigare VARCHAR(100),
    productie_estimata_kg DOUBLE PRECISION,
    observatii VARCHAR(500),
    parcela_id BIGINT NOT NULL,
    CONSTRAINT fk_vita_de_vie_parcela FOREIGN KEY (parcela_id) REFERENCES parcele(id)
);

CREATE INDEX idx_vita_de_vie_parcela ON vita_de_vie(parcela_id);
```

`tip_inregistrare` ține enumul `TipInregistrareVita` (**IZOLAT** / **PLANTATIE**): `numarVite` relevant pt. IZOLAT, `suprafataHa`/`densitateViteHa` relevante pt. PLANTATIE. `soi` e text liber (ex. Fetească Regală, Cabernet Sauvignon) — fără listă de referință/enum, deci predispus la variații de scriere (ex. "Feteasca Regala" vs "Fetească Regală").

### Riscuri / puncte de atenție

- La fel ca la #2, `RuntimeException` generice sunt folosite pentru erori de business (404/403 logic) în loc de excepții dedicate cu status code — controllerul le prinde generic și le întoarce probabil ca 500 în loc de 400/403/404, în funcție de handler-ul global de excepții.
- `soi` fără validare/enum — risc mic de calitate a datelor, nu de securitate.

---

## 4. `origin/fix/some-bugs` — Hardening securitate, performanță și UX erori

**Status:** deschis, nemerge-uit încă (3 commit-uri peste `main`: `05c13c4`, `21a5d39`, `c946437`)

Trei modificări independente grupate pe acest branch.

### 4.1 `security: externalize JWT secret/DB credentials, harden prod logging` (`05c13c4`)

- **`application.yml`**: secretul JWT capătă fallback pentru dev, dar poate fi suprascris din mediu:
  ```yaml
  jwt:
    # Dev-only fallback so local `mvn spring-boot:run` works without extra setup.
    # Production (see application-prod.yml) requires JWT_SECRET with no fallback.
    secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
  ```
- **`application-prod.yml`** (nou, profil activat alături de profilul de bază în producție via `SPRING_PROFILES_ACTIVE=docker,prod`):
  ```yaml
  server:
    error:
      include-message: never
      include-stacktrace: never
      include-binding-errors: never
  spring:
    jpa:
      show-sql: false
  jwt:
    secret: ${JWT_SECRET}   # fără fallback — eșuează la pornire dacă lipsește
  ```
- **`docker-compose.yml`**: `POSTGRES_USER`/`PASSWORD`/`DB`, `SPRING_DATASOURCE_*` și `JWT_SECRET` sunt acum interpolate din `${VAR:-default}` în loc de valori hardcodate în fișier; `SPRING_PROFILES_ACTIVE` devine `docker,prod`.
- **`Docs/.env.example`** (nou) documentează `GOOGLE_MAPS_API_KEY`, `JWT_SECRET` (`openssl rand -hex 32`), `DB_USERNAME`/`DB_PASSWORD`/`DB_NAME`.

**Risc rezidual:** `application.yml` (profilul de bază, nu `-prod`) păstrează încă valoarea hardcodată `404E63...` ca fallback de dev, deci acest string rămâne vizibil oricui clonează repo-ul. E marcat explicit „dev-only" și profilul `prod` nu-l folosește, dar dacă cineva rulează accidental fără `JWT_SECRET` setat într-un mediu expus, token-urile JWT ar fi semnate cu un secret public.

### 4.2 `perf: add database indexes for foreign keys and common filter columns` (`21a5d39`)

Migrări `V11` (public schema) și `V30` (tenant schema) — indexuri pe FK-uri și coloane des filtrate, motivate explicit în comentariile SQL (scanări full-table pe liste mari):

```sql
-- V30__add_indexes_for_fks_and_filters.sql (tenant), selecție:
CREATE INDEX IF NOT EXISTS idx_gospodarii_uat_id ON gospodarii(uat_id);
CREATE INDEX IF NOT EXISTS idx_terenuri_gospodarie_id ON terenuri(gospodarie_id); -- terenuri și-a pierdut UNIQUE-ul pe gospodarie_id în V10
CREATE INDEX IF NOT EXISTS idx_parcele_teren_id ON parcele(teren_id);
CREATE INDEX IF NOT EXISTS idx_contracte_utilizare_parcela_id ON contracte_utilizare(parcela_id);
CREATE INDEX IF NOT EXISTS idx_contracte_utilizare_status_data_sfarsit ON contracte_utilizare(status_contract, data_sfarsit);
CREATE INDEX IF NOT EXISTS idx_persoane_gospodarii_gospodarie_id ON persoane_gospodarii(gospodarie_id); -- PK compus (persoana_id, gospodarie_id) nu acoperă lookup după gospodarie_id singur
-- + indexuri pe animale_individuale, efective_grup, evenimente_animale, persons, identity_documents, person_relations, cladiri, utilaje, culturi_parcele, documente, uats.judet

-- V11__add_indexes_for_fks_and_filters.sql (public schema):
CREATE INDEX IF NOT EXISTS idx_public_uats_tenant_id ON public.uats(tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON public.users(tenant_id);
```

Migrarea evită explicit coloanele deja acoperite de un `UNIQUE` (`uats.cod_siruta`, `persons.cnp/cui`) sau de un index compus existent.

### 4.3 `refactor: replace alert()/console.log with toast notification service` (`c946437`)

`ToastService` (signal-based Angular, fără dependințe externe):
```ts
@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);
  show(message: string, type: ToastType = 'info', durationMs = 4000): void {
    const id = this.nextId++;
    this.toasts.update(list => [...list, { id, message, type }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }
  success(message: string, durationMs?: number) { this.show(message, 'success', durationMs); }
  error(message: string, durationMs?: number)   { this.show(message, 'error', durationMs); }
}
```
Înlocuiește `alert()`/`console.log` în ~15 componente: `gospodarie-form`, `teren-form`, `contract-management`, `persoana-form`, `document-management`, `machinery-management`, `animal-individual-form`, `efectiv-grup-form`, `cladire-management`, `create-tenant`, `persoana-list`, `tenant-admin-dashboard`, `super-admin-dashboard`, `teren-parcele`, `generic-form`, `parcela-map`.

**Fișiere cheie:** `toast.service.ts`, `toast.component.ts/.html/.scss`, `application-prod.yml`, `V30__add_indexes_for_fks_and_filters.sql`, `Docs/.env.example`

---

## 5. `origin/fix/authorization-integrity` — Autorizare pe controllere + integritate contracte

**Status:** deschis, nemerge-uit încă (2 commit-uri peste `main`: `37d3c8c` „fix: enforce authorization and data integrity controls", `697112d` „Fix")

### 5.1 Autorizare adăugată pe controllere

Adaugă `@PreAuthorize` acolo unde lipsea, pe ~13 controllere: `AnimalController`, `CategorieFolosintaController`, `CladireController`, `ContractUtilizareController`, `CulturaParcelaController`, `GospodarieController`, `MachineryController`, `ParcelaController`, `PersoanaController`, `SursaApaController`, `TerenController`, `UatController`, `UserController`.

Exemplu concret — `UatController` trece de la zero verificare de rol la:
```java
@PostMapping
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
public ResponseEntity<Uat> createUat(@RequestBody Uat uat) { ... }

@PutMapping("/{codSiruta}")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
public ResponseEntity<Uat> updateUat(@PathVariable String codSiruta, @RequestBody Uat request) { ... }

@DeleteMapping("/{codSiruta}")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
public ResponseEntity<Void> deleteUat(@PathVariable String codSiruta) { ... }
```
Înainte de acest fix, `SecurityConfig` cerea doar `.anyRequest().authenticated()` la nivel global — **orice** utilizator autentificat, indiferent de rol (inclusiv `ROLE_USER` dintr-un tenant obișnuit), putea crea/modifica/șterge UAT-uri (tabel de referință global, folosit de toți tenanții), pentru că `UatController` nu avea nicio adnotare proprie. Vezi secțiunea 9 pentru originea acestui gol.

### 5.2 Integritate date — contracte suprapuse

`ContractUtilizareService` capătă o validare nouă, apelată la creare și actualizare:
```java
private void checkNoOverlap(Long terenId, Long parcelaId, Long excludeContractId,
                             LocalDate dataInceput, LocalDate dataSfarsit) {
    if (dataInceput == null || dataSfarsit == null) return;
    Long exclude = excludeContractId != null ? excludeContractId : -1L;
    if (parcelaId != null) {
        if (contractUtilizareRepository.existsOverlappingContract(parcelaId, exclude, dataInceput, dataSfarsit))
            throw new IllegalStateException("Există deja un contract activ pentru această parcelă în intervalul de date specificat.");
    } else if (terenId != null) {
        if (contractUtilizareRepository.existsOverlappingContractByTeren(terenId, exclude, dataInceput, dataSfarsit))
            throw new IllegalStateException("Există deja un contract activ pentru acest teren în intervalul de date specificat.");
    }
}
```
Interogarea JPQL din spate (`ContractUtilizareRepository`):
```java
@Query("SELECT COUNT(c) > 0 FROM ContractUtilizare c " +
       "WHERE c.parcela.id = :parcelaId AND c.id <> :excludeId " +
       "AND c.statusContract <> com.multitenant.model.registru.StatusContractUtilizare.EXPIRAT " +
       "AND c.esteActiv = true " +
       "AND (c.dataSfarsit IS NULL OR c.dataSfarsit >= :dataInceput) " +
       "AND (c.dataInceput IS NULL OR c.dataInceput <= :dataSfarsit)")
boolean existsOverlappingContract(...);
```
Practic: două contracte active (nu EXPIRAT, `esteActiv=true`) pe aceeași parcelă nu pot avea intervale `[dataInceput, dataSfarsit]` care se suprapun. Există o variantă analoagă `existsOverlappingContractByTeren` pentru contractele vechi care încă țin de `Teren` direct (fără `parcela_id` — compatibilitate cu date istorice dinainte de migrarea la parcelă, vezi V27 la secțiunea 1).

Migrare asociată: `V22__add_parcela_id_to_contract_utilizare.sql` (leagă contractul de `Parcela`, nu doar de `Teren`); elimină `V16__fix_parcele_polygon_type.sql`, înlocuită de o versiune ulterioară.

### 5.3 Guard-uri de rută (frontend)

```ts
// auth.guard.ts — blochează dacă nu e autentificat
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (authService.currentUserSubject.value) return true;
  return inject(Router).createUrlTree(['/login']);
};

// role.guard.ts — verifică route.data['roles'] față de rolul curent
export const roleGuard: CanActivateFn = (route) => {
  const user = inject(AuthService).currentUserSubject.value;
  if (!user) return inject(Router).createUrlTree(['/login']);
  const allowedRoles: string[] = route.data['roles'] ?? [];
  if (allowedRoles.length === 0 || allowedRoles.includes(user.role)) return true;
  return inject(Router).createUrlTree([homeForRole(user.role)]); // redirect spre home-ul rolului
};
```
Folosite în `app.routes.ts` pe rutele sensibile, ex:
```ts
{ path: 'super-admin', component: SuperAdminDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_SUPER_ADMIN'] } },
{ path: 'uats', component: UatManagementComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'] } },
```

### Riscuri / puncte de atenție

- **Acesta e branch-ul care remediază** golul de autorizare descris în secțiunea 9 — merită prioritizat la merge, altfel `main` rămâne cu `UatController` neprotejat la nivel de API (frontend-ul blochează navigarea la `/uats`, dar un apel direct către `POST /api/uats` tot ar trece pentru orice user autentificat).
- Guard-urile de rută (`authGuard`/`roleGuard`) sunt doar UX — protecția reală trebuie să existe mereu și pe backend (`@PreAuthorize`), ceea ce acest branch face corect pentru cele 13 controllere enumerate.
- `checkNoOverlap` compară doar pe `parcela_id`/`teren_id` — nu ia în calcul suprapuneri parțiale între contracte pe `teren` vs. pe `parcela` derivată din același teren (un contract pe tot terenul și unul pe o parcelă a acelui teren, în aceeași perioadă, nu s-ar detecta reciproc).

**Fișiere cheie:** `role.guard.ts`, `auth.guard.ts`, `ContractUtilizareService.java` (`checkNoOverlap`), `ContractUtilizareRepository.java`, `app.routes.ts`

---

## 6. `origin/integrareGoogleMaps` — Integrare Google Maps

**Status:** merged (PR #30, commit `9385da6`)

Adaugă hărți interactive și autocomplete de adresă în fluxul de introducere teren/parcelă. **Înlocuiește Leaflet** (`leaflet`, `leaflet-draw`, `@types/leaflet*`) cu `@angular/google-maps` — o schimbare de bibliotecă de hărți, nu doar o adăugare.

### Flux

1. **Încărcare API**: `GoogleMapsLoaderService.load()` injectează dinamic `<script src="https://maps.googleapis.com/maps/api/js?key=...&callback=googleMapsApiLoaded">` o singură dată (promise cache-uit), apoi rezolvă promisiunea din callback-ul global.
2. **`GoogleMapComponent`** (wrapper standalone peste `GoogleMapsModule`) așteaptă `load()` înainte de a seta `apiLoaded = true` și a randa harta.
3. **`parcela-map.component.ts`** (refactor de 166 linii) — desenează/editează poligoane de parcelă direct pe harta Google (înlocuiește layer-ul Leaflet Draw anterior).
4. **`teren-form.component.ts`** (194 linii) — la crearea/editarea unui teren, câmpul de adresă e legat de `StreetAutocompleteComponent`.
5. **`StreetAutocompleteComponent`** — `ControlValueAccessor` custom (`NG_VALUE_ACCESSOR`) cu debounce 400ms + `distinctUntilChanged` + `switchMap` către `StreetAutocompleteService.search(query, city, county)`.

### Cheia API și build

`set-env.js` (rulat la `prestart`/`prebuild` via `npm run set-env`) citește `GOOGLE_MAPS_API_KEY` din `.env` (`dotenv`) și îl injectează prin substituție de regex în `environment.ts`/`environment.prod.ts`, generate din template-uri (`environment.template.ts`):
```js
const generated = content.replace(/googleMapsApiKey:\s*'[^']*'/, `googleMapsApiKey: '${apiKey}'`);
```
Dacă lipsește cheia, avertizează la build (`console.warn`) dar nu blochează build-ul.

### Riscuri / puncte de atenție

- **Confuzie de nume — nu e Google Places**: deși branch-ul se numește „integrareGoogleMaps", `StreetAutocompleteService` folosește de fapt **Nominatim (OpenStreetMap)**, nu Google Places API:
  ```ts
  let url = `https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&limit=8&countrycodes=ro&email=admin@registru.ro&street=${encodeURIComponent(query)}`;
  ```
  Practic sunt două servicii externe diferite: Google Maps pentru afișarea hărții/poligoane, Nominatim (gratuit, fără cheie) pentru autocomplete stradă. Nu e un bug, dar merită clarificat în documentația publică a feature-ului ca să nu se caute din greșeală o cheie Google Places API care nu există.
  Notă: `email=admin@registru.ro` e trimis către Nominatim conform politicii lor de utilizare (identificare pentru rate-limiting), nu e o scurgere de date sensibile.
- **Cheia Google Maps ajunge în bundle-ul JS livrat browserului** (`environment.ts` e compilat în frontend) — normal pentru Maps JavaScript API, dar cheia **trebuie restricționată în Google Cloud Console** (HTTP referrer whitelist pe domeniul aplicației), altfel oricine poate inspecta bundle-ul și reutiliza cheia pe alt site.
- Nominatim are un rate-limit strict (1 request/secundă, politică de utilizare echitabilă) — potrivit pentru volum mic de utilizatori, dar nepotrivit dacă traficul crește; nu are SLA.

**Fișiere cheie:** `google-maps-loader.service.ts`, `google-map.component.ts`, `parcela-map.component.ts`, `street-autocomplete.component.ts`, `street-autocomplete.service.ts`, `set-env.js`

---

## 7. `origin/CategorieFolosinta` — CRUD Categorie de Folosință

**Status:** merged (PR #16, commit `2be0127`)

Modul pentru clasificarea modului de utilizare a unui teren (ex: Arabil, Pășune, Livadă). Documentat deja separat și mai amănunțit din perspectivă de utilizator în [`Docs/CategorieFolosinta.md`](./CategorieFolosinta.md).

### Schemă DB

```sql
CREATE TABLE IF NOT EXISTS categorie_folosinta (
    id SERIAL PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    descriere TEXT,
    teren_id INT NOT NULL REFERENCES teren(id) ON DELETE CASCADE
);
```
`ON DELETE CASCADE` — ștergerea unui teren șterge automat toate categoriile lui de folosință.

### Endpointuri

```java
@GetMapping("/api/terenuri/{terenId}/categorii-folosinta")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
public ResponseEntity<?> getCategoriiForTeren(@PathVariable Long terenId) { ... }

@PostMapping("/api/terenuri/{terenId}/categorii-folosinta")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_USER')")
public ResponseEntity<?> createCategorie(@PathVariable Long terenId, @RequestBody CategorieFolosinta categorie) { ... }

@PutMapping("/api/categorii-folosinta/{id}")     // notă: ruta de update/delete NU e sub /terenuri/{terenId}/...
@DeleteMapping("/api/categorii-folosinta/{id}")
```

### Flux frontend

Panou dedicat în `teren-parcele.component.ts/.html/.css`, deasupra listei de parcele: vizualizare directă, „+ Adaugă Categorie" (denumire + descriere), editare (✎) și ștergere cu confirmare (✕) — detaliat în `Docs/CategorieFolosinta.md`.

### Riscuri / puncte de atenție

- `updateCategorie`/`deleteCategorie` sunt adresate direct prin `id`-ul categoriei (`/api/categorii-folosinta/{id}`), fără să verifice explicit în controller că `terenId` din context aparține tenantului curent — protecția se bazează pe filtrarea implicită per-tenant la nivel de schema/DataSource (multi-tenant prin schema separată), nu pe o verificare explicită suplimentară în acest controller.

**Fișiere cheie:** `CategorieFolosintaController.java`, `V11__create_categorie_folosinta_table.sql`, `Docs/CategorieFolosinta.md`

---

## 8. `origin/documentation` — Refactor module timpurii + walkthrough

**Status:** merged (PR #10, commit `3f68378`)

Cel mai vechi branch din listă; conține refactorizări din faza incipientă a aplicației, **dinainte de arhitectura multi-tenant curentă** (schema `tenant` per gospodărie/UAT). Schema inițială din acea vreme era plată:
```sql
-- V1__init_tenant_schema.sql (epoca acestui branch)
CREATE TABLE uat (
    id SERIAL PRIMARY KEY,
    cod_siruta VARCHAR(50) NOT NULL,
    denumire VARCHAR(255) NOT NULL,
    judet VARCHAR(100) NOT NULL,
    tip_uat VARCHAR(50) NOT NULL
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- ADMIN or USER
    tenant_id VARCHAR(255),
    ...
);
```
Curăță payload-uri (`JwtResponse`, `LoginRequest`, `TenantCreateRequest`), actualizează `TenantService`, refactorizează UI-ul dashboard-urilor de super-admin/tenant-admin/user-management (mult cod vechi eliminat — 660 fișiere afectate în total în diff-ul brut, majoritatea prin curățarea de cod dead/duplicat). Include și un `walkthrough.md` original, ulterior eliminat/mutat din rădăcina repo-ului.

### Riscuri / puncte de atenție

- **Conținutul e depășit** — restul branch-urilor din această listă (module noi, Google Maps, autorizare) au fost construite pe deasupra arhitecturii multi-tenant care a rezultat *după* acest branch; nu conține cod relevant pentru munca curentă, e păstrat doar ca istoric.

---

## 9. `uat_crud` — Pagină management UAT-uri (CRUD)

**Status:** merged direct în `main` (fără PR), ultimul commit `e794aac`, autor `carinaa1`

Adaugă o pagină de administrare pentru UAT-uri (unități administrativ-teritoriale), accesibilă din dashboard-ul de super-admin.

### Schemă DB

```sql
-- V3__create_public_uat_table.sql
CREATE TABLE public.uat (
    id SERIAL PRIMARY KEY,
    cod_siruta VARCHAR(50) NOT NULL UNIQUE,
    denumire VARCHAR(255) NOT NULL,
    judet VARCHAR(100) NOT NULL,
    tip_uat VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO public.uat (cod_siruta, denumire, judet, tip_uat, is_active) VALUES
('54975', 'Cluj-Napoca', 'Cluj', 'Municipiu', true),
('55311', 'Florești', 'Cluj', 'Comună', true),
('1017', 'București', 'București', 'Municipiu', false);

-- V4__add_user_profile_columns.sql
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS nume VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS activ BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS uat_id INTEGER REFERENCES public.uat(id);
```

### Endpointuri (ORIGINAL — vezi secțiunea 5 pentru fix)

```java
@RestController
@RequestMapping("/api/uats")
public class UatController {
    @PostMapping                      public Uat createUat(@RequestBody Uat uat) { ... }
    @GetMapping                       public List<Uat> getAllUats() { ... }
    @GetMapping("/{codSiruta}")       public Uat getUat(@PathVariable String codSiruta) { ... }
    @PutMapping("/{codSiruta}")       public Uat updateUat(@PathVariable String codSiruta, @RequestBody Uat uat) { ... }
    @DeleteMapping("/{codSiruta}")    public void deleteUat(@PathVariable String codSiruta) { ... }
}
```

### Flux frontend

`uat-management.component.ts/.html/.scss` — tabel de UAT-uri, cu `HttpClient` direct în componentă (nu printr-un service dedicat): `GET this.apiUrl`, `PUT {apiUrl}/{codSiruta}`, `POST apiUrl`, `DELETE {apiUrl}/{codSiruta}`. Link nou în `super-admin-dashboard.component`, rută nouă `{ path: 'uats', component: UatManagementComponent }` în `app.routes.ts`.

### Riscuri / puncte de atenție

- **Golul de autorizare care a motivat `fix/authorization-integrity`**: `UatController` nu are nicio adnotare `@PreAuthorize`, deci `SecurityConfig`-ul global (`.anyRequest().authenticated()`) e singura protecție — orice utilizator autentificat, indiferent de rol, poate crea/edita/șterge UAT-uri prin apel direct la API, chiar dacă ruta frontend `/uats` e restricționată la `ROLE_ADMIN`/`ROLE_SUPER_ADMIN` de `roleGuard` (protecție doar de UX, ocolibilă). Fixul e deja scris în branch-ul din secțiunea 5, dar nemerge-uit încă în `main`.
- Cod comentat lăsat în `uat-management.component.ts` (versiuni vechi ale apelurilor `PUT`/`POST`/`DELETE` fără `{ ... }` de subscribe cu `next/error`, înlocuite dar nu șterse) — curățare minoră recomandată la următorul merge.
- `deleteUat` întoarce `void` (fără body) în loc de un status code explicit verificat — comportament implicit Spring (200 OK dacă nu aruncă excepție), acceptabil dar inconsistent cu restul API-ului care întoarce `ResponseEntity` explicit.

**Fișiere cheie:** `UatController.java`, `V3__create_public_uat_table.sql`, `uat-management.component.ts`

---

## Recomandări

1. **Curățenie remote:** branch-urile ✅ **Merged** (`semnatura-signnow`, `evidenta-pasuni-fanete`, `evidenta-vita-de-vie`, `integrareGoogleMaps`, `CategorieFolosinta`, `documentation`, `uat_crud`) pot fi șterse — conținutul lor e deja în `main`, păstrarea lor nu aduce beneficii și încarcă lista de branch-uri.
2. **Prioritate de merge:** `origin/fix/authorization-integrity` ar trebui integrat curând — repară un gol real de autorizare pe `/api/uats` (și alte 12 controllere) prezent azi în `main` prin `uat_crud`, plus validarea de suprapunere a contractelor.
3. **`origin/fix/some-bugs`** conține trei schimbări independente utile (secrete externalizate, indexuri DB, toast UX) fără riscuri majore — candidat bun pentru merge rapid, eventual după ce e verificat că `JWT_SECRET` chiar e setat în mediul de producție curent.
4. **Clarificare denumire:** branch-ul „integrareGoogleMaps" ar merita menționat explicit (în PR sau README) că autocomplete-ul de adresă folosește Nominatim/OpenStreetMap, nu Google Places, pentru viitorii dezvoltatori care caută o cheie API separată.
