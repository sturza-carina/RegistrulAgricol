# Auditare acces date personale și istoric versiuni entități

> Documentație tehnică pentru două funcționalități de securitate/conformitate adăugate în RegistrulAgricol, corespunzătoare punctelor **2.3** și **5.2** din roadmap-ul de dezvoltare.

---

## De ce au fost necesare aceste funcționalități

RegistrulAgricol gestionează date sensibile despre cetățeni (CNP, adrese, structură familie) și date despre proprietăți (terenuri, parcele, clădiri) care pot deveni probă în litigii de proprietate sau în verificări de eligibilitate pentru fonduri APIA. Până acum, aplicația nu avea niciun mecanism care să răspundă la două întrebări esențiale:

1. **"Cine a văzut sau modificat datele acestui cetățean, și când?"** — cerință legală explicită conform GDPR (Regulamentul UE 2016/679).
2. **"Cine a modificat această parcelă/clădire/gospodărie, când și ce anume s-a schimbat?"** — necesar pentru a putea demonstra, în caz de dispută, un istoric complet și de încredere al modificărilor.

Fără aceste mecanisme, primăria nu putea demonstra conformitate în caz de control sau plângere GDPR, iar o modificare greșită sau frauduloasă a unei suprafețe de teren nu lăsa nicio urmă verificabilă.

---

## Task 1 — Registru GDPR de auditare a accesului la date personale

### Sursă legală

Regulamentul (UE) 2016/679 (GDPR), în special:
- **Art. 5** — principiul de responsabilitate (accountability): operatorul de date trebuie să poată demonstra conformitatea, nu doar să o afirme.
- **Art. 32** — securitatea prelucrării datelor cu caracter personal.

Normele tehnice recente pentru registrul agricol (Norma din 17.02.2025) reamintesc explicit că toate entitățile care completează registrul agricol prelucrează date cu caracter personal sub incidența GDPR.

### Ce face, concret

Orice vizualizare sau modificare a datelor unei persoane fizice sau ale unei gospodării (CNP, adresă, structură familie, proprietăți) generează automat o intrare de audit, **fără nicio acțiune manuală din partea funcționarului**. Sistemul înregistrează:

- **cine** a accesat datele (utilizatorul autentificat)
- **când** (timestamp)
- **ce tip de acțiune** (vizualizare, creare, modificare, ștergere)
- **ce entitate și ce persoană** au fost vizate
- **pe ce endpoint** s-a făcut accesul
- **din ce tenant/UAT**

### Cum funcționează tehnic

1. Metodele din controllere care expun date personale (`PersoanaController`, `GospodarieController`) sunt marcate cu o adnotare custom `@GdprAudited(entity = "...")`.
2. Un `@Aspect` Spring (AOP) interceptează automat orice apel către aceste metode — indiferent din ce parte a aplicației vine cererea (Angular, Postman, alt client).
3. Aspectul extrage automat utilizatorul curent (din `SecurityContextHolder`), tipul de acțiune (din metoda HTTP) și ID-ul persoanei vizate, apoi salvează o intrare în tabela `gdpr_audit_logs`.
4. Scrierea în audit se face într-o **tranzacție separată** (`REQUIRES_NEW`), astfel încât:
   - accesul e înregistrat chiar dacă restul cererii eșuează ulterior;
   - un eșec la scrierea log-ului de audit **nu blochează** acțiunea utilizatorului — orice problemă e doar loghată separat, nu afectează experiența funcționarului.
5. Pentru endpoint-urile care întorc liste de persoane (nu o singură persoană), sistemul loghează toate ID-urile accesate într-o singură intrare, ca să nu genereze un volum excesiv de rânduri în baza de date fără să piardă informația despre cine a fost vizat.

### Ce NU face (deocamdată)

Scope-ul inițial acoperă `Persoana` și `Gospodarie`, extins ulterior cu `Document` (vezi secțiunea de extindere de mai jos). Alte entități cu date sensibile (de exemplu `AnimalIndividual`, legate de evidența crotaliilor) nu sunt încă acoperite — a fost o decizie conștientă de a limita efortul inițial, discutabilă ca extindere ulterioară.

---

## Task 2 — Istoric de audit detaliat pentru entități (Hibernate Envers)

### De ce contează

Spre deosebire de task-ul 1 (care răspunde la "*cine a accesat*"), acest task răspunde la "*ce s-a schimbat, față de ce a fost înainte*". Dacă cineva modifică suprafața unei parcele sau categoria de folosință a unui teren și ulterior apare o dispută de proprietate, e esențial să existe un istoric complet — nu doar valoarea curentă din baza de date.

### Ce face, concret

Orice modificare pe entitățile **`Parcela`**, **`Cladire`** și **`Gospodarie`** generează automat o versiune istorică, interogabilă ulterior. Pentru fiecare modificare se poate afla:
- cine a făcut modificarea
- când
- ce câmpuri anume s-au schimbat, cu valoarea veche și valoarea nouă (diff pe câmp)

### Cum funcționează tehnic

1. Entitățile `Parcela`, `Cladire`, `Gospodarie` au fost adnotate cu `@Audited` (Hibernate Envers) — Hibernate ține automat o versiune istorică la fiecare `INSERT`/`UPDATE`/`DELETE`, fără cod suplimentar de audit scris manual.
2. Un `RevisionListener` custom (`CustomRevisionListener`) leagă fiecare revizie de utilizatorul curent din `SecurityContextHolder`, ca să se știe exact cine a făcut modificarea, nu doar când.
3. Pentru fiecare tenant (UAT), Envers generează propriile tabele de istorie (`gospodarii_aud`, `cladiri_aud`, `parcele_aud`, `revinfo`), respectând izolarea multi-tenant existentă în aplicație (schema-per-tenant).
4. Endpoint-ul `GET /api/parcele/{id}/istoric` interoghează istoricul prin `AuditReader` și calculează automat diferențele față de versiunea anterioară pentru fiecare câmp relevant (denumire, suprafață, categorie de folosință, poligon).

### Ce NU acoperă (deocamdată)

Inițial doar `Parcela`, `Cladire`, `Gospodarie` erau auditate, scop extins ulterior cu ierarhia `Persoana` și cu `Teren` (vezi secțiunea de extindere de mai jos). Entități precum `Machinery` sau evidența animalelor (`AnimalIndividual`, `EfectivGrup`) tot nu au istoric Envers — motivul principal a fost limitarea volumului de date generat (fiecare entitate `@Audited` suplimentară înseamnă un tabel de istorie nou și un rând nou la fiecare modificare), dar rămâne un candidat de extindere dacă echipa decide că e necesar.

---

## Diferența dintre cele două mecanisme

| | GDPR Audit Log | Hibernate Envers |
|---|---|---|
| **Răspunde la** | Cine a *accesat* datele? | Ce s-a *schimbat* și cine a schimbat? |
| **Acoperă și vizualizări (GET)?** | Da | Nu — doar modificări (INSERT/UPDATE/DELETE) |
| **Entități acoperite** | `Persoana`, `Gospodarie`, `Document` | `Parcela`, `Cladire`, `Gospodarie`, `Persoana` (+ subclase), `Teren` |
| **Scop principal** | Conformitate GDPR (demonstrarea accesului) | Istoric de încredere pentru dispute de proprietate |
| **Mecanism** | Spring AOP (`@Aspect`) | Hibernate Envers (`@Audited`) |

Cele două mecanisme sunt complementare, nu redundante: GDPR-ul urmărește *accesul* la datele persoanelor fizice, Envers urmărește *modificările* asupra proprietăților (teren/clădiri/gospodării), care sunt entități diferite cu cerințe legale diferite.

---

## Structura fișierelor adăugate

**Task 1 — GDPR Audit Log**
- `annotation/GdprAudited.java` — adnotarea custom
- `aspect/GdprAuditAspect.java` — interceptorul AOP
- `model/audit/GdprAuditLog.java` — entitatea de audit
- `repository/GdprAuditLogRepository.java`
- `service/GdprAuditService.java` — logare cu tranzacție separată
- `resources/db/tenant/V32__create_gdpr_audit_log_table.sql`
- `controller/DocumentController.java` — adnotat ulterior cu `@GdprAudited(entity = "Document")`

**Task 2 — Hibernate Envers**
- `model/audit/CustomRevisionEntity.java` / `CustomRevisionListener.java`
- `dto/FieldDiff.java`, `dto/ParcelaRevisionDto.java`
- `resources/db/tenant/V33__create_envers_audit_tables.sql`
- Modificări pe `Parcela.java`, `Cladire.java`, `Gospodarie.java` (adnotare `@Audited`)
- Metodă nouă `getParcelaHistory()` în `ParcelaService.java` + endpoint `GET /api/parcele/{id}/istoric`
- `model/persoana/Persoana.java`, `PersoanaFizica.java`, `PersoanaJuridica.java` — adnotate ulterior cu `@Audited`
- `model/registru/Teren.java` — adnotat ulterior cu `@Audited`
- `resources/db/tenant/V36__add_persons_and_teren_to_envers.sql`

---

## Extindere — acoperire suplimentară pentru Document, Persoana și Teren

> Continuare a celor două task-uri de mai sus, cu extinderea scope-ului la entități care nu erau acoperite inițial.

### Task 1 — GDPR Audit Log extins la Documente

Endpoint-urile REST din `DocumentController.java` au fost adnotate cu `@GdprAudited(entity = "Document")`, astfel încât operațiile standard asupra documentelor unei gospodării (VIEW, CREATE, UPDATE, DELETE) sunt acum logate automat în `gdpr_audit_logs`, la fel ca pentru `Persoana` și `Gospodarie`. Auditarea pentru `ActIdentitate` și relațiile de rudenie din `PersoanaController` era deja acoperită prin interceptoarele și aspectele existente — nu a fost nevoie de cod suplimentar pentru acestea.

### Task 2 — Hibernate Envers extins la ierarhia Persoana și la Teren

- `Persoana.java` și subclasele sale (`PersoanaFizica.java`, `PersoanaJuridica.java`) au fost adnotate cu `@Audited`, astfel încât modificările asupra datelor unei persoane fizice/juridice au acum și ele istoric de versiuni, nu doar `Parcela`/`Cladire`/`Gospodarie`. Colecțiile care nu trebuiau incluse în istoric au fost marcate explicit cu `@NotAudited`, ca să evite probleme de mapare Hibernate.
- `Teren.java` a fost adnotat similar cu `@Audited`.
- A fost adăugată migrarea `V36__add_persons_and_teren_to_envers.sql`, cu atenție specială la tipul coloanelor generate — în particular `persons_aud.id` a trebuit tipizat explicit ca `BIGINT`, ca să corespundă cu tipul `Long` din entitate.


---

## Note pentru discuția cu echipa

- Ambele funcționalități sunt **transparente pentru utilizatorul final** — nimic vizibil în UI nu s-a schimbat pentru funcționarul care lucrează cu aplicația; auditul se întâmplă automat, pe server, la fiecare cerere.
- Volumul de date generat de ambele mecanisme crește proporțional cu utilizarea aplicației — merită o discuție ulterioară despre politică de retenție/arhivare, mai ales pentru `gdpr_audit_logs`.
- Extinderea scope-ului (mai multe entități auditate) e posibilă oricând ulterior, fără schimbări structurale — doar adăugare de adnotări/configurare.
