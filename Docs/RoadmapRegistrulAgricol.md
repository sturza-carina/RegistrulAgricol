# Roadmap RegistrulAgricol — Propuneri de dezvoltare

> Document generat pe baza analizei Antigravity a codului existent, `docs/research.md`
> și legislației aplicabile. Ultima actualizare: iulie 2026.

**Acte normative de referință:**
- [Codul Fiscal (Legea nr. 227/2015)](https://static.anaf.ro/static/10/Anaf/legislatie/Cod_fiscal_norme_2023.htm) — Titlul IV (impozit pe venit), Titlul IX (impozite și taxe locale)
- [Legea nr. 145/2014](https://legislatie.just.ro/Public/DetaliiDocument/162616) — reglementarea pieței produselor din sectorul agricol (atestat de producător, carnet de comercializare)
- [OG nr. 28/2008](https://legislatie.just.ro/Public/DetaliiDocument/187493) — privind registrul agricol
- [HG nr. 1627/2024](https://legislatie.just.ro/Public/DetaliiDocumentAfis/292458) — aprobă formularele registrului agricol pentru perioada 2025-2029 (16 capitole standardizate); publicată în M.O. nr. 1275 bis din 12 decembrie 2024, completată de Ordinul comun MADR/MLPDA/MAI/MFP/INS/ANCPI/ANSVSA nr. 51/348/59/765/285/14633/678 din 2025 (norme tehnice de completare)
- [Regulamentul (UE) 2016/679 — GDPR](https://eur-lex.europa.eu/legal-content/RO/TXT/?uri=celex%3A32016R0679) — protecția datelor cu caracter personal

> Notă: linkurile duc la formele consolidate/actualizate publicate pe Portalul Legislativ (legislatie.just.ro) și EUR-Lex. Codul Fiscal se modifică frecvent prin ordonanțe de urgență — verificați forma în vigoare la momentul implementării, nu doar la momentul citirii acestui document.

---

## Cum se citește acest document

Fiecare propunere are:
- **Sursă legală** — actul normativ pe care se bazează (util pentru documentarea deciziei în discuția cu echipa)
- **Efort** — estimare mică/medie/mare, cu zile aproximative
- **Dependențe** — ce entități/module trebuie să existe deja
- **Riscuri / lucruri de clarificat** — ce ar putea bloca sau complica implementarea
- **Criterii de acceptare** — cum știi că e "gata"

Propunerile sunt grupate pe cele 7 categorii, iar la final e un **plan pe faze** cu ordine de execuție recomandată.

---

## 0. Analiza de conformitate — cele 16 capitole ale registrului agricol (HG 1627/2024)

Registrul agricol pentru perioada 2025-2029 e împărțit legal în 16 capitole standardizate. Tabelul de mai jos arată, capitol cu capitol, ce e deja acoperit de aplicație și ce lipsește — util ca listă de verificare separată de propunerile funcționale de mai jos, și ca argument documentat pentru discuția despre ce rămâne de acoperit legal, nu doar "de bun simț".

| Capitol | Scop legal | Status curent | Ce lipsește |
|---|---|---|---|
| **Cap. I** | Componența gospodăriei fără personalitate juridică | Parțial (`PersoanaFizica`, `PersoanaJuridica`, `RelatieRudenie`) | Desemnarea explicită a "capului de gospodărie"; data intrării/ieșirii din gospodărie (naștere, deces, căsătorie, schimbare domiciliu) |
| **Cap. II** | Terenuri în proprietate și identificare pe parcele | Parțial (`Teren`, `Parcela`, categorii de folosință dinamice) | Număr cadastral și Carte Funciară (CF); flag Intravilan/Extravilan per parcelă; "titular drept folosință" per parcelă; cod bloc fizic/parcelă fizică (pentru declarații APIA) |
| **Cap. III** | Sinteza modului de utilizare a suprafețelor agricole | Parțial | Calcul automat de agregare a suprafeței totale pe categorie de folosință (arabil, pășune, livadă, vie, pădure) la nivelul UAT |
| **Cap. IV** | Suprafața arabilă cultivată | Parțial (`CulturaParcela`) | Nomenclatorul de culturi existent ca lookup table ar trebui verificat/aliniat explicit cu listele standard MADR/APIA, dacă nu e deja |
| **Cap. V** | Pomicultură — pomi răzleți și plantații | **Lipsește complet** | Evidența pomilor izolați pe specie (măr, păr, prun, nuc) și a plantațiilor structurate |
| **Cap. VI** | Suprafețe efectiv irigate | Parțial (`SursaApa` legată de `Parcela`) | Suprafața efectiv irigată (ha) pe tip de cultură și an agricol, nu doar sursele de apă |
| **Cap. VII** | Registrul animalelor domestice/sălbatice în captivitate | Implementat (`AnimalIndividual`, `EvenimentAnimal`, `EfectivGrup`, `crotal_registry` unic) | Aliniat bine cu regulile SNIIA (ANSVSA); necesită doar standardizarea listelor de specii |
| **Cap. VIII** | Date eșantion statistic | **Lipsește complet** | Metadate statistice speciale cerute de INS |
| **Cap. IX** | Utilaje, instalații agricole și silvice | Parțial (`Machinery` legată de `Gospodarie`) | Statutul de proprietate (deținut vs. închiriat/leasing); gruparea tractoarelor pe clase de putere (CP), conform normelor INS |
| **Cap. X** | Aplicarea îngrășămintelor și pesticidelor | **Lipsește complet** | Evidența tratamentelor chimice — cantitate îngrășăminte chimice vs. organice și tipuri de pesticide aplicate per parcelă (cerut pentru conformitate de mediu) |
| **Cap. XI** | Clădiri și construcții-anexe | Parțial (`Cladire`) | Materiale pereți exteriori (cărămidă, lemn, beton, chirpici) și acoperiș (țiglă, tablă) pentru încadrare fiscală; racorduri la utilități (electricitate, apă, gaz, canalizare, încălzire) — obligatorii explicit în capitol |
| **Cap. XII** | Producția vegetală obținută | Parțial (coloane de producție pe `CulturaParcela`) | Separarea producției din sere/solarii față de câmp deschis |
| **Cap. XIII** | Mențiuni privind succesiunile | **Lipsește complet** | Urmărirea notificărilor trimise notarilor publici (SPN/BIN) privind reședinți decedați și bunurile lor înregistrate |
| **Cap. XIV** | Dreptul de preempțiune | **Lipsește complet** | Registru de oferte de vânzare teren, anunțuri publice, înregistrare preemptori, aprobări MADR/DAJ (Legea 17/2014) |
| **Cap. XV** | Arendă și concesiune | Implementat (`ContractUtilizare` cu scheduler) | Migrarea relației către `Parcela` în loc de `Teren` (contractul se aplică pe parcele specifice, nu pe tot terenul) |
| **Cap. XVI** | Mențiuni speciale, comodat, închiriere | Implementat (via `ContractUtilizare` tip `COMODAT`/`INCHIRIERE`/`ALTELE`) | — |

**Cum se folosește acest tabel:** capitolele marcate "Lipsește complet" (V, VIII, X, XIII, XIV) sunt cele mai vizibile goluri de conformitate — merită discutate ca prioritate de listat, chiar dacă nu toate ajung să fie implementate în timpul internship-ului. Capitolele "Parțial" au deja fundația în cod și necesită doar extinderea schemei cu câmpurile lipsă, ceea ce le face candidați buni pentru Faza 2 din planul de mai jos.

---

## 1. Modul taxe/impozite

Este realist să integrăm calculul impozitelor peste modelul actual — datele geometrice, destinațiile clădirilor și categoriile de folosință există deja.

### 1.1 Calculul automat al impozitului pe proprietate (clădiri și terenuri)

- **Sursă legală:** [Codul Fiscal (Legea 227/2015)](https://legislatie.just.ro/Public/DetaliiDocument/171282), Titlul IX — art. 457 (clădiri persoane fizice), art. 460 (clădiri persoane juridice), art. 465 (terenuri)
- **De ce contează:** În prezent, calculul impozitului pe proprietate se face separat, într-un sistem de contabilitate/impozite locale al primăriei (DITL), pe baza unor date transmise manual sau pe hârtie din registrul agricol. Dacă RegistrulAgricol calculează el însuși o estimare, funcționarul poate răspunde imediat unui cetățean la ghișeu ("cam cât ar trebui să plătesc anul ăsta?") fără să mai treacă printr-un alt sistem, iar erorile de transcriere între cele două sisteme dispar.
- **Ce face:** Registrul agricol e sursa oficială pentru categoriile de folosință și suprafețele pe baza cărora DITL stabilește obligațiile de plată. Formule de bază:
  - Clădiri: `suprafață desfășurată × valoare impozabilă pe m² × coeficient zonă/rang localitate × coeficienți de corecție (an construcție, instalații existente)`
  - Teren intravilan/extravilan: `suprafață (ha) × cotă stabilită prin HCL × coeficient corecție rang localitate`
  - Cota diferă pentru clădiri rezidențiale vs. nerezidențiale/mixte — sistemul trebuie să distingă destinația clădirii (atribut deja existent pe `Cladire`)
- **Interfață propusă:** un tab nou "Calcul impozit estimat" în pagina gospodăriei, cu breakdown pe fiecare bun (clădire/teren în parte) și formula vizibilă, ca funcționarul să poată explica suma unui cetățean la ghișeu, nu doar să afișeze un total opac
- **Efort:** Mediu — 4-5 zile
- **Entități noi:**
  - `TaxRateTable` — cote stabilite prin HCL anual, per UAT (categorie folosință, zonă fiscală A/B/C/D, cotă impozit)
  - Coloană nouă `zona_fiscala` (A/B/C/D) pe `gospodarii` sau `terenuri`
- **Dependențe:** `Teren`, `Parcela`, `Cladire`, `Uats`
- **Riscuri:**
  - Cotele HCL diferă de la o UAT la alta și se schimbă anual — trebuie un ecran de administrare pentru actualizarea lor, nu hardcodate în cod
  - Valorile impozabile pe m² pentru clădiri au grile complexe (materiale construcție, an, instalații) — merită să începeți cu o versiune simplificată (un coeficient mediu) și să extindeți iterativ pe măsură ce validați împreună cu echipa
  - Rezultatul e o **estimare**, nu suma oficială de plată — merită etichetat clar în UI ca atare, ca să nu fie confundat cu decizia de impunere emisă de DITL
- **Criterii de acceptare:** Din pagina gospodăriei, un tab "Calcul impozit estimat" afișează suma calculată pentru fiecare clădire/teren, cu formula vizibilă (transparență pentru funcționar), plus o mențiune clară că e o estimare

### 1.2 Evidența mijloacelor de transport vs. utilaje scutite

- **Sursă legală:** [Codul Fiscal (Legea 227/2015)](https://legislatie.just.ro/Public/DetaliiDocument/171282), Titlul IX, Cap. IV — art. 468-470 (impozit mijloace de transport), art. 469 (scutiri)
- **De ce contează:** Multe utilaje agricole (tractoare, combine) sunt scutite de impozit dacă sunt folosite exclusiv în agricultură, dar în practică multe primării nu au o evidență clară a acestei distincții — utilajele sunt fie toate impozitate din prudență, fie toate scutite fără verificare. Un câmp explicit `scutit_agricultura` pe fiecare utilaj face vizibilă și auditabilă această decizie.
- **Ce face:** Grila de calcul se aplică pe bază de capacitate cilindrică (similar impozitului auto standard), dar utilajele marcate ca folosite exclusiv agricol sunt excluse din calcul
- **Efort:** Mic — 1-2 zile
- **Modificări entități:** Extinderea `Machinery` cu: `capacitate_cilindrica`, `este_inmatriculat`, `numar_inregistrare_primarie`, `proprietar_id`, `scutit_agricultura` (boolean)
- **Dependențe:** `Machinery`
- **Riscuri:** Definirea exactă a "folosire exclusiv agricolă" poate necesita o declarație/verificare suplimentară din partea proprietarului — de clarificat în echipă dacă e suficient un simplu checkbox sau dacă trebuie un flux de aprobare
- **Criterii de acceptare:** Grila de calcul pe bază de capacitate cilindrică generează suma corectă, iar utilajele marcate `scutit_agricultura = true` sunt excluse automat din calculul impozitului

### 1.3 Estimator impozit pe venitul agricol (animale și culturi)

- **Sursă legală:** [Codul Fiscal (Legea 227/2015)](https://legislatie.just.ro/Public/DetaliiDocument/171282), Titlul IV, Cap. VII — art. 103-107
- **De ce contează:** Spre deosebire de impozitul pe proprietate (care merge la bugetul local), acest impozit merge la bugetul de stat prin Declarația Unică, deci primăria nu îl colectează direct — rolul ei e doar de a confirma prin adeverință efectivele de animale/suprafețele cultivate pe care ANAF le folosește apoi pentru calcul. Practic, propunerea nu calculează impozitul final, ci ajută funcționarul să identifice din timp gospodăriile care depășesc pragul de scutire, ca să nu fie surprinse la depunerea declarației.
- **Ce face:** Impozitul se calculează pe baza normelor de venit ANAF (stabilite anual, pe județ și specie), aplicabile peste anumite praguri de scutire (ex. peste 2 vaci, peste 50 de oi — pragurile exacte variază pe specie și se actualizează anual de ANAF)
- **Efort:** Mediu-Mare — 5-7 zile
- **Entități noi:** `AgriculturalIncomeNorm` (norme ANAF pe județ, specie, prag minim scutit — introduse manual de admin, actualizate anual)
- **Dependențe:** `AnimalIndividual`, `EfectivGrup`, `CulturaParcela`, `Persoana`
- **Riscuri:** Normele ANAF nu sunt disponibile ca API public, deci necesită introducere manuală anuală de către admin, cu risc de date vechi dacă nu există un proces de actualizare. De clarificat în echipă dacă merită efortul față de propunerea 1.1, care are impact vizual similar cu efort comparabil, dar rezultat mai direct utilizabil (impozitul local se plătește efectiv la primărie, spre deosebire de cel de venit agricol).
- **Criterii de acceptare:** Sistemul semnalează automat gospodăriile care depășesc pragul de scutire pe specie și poate genera adeverința cu efectivele pentru ANAF

---

## 2. Conformitate legală

### 2.1 Modul generare atestate de producător și carnete de comercializare

- **Sursă legală:** [Legea 145/2014](https://legislatie.just.ro/Public/DetaliiDocument/162616), art. 3 (procedura de obținere) și art. 8-9 (carnetul de comercializare)
- **De ce contează:** Legea prevede explicit că atestatul se eliberează **pe baza datelor din registrul agricol** (art. 3 alin. 3) și că e valabil 5 ani, cu o filă distinctă pentru fiecare an completată cu datele curente din registru. Practic, RegistrulAgricol e sursa de adevăr legală pentru acest document — deci automatizarea generării lui e o extensie naturală, nu o funcționalitate adăugată artificial.
- **Ce face:** Primăriile sunt obligate să elibereze atestatul exclusiv după verificarea că solicitantul are înregistrate suprafețele cultivate și animalele declarate în registrul agricol. Refuzul eliberării trebuie motivat în scris (art. 9 alin. 2), deci sistemul ar trebui să poată justifica automat de ce o cerere nu poate fi aprobată (ex. nu există culturi active în anul curent).
- **Flow propus:** Funcționarul selectează o gospodărie → sistemul validează automat existența culturilor/animalelor active în anul curent → generare PDF cu antet UAT și date pre-completate, cu semnătura/ștampila primarului aplicată electronic sau lăsată pentru semnare fizică
- **Efort:** Mediu — 3-4 zile
- **Dependențe:** `Gospodarie`, `Persoana`, `CulturaParcela`, `AnimalIndividual`, `EfectivGrup`
- **Riscuri:** Modelul oficial al atestatului e stabilit prin ordin comun al ministerelor — merită verificat dacă există un template standard obligatoriu (câmpuri, aspect) sau dacă primăriile au libertate de formatare
- **Criterii de acceptare:** Buton "Generează Atestat" în pagina gospodăriei → PDF descărcabil, cu validare care blochează generarea dacă nu există date active, și mesaj explicit de motiv atunci când blochează

### 2.2 Export date pentru sincronizare cu Registrul Agricol Național (RAN)

- **Sursă legală:** [OG 28/2008 privind registrul agricol](https://legislatie.just.ro/Public/DetaliiDocument/187493), art. 6 alin. 5: registrul agricol se întocmește în format electronic "cu obligația de a se interconecta cu Registrul agricol național (RAN), în vederea raportării unitare către instituțiile interesate"
- **De ce contează:** Aceasta e singura propunere care ține de interoperabilitate cu un sistem extern guvernamental, nu doar de funcționalitate internă — motiv pentru care riscul tehnic e mult mai mare decât la celelalte propuneri.
- **Ce face:** Primăriile au obligația legală de transmitere a datelor către sistemul central RAN. În practică, majoritatea aplicațiilor comerciale de registru agricol (ex. RegistrulAgricol.RO) susțin deja acest export, deci există un precedent tehnic de urmat, chiar dacă schema exactă nu e public documentată în detaliu.
- **Efort:** Mare — 7-10 zile
- **Dependențe:** Întregul model de date
- **Riscuri:** Necesită schema XSD/format exact cerută de RAN — de identificat dacă există documentație publică (verificați pe site-ul ANCPI sau prin cerere directă) sau dacă e nevoie de contact direct cu instituția. **Recomandare: nu porniți implementarea până nu confirmați formatul exact**, altfel riscați rescrieri majore.
- **Criterii de acceptare:** Export valid conform schemei confirmate, testat cu un validator XML/JSON corespunzător

### 2.3 Registru GDPR de auditare a accesului la date personale

- **Sursă legală:** [Regulamentul (UE) 2016/679 (GDPR)](https://eur-lex.europa.eu/legal-content/RO/TXT/?uri=celex%3A32016R0679), în special art. 5 (principii de prelucrare, inclusiv responsabilitate/accountability) și art. 32 (securitatea prelucrării). De menționat că normele tehnice recente pentru registrul agricol (Norma din 17.02.2025) reamintesc explicit că toate entitățile care completează registrul "prelucrează date cu caracter personal cu respectarea prevederilor Regulamentului (UE) nr. 2016/679"
- **Ce face:** Orice vizualizare/modificare a datelor unui cetățean (CNP, adrese, structură familie, proprietate) trebuie auditată — nu doar modificările, ci și simplele accesări, pentru a putea demonstra conformitate în caz de plângere sau control
- **Efort:** Mediu — 3-4 zile
- **Entități noi:** `GdprAuditLog` (timestamp, utilizator backend, tip acțiune, ID persoană vizată, motiv acces/modificare)
- **Dependențe:** `SecurityContextHolder`, Spring AOP
- **Riscuri:** Logarea fiecărui acces poate genera un volum mare de date — merită gândită de la început o politică de retenție/arhivare, nu doar mecanismul de scriere
- **Criterii de acceptare:** Fiecare acces la o pagină/endpoint cu date personale ale unei persoane fizice generează automat o intrare în `GdprAuditLog`, fără intervenție manuală a dezvoltatorului la fiecare endpoint (implementare via aspect/interceptor)

---

## 3. Funcționalități pentru UAT-uri / administratori

### 3.1 Modul mobil de inspecție și validare în teren (GPS & Stereo70)

- **Sursă legală:** [Legea 145/2014](https://legislatie.just.ro/Public/DetaliiDocument/162616), art. 8 alin. 3 — carnetul de comercializare se eliberează în 5 zile lucrătoare, "după verificarea existenței atestatului de producător și după verificarea faptică în teren a existenței produsului/produselor supus/supuse comercializării"
- **De ce contează:** Termenul de 5 zile e strâns, iar verificarea faptică e obligatorie prin lege, nu opțională — dacă inspectorul trebuie să se întoarcă la birou pentru a nota rezultatul verificării, procesul devine mai lent decât e nevoie. Un flux mobil elimină acest du-te-vino.
- **Ce face:** Inspectorul deschide harta parcelei (aveți deja componenta Leaflet și coordonatele geometrice în DB), validează vizual cultura la fața locului, încarcă o fotografie geo-etichetată ca dovadă a verificării
- **Efort:** Mediu-Mare — 5-7 zile
- **Dependențe:** `Parcela`, `Teren`, `Document`
- **Riscuri:** Necesită UI responsive/mobile-first pentru acest flux specific — verificați dacă frontend-ul Angular actual e testat pe mobil sau dacă e nevoie de o pagină dedicată simplificată, separată de restul aplicației de administrare
- **Criterii de acceptare:** Din telefon/tabletă, inspectorul poate deschide o parcelă pe hartă, atașa o fotografie cu geolocație și marca inspecția ca "validată", cu timestamp înregistrat automat

### 3.2 Gestiunea campaniilor de declarare și notificare (somații)

- **Sursă legală:** [OG 28/2008 privind registrul agricol](https://legislatie.just.ro/Public/DetaliiDocument/187493), art. 11 — perioadele fixe de declarare a modificărilor. Notele tehnice mai recente precizează că, dacă persoanele nu declară la termen, "se consideră că nu au intervenit niciun fel de modificări", iar datele din anul precedent se reportează din oficiu, cu mențiunea "report din oficiu"
- **De ce contează:** Legea are deja un mecanism de rezervă (report automat) pentru gospodăriile care nu se actualizează — dar asta nu înseamnă că primăria nu trebuie să încerce activ obținerea datelor curente. O gospodărie cu date reportate ani la rând poate ascunde schimbări reale (construcții noi, schimbări de folosință) care afectează impozitele.
- **Ce face:** Ecran cu procent de actualizare pe sate/străzi, care arată vizual unde e nevoie de intervenție, plus generare scrisori de somație PDF în masă pentru gospodăriile neactualizate în perioada legală
- **Efort:** Mediu — 3-4 zile
- **Dependențe:** `Gospodarie`, `Persoana`
- **Riscuri:** De clarificat în echipă dacă somația trebuie doar generată (PDF) sau dacă există și o cerință de transmitere/confirmare de primire, ceea ce ar extinde scopul
- **Criterii de acceptare:** Dashboard cu procent actualizare per sat + export PDF în masă pentru lista de gospodării restante

---

## 4. Funcționalități pentru cetățeni / gospodării

### 4.1 Portal cetățean — cereri și declarații online (self-service)

- **Sursă legală:** [OG 28/2008 privind registrul agricol](https://legislatie.just.ro/Public/DetaliiDocument/187493), art. 11 (obligația de declarare a modificărilor de către cetățean, în anumite perioade fixe)
- **De ce contează:** Legea cere ca cetățeanul să declare modificările, dar nu impune ca declararea să se facă fizic la ghișeu — un portal online e o interpretare validă a digitalizării acestui proces, aliniată și cu direcția generală a administrației publice românești către servicii online.
- **Ce face:** Cetățenii se autentifică, sunt asociați cu CNP-ul lor în baza de date a tenant-ului (UAT-ul selectat), trimit propuneri de modificare care apar ca "Draft" în coada de aprobare a funcționarului — nimic nu se modifică direct în registrul oficial fără validare umană
- **Efort:** Mare — 7-10 zile
- **Dependențe:** `Persoana`, `Gospodarie`, `User`
- **Riscuri:** Cel mai mare efort din tot roadmap-ul — implică autentificare publică separată de cea a funcționarilor (posibil integrare cu un sistem de identitate electronică, dacă se dorește nivel ridicat de asigurare), plus un flux complet de aprobare/respingere cu notificări. Recomandat spre finalul internship-ului, ca "stretch goal", nu ca prioritate.
- **Criterii de acceptare:** Cetățean autentificat poate trimite o modificare, funcționarul o vede în coadă și o poate aproba/respinge, cu notificare către cetățean asupra rezultatului

### 4.2 Solicitare și eliberare adeverințe semnate electronic (ex. adeverință APIA)

- **Sursă legală:** Normele APIA (Agenția de Plăți și Intervenție pentru Agricultură) pentru acordarea subvențiilor directe pe suprafață/cap de animal — merită verificată versiunea curentă a normelor de campanie pe site-ul APIA înainte de implementare, deoarece cerințele de formular se pot schimba anual
- **De ce contează:** Fermierii au nevoie recurent, o dată sau de mai multe ori pe an, de o adeverință oficială care confirmă suprafețele/efectivele înregistrate, pentru a putea depune cereri de subvenție. În prezent acest proces implică o vizită la primărie și completare manuală.
- **Ce face:** Cetățeanul solicită adeverința (din portal, dacă există propunerea 4.1, sau printr-o cerere clasică la funcționar), sistemul o generează automat pe baza datelor din anul agricol curent
- **Efort:** Mediu — 3-4 zile
- **Dependențe:** `Document`, `Teren`, `CulturaParcela`
- **Riscuri:** Semnătura electronică propriu-zisă (calificată, conform eIDAS) e complexă din punct de vedere legal/tehnic și necesită de obicei un certificat digital al primarului — pentru internship, o versiune fără semnătură calificată (doar generare PDF + parcurs de aprobare internă/semnare fizică ulterioară) e suficientă ca demo funcțional
- **Criterii de acceptare:** Cetățean/funcționar generează adeverința pe baza datelor din anul agricol curent, disponibilă ca PDF descărcabil

---

## 5. Securitate / compliance

### 5.1 Separarea granulară a rolurilor (RBAC) în controllere și interfață

- **De ce:** Code review-ul arată că `ROLE_USER` poate face modificări distructive; `CladireController` și `CulturaParcelaController` nu aveau deloc `@PreAuthorize`, ceea ce înseamnă că orice utilizator autentificat, indiferent de rol, putea apela aceste endpoint-uri
- **Ce face:** Introduce roluri diferențiate în loc de un singur nivel de acces: `REGISTRU_READ` (vizualizare, potrivit pentru cetățeni în viitorul portal self-service), `REGISTRU_OPERATOR` (funcționari cu drept de scriere pe date curente), `REGISTRU_ADMIN` (setări, nomenclatoare, import-uri, cote de taxe). Practic e extinderea logică a distincției super-admin / tenant-admin / user pe care ați construit-o deja, dar aplicată consecvent la nivel de controller, nu doar de UI.
- **Efort:** Mic-Mediu — 2-3 zile
- **Dependențe:** `SecurityConfig.java`, Angular Route Guards
- **Riscuri:** Ascunderea unui buton/rută în Angular nu e o măsură de securitate — trebuie verificat separat că fiecare endpoint respins de UI e respins și de backend, altfel rolurile sunt doar cosmetice
- **Criterii de acceptare:** Fiecare endpoint sensibil are `@PreAuthorize` explicit testat (inclusiv printr-un test de integrare care încearcă acces neautorizat și verifică 403), iar rutele Angular respectă aceleași roluri fără a fi singura linie de apărare

### 5.2 Istoric de audit detaliat pentru entități (Hibernate Envers)

- **De ce:** Datele din registrul agricol pot ajunge să fie folosite ca probă în litigii de proprietate sau în verificarea eligibilității pentru fonduri APIA — dacă cineva modifică o suprafață de teren sau categoria de folosință și apoi apare o dispută, e esențial să existe un istoric complet și de încredere (cine a modificat, când, ce anume s-a schimbat), nu doar valoarea curentă
- **Ce face:** Hibernate Envers ține automat un istoric al fiecărei versiuni a unei entități, fără să fie nevoie de o implementare manuală a unui sistem de audit — se adaugă doar adnotarea `@Audited` pe entitățile relevante, iar framework-ul se ocupă de restul
- **Efort:** Mic-Mediu — 2-3 zile (configurare `@Audited` + testarea interogării istoricului)
- **Dependențe:** JPA/Hibernate
- **Riscuri:** Volumul de date crește (fiecare modificare generează un rând nou de istoric) — merită estimat impactul pe termen lung asupra dimensiunii bazei de date, mai ales dacă se aplică pe multe entități deodată
- **Criterii de acceptare:** Orice modificare pe `Parcela`, `Cladire`, `Gospodarie` are un istoric de versiuni interogabil (cine a modificat, când, ce câmpuri s-au schimbat față de versiunea anterioară)

### 5.3 Criptarea CNP/CUI la nivel de bază de date

- **De ce:** CNP-ul (persoane fizice) și CUI-ul (persoane juridice) sunt identificatori unici cu sensibilitate maximă din perspectivă GDPR — spre deosebire de propunerea 2.3 (care auditează *cine* accesează datele), această propunere protejează datele *în repaus*, astfel încât chiar și un acces neautorizat direct la baza de date (ex. un backup furat, un acces SQL neautorizat) să nu expună CNP-urile în clar
- **Ce face:** Criptare AES-256 la nivel de coloană, aplicată transparent prin JPA `AttributeConverter` (`@Convert(converter = CnpEncryptor.class)`), astfel încât restul codului continuă să lucreze cu valori decriptate în memorie, fără să fie nevoie de modificări extinse în logica de business
- **Efort:** Mediu — 3-4 zile (implementare converter + migrare date existente + gestionarea cheii de criptare)
- **Dependențe:** Toate entitățile care stochează CNP/CUI (`Persoana`, `PersoanaJuridica` etc.)
- **Riscuri:**
  - Coloanele criptate nu mai pot fi căutate direct prin `WHERE cnp = ?` fără o strategie suplimentară (ex. un hash determinist separat, indexat, folosit doar pentru căutare exactă) — de clarificat în echipă dacă căutarea după CNP e o funcționalitate curentă a aplicației
  - Gestionarea cheii de criptare (unde se stochează, cum se rotește) trebuie discutată separat — nu poate fi hardcodată în cod sau în fișierul de configurare din Git
- **Criterii de acceptare:** CNP-urile și CUI-urile sunt stocate criptat în baza de date (verificabil printr-o interogare SQL directă), dar aplicația funcționează normal din perspectiva utilizatorului

### 5.4 Soft delete în loc de hard delete

- **De ce:** Ștergerea fizică (`DELETE`) a unui rând din `Parcela`, `Gospodarie` sau alte entități rupe istoricul administrativ și orice referințe din audit log-uri (inclusiv cel din 5.2/2.3) — un rând șters dispare din istoricul de modificări, ceea ce contrazice scopul auditării
- **Ce face:** Marcarea rândurilor ca șterse (`deleted = true`) în loc de eliminarea lor fizică, prin adnotările Hibernate `@SQLDelete` (intercepteaza operația de delete și o transformă într-un update) și `@Where(clause = "deleted = false")` (exclude automat rândurile șterse din interogările normale)
- **Efort:** Mic-Mediu — 2-3 zile
- **Dependențe:** Entitățile principale (`Parcela`, `Gospodarie`, `Cladire`, `AnimalIndividual` etc.)
- **Riscuri:** Interoghează cu atenție relațiile — un `Gospodarie` soft-deleted nu trebuie să mai apară în liste, dar rapoartele istorice/auditul trebuie să poată accesa în continuare date despre el dacă e nevoie
- **Criterii de acceptare:** Ștergerea unei entități prin UI o marchează ca inactivă fără să o elimine din baza de date; interogările standard nu mai returnează entitățile șterse, dar istoricul rămâne accesibil pentru audit

---

## 6. Rapoarte și analytics

### 6.1 Centralizatoare statistice anuale pentru DAJ și INS

- **Sursă legală:** HG 290/2020 sau normele tehnice curente pentru completarea registrului agricol — **recomandare: verificați varianta actualizată a acestei hotărâri (sau eventualul act care a înlocuit-o) direct pe legislatie.just.ro înainte de implementare**, deoarece formularele și structura de raportare se pot schimba
- **De ce contează:** Primăriile trebuie să trimită periodic date centralizate către Direcția Agricolă Județeană (DAJ) și către Institutul Național de Statistică (INS) — în prezent, aceste centralizări se fac probabil manual, prin agregarea datelor din registrul pe hârtie sau din exporturi ad-hoc, ceea ce e predispus la erori și consumă timp la fiecare termen de raportare
- **Ce face:** Rapoarte agregate — de exemplu total capete bovine pe categorii vârstă/sex, total hectare cultivate pe specii de culturi — generate automat din datele curente și exportate în Excel/CSV
- **Efort:** Mediu — 3-5 zile
- **Dependențe:** `CulturaParcela`, `AnimalIndividual`, `EfectivGrup`, `Machinery`
- **Riscuri:** Structura exactă a rapoartelor cerute de DAJ/INS poate diferi de la un formular la altul — merită clarificat în echipă dacă există un format standard sau dacă primăria are un template propriu
- **Criterii de acceptare:** Export Excel/CSV cu structura cerută de rapoartele DAJ/INS, generat automat din datele curente, fără intervenție manuală de recalculare

---

## 7. Calitate cod / testare

### 7.1 Refactorizare relații EAGER și expunere DTO

- **De ce:** Entitățile folosesc `@Data` (Lombok) și relații `EAGER`, ceea ce aduce două riscuri: `LazyInitializationException` atunci când o relație lazy e accesată în afara sesiunii Hibernate, și bucle de serializare infinită atunci când două entități se referă reciproc (ex. `Gospodarie` → `Parcela` → `Gospodarie`) și Jackson încearcă să le serializeze fără protecție explicită
- **Ce face:** Introduce un strat de DTO-uri explicite între entitățile JPA și răspunsurile API, astfel încât fiecare endpoint returnează exact câmpurile necesare, în forma potrivită, fără să expună direct structura internă a bazei de date sau să declanșeze încărcări lazy neașteptate
- **Efort:** Mediu — 3-4 zile
- **Dependențe:** Tot modelul JPA existent
- **Riscuri:** Refactorizarea trebuie făcută incremental, controller câte controller, ca să nu riște să blocheze alte funcționalități în curs de dezvoltare — nu e nevoie de un "big bang" pe tot backend-ul deodată
- **Criterii de acceptare:** Toate endpoint-urile returnează DTO-uri explicite (nu entități direct), relațiile lazy sunt încărcate explicit unde e nevoie, iar un test de integrare confirmă absența buclelor de serializare și a `LazyInitializationException` pe fluxurile principale

### 7.2 Validarea suprapunerii contractelor active pe aceeași parcelă

- **De ce:** Momentan aplicația permite înregistrarea a două contracte de arendă/concesiune active simultan pe aceeași parcelă, pentru aceeași perioadă — o eroare de integritate a datelor care poate crea confuzii legale reale (cine are dreptul de folosință pe acea perioadă?), mai ales relevant dat fiind că `ContractUtilizare` e deja un modul complet implementat (vezi 3.2 din analiza pe capitole, Cap. XV)
- **Ce face:** O validare (la nivel de serviciu, sau constrângere la nivel de bază de date) care verifică, la crearea/editarea unui contract, dacă parcela respectivă are deja un alt contract activ care se suprapune pe intervalul de date propus
- **Efort:** Mic — 1-2 zile
- **Dependențe:** `ContractUtilizare`, `Parcela`
- **Riscuri:** Definirea exactă a "suprapunere" trebuie clarificată — contracte pe același interval mereu se interzic, dar contracte pe intervale adiacente (unul se termină exact când începe celălalt) ar trebui permise
- **Criterii de acceptare:** Sistemul respinge cu un mesaj clar orice încercare de a crea un contract care se suprapune temporal cu unul activ existent pe aceeași parcelă

---

## Plan pe faze (recomandare de ordine)

### Faza 1 — Fundație solidă (≈1-1.5 săptămâni)
**Obiectiv:** bază stabilă și securizată înainte de funcționalități noi de business.

1. **5.1** Separare granulară roluri (RBAC) — 2-3 zile
2. **7.1** Refactorizare EAGER/DTO — 3-4 zile
3. **7.2** Validare suprapunere contracte — 1-2 zile *(rapid, se poate strecura oricând în această fază)*

> Aceste trei rezolvă restul de probleme din code review și arată rigoare tehnică — merită să fie *vizibil* prima livrare.

### Faza 2 — Impact vizual rapid, valoare de business (≈1-2 săptămâni)
**Obiectiv:** funcționalități noi, demonstrabile, cu bază legală clară.

4. **1.1** Calcul impozit pe proprietate (clădiri + terenuri) — 4-5 zile
5. **2.1** Generare atestate de producător (PDF) — 3-4 zile
6. **1.2** Evidență utilaje scutite — 1-2 zile *(rapid, se poate strecura oricând)*

### Faza 3 — Extindere conformitate și operare (≈1.5-2 săptămâni, opțional în funcție de timp)

7. **5.2** Audit istoric (Hibernate Envers) — 2-3 zile
8. **5.3** Criptare CNP/CUI la nivel de bază de date — 3-4 zile
9. **3.2** Campanii de notificare/somații — 3-4 zile
10. **6.1** Centralizatoare statistice DAJ/INS — 3-5 zile
11. **2.3** Registru GDPR audit acces — 3-4 zile
12. **5.4** Soft delete pentru entitățile principale — 2-3 zile

### Faza 4 — Extindere date conform HG 1627/2024 (opțional, dacă rămâne timp)

13. **Cap. II** — Număr cadastral, CF, flag Intravilan/Extravilan, titular drept folosință — extindere `Parcela`/`Teren`
14. **Cap. IX** — Statut proprietate + clase de putere pe `Machinery`
15. **Cap. XI** — Materiale construcție + racorduri utilități pe `Cladire`

> Capitolele complet lipsă (V, VIII, X, XIII, XIV din analiza de conformitate) sunt efort mare fiecare și probabil depășesc scopul realist al unui internship — merită discutate ca goluri cunoscute, nu neapărat implementate acum.

### Faza 5 — Stretch goals (dacă rămâne timp, spre final)

16. **1.3** Estimator impozit pe venit agricol — 5-7 zile
17. **3.1** Inspecție mobilă în teren — 5-7 zile
18. **4.2** Adeverințe electronice — 3-4 zile
19. **2.2** Export RAN — 7-10 zile *(riscant fără confirmare schemă/format exact)*
20. **4.1** Portal cetățean self-service — 7-10 zile *(cel mai mare efort, cel mai potrivit ca demo final sau proiect separat)*

---

## Note pentru discuție

- Propunerile **1.1, 2.1, 5.1** au raportul cel mai bun impact/efort și sunt cele mai potrivite de prezentat ca "următorii pași".
- **Tabelul de conformitate din secțiunea 0** e util ca argument documentat pentru orice discuție despre ce mai rămâne de acoperit legal — arată clar diferența dintre "funcționalitate în plus" și "cerință legală neacoperită încă".
- **2.2 (export RAN)** nu ar trebui început fără confirmarea formatului exact — riscă să consume mult timp fără rezultat garantat.
- **4.1 (portal cetățean)** e suficient de mare încât ar putea deveni un proiect separat sau o extensie de internship, nu o simplă "funcționalitate în plus".
- **5.3 (criptare CNP/CUI)** are o dependență importantă de clarificat înainte de start: dacă aplicația are nevoie să caute cetățeni după CNP, criptarea simplă pe coloană nu permite căutare directă — soluția (hash determinist separat pentru căutare) trebuie aleasă înainte de implementare, nu după.
- Pentru propunerile cu sursă legală, articolele citate trebuie verificate în forma actualizată a legii (Codul Fiscal se modifică frecvent prin OUG-uri) înainte de implementare finală.
