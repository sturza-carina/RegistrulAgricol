# Research: Modul taxe/impozite în RegistrulAgricol

Document de research pentru evaluarea posibilității de a adăuga calculul impozitului anual per gospodărie/persoană, agregat pe toate entitățile legate de gospodărie din registrul agricol: teren, clădiri, utilaje/mijloace de transport, animale. Include și actualizarea valorilor prin import de fișiere oficiale.

**Concluzie cheie, înainte de detalii**: nu există un singur "impozit" care acoperă toate cele patru categorii. În realitate sunt **două regimuri fiscale complet diferite**:

1. **Impozite locale pe proprietate** (teren, clădiri, mijloace de transport înmatriculate) — bugetul local al UAT, stabilite prin HCL anual, bază legală Codul Fiscal Titlul IX.
2. **Impozitul pe venitul din activități agricole** (animale, anumite culturi vegetale peste anumite praguri) — bugetul de stat, stabilit pe bază de "normă de venit" pe cap de animal/hectar, publicată anual pe județe de ANAF, bază legală Codul Fiscal Titlul IV, Cap. VII (art. 103-107).

Utilajele/echipamentele agricole (tractoare neînmatriculate, instalații, agregate) **nu au, în general, un impozit propriu** — doar mijloacele de transport înmatriculate (tractoare/remorci înmatriculate) intră la impozitul pe mijloace de transport. Detaliat mai jos la secțiunea 1.4.

---

## 1. Cadru legal

Sursa primară: **Legea nr. 227/2015 privind Codul Fiscal**, Titlul IX "Impozite și taxe locale", Capitolele II (clădiri) și III (teren), plus **Normele metodologice de aplicare**. Valorile de bază pentru 2026 au fost actualizate prin **Legea 239/2025** (MO nr. 1160/15.12.2025).

Nivelul concret de impozit aplicabil într-o localitate se stabilește anual prin **Hotărâre a Consiliului Local (HCL)**

### 1.1. Impozitul pe teren (art. 463-467 Cod Fiscal)

Reguli de bază:
- Impozitul se stabilește anual în funcție de **suprafață, rangul localității, zona și categoria de folosință a terenului**, conform încadrării făcute de consiliul local.
- **Intravilan, categoria "curți-construcții"** sau orice altă categorie pe primii 400 m²: suprafață (ha) × sumă fixă din tabelul Codului Fiscal pe categorie.
- **Intravilan, altă categorie de folosință, peste 400 m²**: suprafață (ha) × sumă din tabel × coeficient de corecție pe rang/zonă.
- **Extravilan** (arabil, pășune, fânețe, vie, livadă, pădure): suprafață (ha) × sumă din tabel × coeficient de corecție (între 0,90 și 2,60, în funcție de rangul localității și zonă, conform art. 457 alin. 6).
- Pentru **persoane juridice**, terenul intravilan-altă categorie se impozitează ca extravilan *doar* dacă firma are agricultura ca obiect de activitate înregistrat și venituri/cheltuieli aferente în contabilitate.
- **Majorări speciale**: consiliul local poate majora impozitul cu până la 500% pentru terenuri neîngrijite (intravilan) sau pentru teren agricol nelucrat 2 ani consecutivi (începând cu al treilea an).
- **Bonificație** de până la 10% pentru plata integrală până la 31 martie.
- Plată în 2 rate egale, termene tipice 31 martie / 30 septembrie.
- Categoriile de folosință se preiau din **registrul agricol** — exact sursa de date pe care o aveți deja în aplicație.
- Diferențele dintre suprafața din actele de proprietate și cea din măsurători cadastrale: impozitul se recalculează conform suprafeței reale de la 1 ianuarie a anului următor înregistrării.

Context 2026: creșteri semnificative ale bazei de impozitare pentru terenuri agricole (ex. categoria A de la 28 lei/ha la 75 lei/ha), introduse prin pachetul fiscal — relevant pentru testarea modulului de import, fiindcă arată cât de mult se pot schimba valorile de la un an la altul.

### 1.2. Impozitul pe clădiri (art. 453-461 Cod Fiscal)

- **Persoane fizice**: cotă minimă 0,1% din valoarea impozabilă (stabilită prin HCL, de obicei 0,08%-0,2%), aplicată asupra valorii impozabile determinate pe baza suprafeței construite desfășurate × valoare pe m² din tabel × coeficient de corecție rang/zonă.
- Reduceri pe vechime: -15% pentru clădiri 50-100 ani, -25% pentru clădiri peste 100 ani.
- **Persoane juridice**: 0,08%-0,2% pentru rezidențiale, 0,2%-1,3% pentru nerezidențiale, **0,4% pentru clădiri nerezidențiale folosite în activități agricole** (cotă specială, relevant pentru voi), 5% dacă valoarea impozabilă nu a fost actualizată în ultimii 5 ani.
- Clădiri cu destinație mixtă: impozitul se calculează separat pe suprafața rezidențială și cea nerezidențială, apoi se însumează.
- Majorare 500% pentru clădiri neîngrijite (an 2026, conform art. 489 alin. 5-6).
- Plată în 2 rate (31 martie / 30 septembrie), bonificație până la 10% la plata integrală anticipată.

### 1.3. Mijloace de transport / utilaje agricole

- **Impozitul pe mijloacele de transport** (art. 468-470 Cod Fiscal) se aplică doar mijloacelor de transport **înmatriculate/înregistrate** (tractoare, remorci, autoutilitare), calculat pe capacitate cilindrică (lei/200 cm³) sau, după caz, pe masa totală maximă autorizată pentru remorci/semiremorci. Cota e stabilită tot prin HCL anual, similar terenului/clădirilor.
- **Scutire**: mijloacele de transport agricole (tractoare, combine etc.) utilizate efectiv în activități agricole sunt scutite de acest impozit în anumite condiții prevăzute de Codul Fiscal — trebuie verificat punctual articolul de scutire (art. 469) și dacă se cere o declarație/dovadă a utilizării agricole.
- **Utilajele și echipamentele neînmatriculate** (instalații de irigat, agregate, echipamente fixe) **nu au un impozit propriu în Codul Fiscal**. Ele apar însă în **registrul agricol național (RAN)** ca o categorie de "bunuri" alături de teren, animale, clădiri — Norma tehnică de completare a registrului agricol include explicit echipamentele, utilajele şi instalaţiile/agregatele pentru agricultură şi silvicultură, mijloacele de transport cu tracţiune animală şi mecanică ca bunuri înregistrate, dar înregistrarea lor are scop statistic/evidență, nu fiscal direct.
- Concluzie practică: dacă vreți să le includeți în modul, ele intră mai degrabă la o secțiune de **evidență/inventar** decât la "impozit calculat", cu excepția mijloacelor de transport înmatriculate care chiar au impozit.

### 1.4. Impozitul pe venitul din activități agricole — animale (și anumite culturi)

Bază legală: Codul Fiscal, Titlul IV, Cap. VII, art. 103-107 (venituri din activități agricole, silvicultură și piscicultură). Regim complet diferit față de impozitul pe proprietate:

- **Plătit la bugetul de stat** (nu local), pe baza unei **declarații unice** depuse de contribuabil până la 25 mai pentru anul în curs.
- Baza de calcul este o **normă anuală de venit pe cap de animal / familie de albine / hectar cultivat**, stabilită anual **pe județe** de direcțiile generale ale finanțelor publice, pe baza metodologiei Guvernului (HG 30/2019). Nu există un tabel național unic — valorile diferă de la un județ la altul, analog cu HCL-urile locale pentru teren/clădiri.
- Impozitul anual datorat se calculează prin aplicarea unei cote de 16% asupra venitului anual din activități agricole stabilit pe baza normei anuale de venit, plătit în două rate egale: 50% până la 25 octombrie, 50% până la 15 decembrie.
- **Praguri de scutire** (sub acest număr de capete nu se datorează impozit) — cifrele circulă diferit pe surse, deci trebuie verificate exact pe normele actuale, dar ordinul de mărime e: câteva capete de bovine, câteva zeci de caprine/ovine, câteva capete de porcine, ~100 de păsări. Există și scutire totală pentru persoane cu handicap grav/accentuat.
- Pentru sectorul vegetal există un mecanism similar (normă de venit pe hectar pe cultură), cu praguri de suprafață minimă scutită pe tip de cultură.
- Acest impozit **nu trece prin registrul agricol al primăriei** în sensul calculului (deși datele despre animale/terenuri din registrul agricol pot fi folosite ca sursă de evidență) — el e administrat de ANAF, nu de compartimentul de taxe și impozite locale.

**Implicație importantă pentru voi**: dacă includeți animalele în modulul de "calcul impozit", trebuie tratate separat de teren/clădiri — alt buget, altă autoritate, altă sursă de valori (norme ANAF pe județ, nu HCL pe UAT), alt termen de plată, altă bază de calcul (venit estimat, nu valoare a bunului).

### 1.5. Alte taxe locale relevante pentru zona agricolă

- **Taxa pe teren** (vs. impozit) — se aplică pentru terenuri publice concesionate/închiriate/în folosință, calculată similar cu impozitul.
- Scutiri generale: terenuri/clădiri ale cultelor religioase, cimitire, unități de învățământ și sanitare, anumite categorii prevăzute la art. 464 / art. 456.

### 1.6. Cine datorează impozitul

Persoana care are dreptul de proprietate la **31 decembrie a anului fiscal precedent** — important pentru logica de calcul per "stare la 31 decembrie anul N-1, impozit datorat în anul N".

---

## 2. Structura tipică a "fișierului oficial" (anexa HCL)

Studiind anexele publicate de primării (ex. emol.ro, primariarupea.ro), formatul standard conține:

1. **Tabel impozit clădiri** — cote pe categorie (persoane fizice / juridice, rezidențial / nerezidențial), valoare impozabilă lei/m², coeficienți de corecție pe rang/zonă, reduceri pe vechime.
2. **Tabel impozit teren intravilan** — pe categorie de folosință (curți-construcții și restul), rang, zonă.
3. **Tabel impozit teren extravilan** — pe categorie de folosință (arabil, pășune, fânețe, vie, livadă, pădure), cu coeficient de corecție.
4. **Tabel impozit mijloace de transport** — pe capacitate cilindrică / tip vehicul.
5. **Bonificații și termene de plată.**
6. **Majorări speciale** (terenuri/clădiri neîngrijite, teren nelucrat).

**Problema practică principală**: formatul fișierului diferă de la o primărie la alta (PDF cu tabel, uneori DOC/DOCX, rar Excel structurat) — nu există un standard național de schimb de date pentru aceste anexe. Nu am găsit (și probabil nu există) un API oficial central care expune aceste valori per UAT; ele sunt publicate individual pe site-urile primăriilor sau platforme precum emol.ro.

---

## 3. Implicații pentru arhitectura aplicației

### 3.1. Separarea pe niveluri a datelor de impozitare

1. **Nivel legal (Codul Fiscal)** — structura formulelor, categoriile de folosință, intervalele min-max ale cotelor și coeficienților. Se schimbă rar (anual, prin lege de modificare a Codului Fiscal sau OUG).
2. **Nivel local (HCL per UAT)** — pentru teren, clădiri, mijloace de transport înmatriculate: valorile concrete aplicate într-un UAT pentru un an fiscal dat. Se schimbă anual, publicat de obicei în decembrie pentru anul următor.
3. **Nivel județean (norme ANAF per județ)** — pentru animale și culturi vegetale supuse normei de venit: valorile concrete diferă pe județ, nu pe UAT, publicate separat de ANAF.
4. **Nivel asset** — datele efective de teren/clădire/mijloc de transport/animal din registrul agricol al gospodăriei.

Deoarece (2) și (3) sunt regimuri fiscale diferite (local vs. de stat), recomand să le tratați ca **două module distincte** care produc fiecare un total separat, agregate doar la nivel de raportare ("impozit total estimat al gospodăriei"), nu combinate într-o singură formulă.

### 3.2. Schema de date propusă (schiță conceptuală)

**Pentru impozitele locale (teren, clădiri, mijloace de transport):**
- `tax_rate_table` — versionat pe `(uat_id, fiscal_year, asset_type, land_use_category, zone, locality_rank)` → `base_value`, `unit` (lei/ha sau lei/m²).
- `correction_coefficient` — pe `(locality_rank, zone)`, sau preluat direct din HCL dacă UAT-ul are valori proprii.
- `building_age_reduction` — reguli fixe din Codul Fiscal (50-100 ani / >100 ani).
- `vehicle_tax_table` — pe `(uat_id, fiscal_year, vehicle_type, engine_capacity_bracket)`.

**Pentru impozitul pe venitul agricol (animale, culturi pe normă de venit):**
- `agricultural_income_norm` — versionat pe `(judet_id, fiscal_year, animal_category sau crop_category)` → `norma_venit`, `unit` (lei/cap sau lei/ha), `prag_scutire` (numărul de capete/ha neimpozabile).
- Calculul aici e per persoană (nu per asset individual): se însumează capetele de animale pe categorie, se scade pragul de scutire, se aplică norma de venit, apoi 16%.

**Comun:**
- `tax_import_batch` — metadate despre fiecare fișier oficial încărcat: sursă (UAT sau județ), an fiscal, dată upload, status (în validare / activ / arhivat), utilizator care a încărcat.
- Calculul efectiv **nu suprascrie** anul precedent — adaugă rânduri noi per an, păstrând istoricul pentru audit.

### 3.3. Serviciul de calcul

Recomand două servicii separate, agregate la final:

**`PropertyTaxCalculationService`** (teren + clădiri + mijloace de transport înmatriculate):
1. Identifică assets-urile din registrul agricol al gospodăriei, cu UAT-ul lor.
2. Pentru fiecare, preia tabelul de valori activ pentru `(uat, an_fiscal)`.
3. Aplică formula corespunzătoare (intravilan/extravilan, prag 400m², rezidențial/nerezidențial, persoană fizică/juridică, capacitate cilindrică pentru vehicule).
4. Aplică coeficienți de corecție, reduceri (vechime clădire), majorări (teren/clădire neîngrijite, teren nelucrat), verifică scutirile (mijloace agricole utilizate efectiv).
5. Însumează pe UAT (gospodăria poate avea assets în UAT-uri diferite → total separat per UAT, plus total general).

**`AgriculturalIncomeTaxCalculationService`** (animale + culturi pe normă de venit):
1. Identifică efectivele de animale și suprafețele cultivate pe categorii relevante din registrul agricol al gospodăriei.
2. Identifică județul (nu UAT-ul) pentru fiecare.
3. Scade pragul de scutire per categorie.
4. Aplică norma de venit pe județ și an fiscal → venit impozabil.
5. Aplică cota de 16% → impozit datorat la bugetul de stat.

**Pentru utilaje neînmatriculate**: dacă tot vreți să le includeți, propun un modul separat de **inventar/evidență** (fără calcul de impozit, doar listare valoare/stare), clar marcat ca atare în UI, ca să nu se creeze impresia greșită că generează o obligație fiscală.

### 3.4. Modulul de import al fișierelor oficiale

Aici trebuie două fluxuri de import distincte, fiindcă sursele și formatele sunt diferite:

- **Import HCL** (teren/clădiri/vehicule) — per UAT, format variabil (PDF/DOC), cum a fost descris mai jos la 3.4.1.
- **Import norme de venit agricol** (animale/culturi) — per județ, publicate de ANAF/direcțiile județene de finanțe, posibil mai standardizate ca format (verificați site-urile ANAF județene), dar tot fără API public cunoscut.

#### 3.4.1. Opțiuni de import, de la simplu la complex

- **Opțiunea A (recomandată ca prim pas)**: template propriu (Excel/CSV) cu coloane fixe, completat manual de un funcționar plecând de la documentul oficial, apoi încărcat în aplicație. Validare automată la import (interval valori conform Codului Fiscal, completitudine categorii).
- **Opțiunea B**: parsing semi-automat al PDF-ului oficial, cu validare/corecție manuală înainte de activare.
- **Opțiunea C** (termen lung, posibil nerealist azi): integrare cu o sursă centralizată, dacă MFP/ANAF publică vreodată export structurat — nu am identificat în prezent un API public pentru niciuna din cele două categorii.

Indiferent de opțiune, fiecare import ar trebui să treacă printr-un **flux de aprobare** (draft → validat → activ pentru anul fiscal X), nu activare automată directă.

---

## 4. Surse oficiale, cu link direct pe fiecare tip de calcul

**Impozit pe teren (formula de calcul, art. 463-467):**
- https://lege5.ro/Gratuit/g43donzvgi/impozitul-pe-teren-si-taxa-pe-teren-codul-fiscal — art. 463-464, cine datorează, taxă vs. impozit
- https://lege5.ro/Gratuit/g43donzvgi/calculul-impozitului-taxei-pe-teren-codul-fiscal — art. 465, formula exactă (intravilan sub/peste 400 m², extravilan, coeficienți de corecție)
- https://lege5.ro/Gratuit/ha4tomrvge/calculul-impozitului-taxei-pe-teren-norma-metodologica — norme metodologice cu exemple de calcul pas-cu-pas (foarte util pentru testare/validare a formulei implementate)
- https://impozitul.ro/impozit-teren — explicație + calculator orientativ pentru 2026, util ca referință rapidă, nu ca sursă primară

**Impozit pe clădiri (formula de calcul, art. 453-461):**
- https://lege5.ro/Gratuit/g43donzvgi/impozitul-pe-cladiri-si-taxa-pe-cladiri-codul-fiscal — art. 453-456, cine datorează, scutiri
- https://mfinante.gov.ro/apps/legis.html?id=68&pagina=taxe&menu=Impozite — art. 457, formula pentru persoane fizice (suprafață construită desfășurată × valoare/m² × coeficient corecție, reduceri pe vechime)
- https://lege5.ro/Gratuit/g43donzvgi/calculul-impozitului-taxei-pe-cladirile-detinute-de-persoanele-juridice-codul-fiscal — art. 460, formula pentru persoane juridice, inclusiv cota specială 0,4% pentru clădiri agricole nerezidențiale
- https://desteaptate.ro/calculator-impozit-cladiri — context 2026 (Legea 239/2025), valori actualizate, util pentru verificare

**Mijloace de transport (formula de calcul, art. 468-470):** nu am un link dedicat verificat separat în acest research — recomand consultarea directă a art. 468-470 din Codul Fiscal pe lege5.ro sau mfinante.gov.ro înainte de implementare, structura fiind analogă cu teren/clădiri (capacitate cilindrică × sumă din tabel, stabilită prin HCL).

**Impozit pe venitul agricol — animale (formula de calcul, art. 103-107):**
- https://static.anaf.ro/static/10/Brasov/Brasov/Animale.pdf — ghid ANAF, cine datorează, formula (cotă 16% pe venitul stabilit pe normă de venit), termene de plată, scutire pentru persoane cu handicap grav/accentuat
- https://agrointel.ro/74994/ai-mai-mult-de-doua-animale-iata-ce-impozit-platesti-in-2017 — explicație pe înțelesul tuturor + praguri de scutire pe categorie de animal (atenție: date din 2017, **doar pentru orientare**, pragurile trebuie reverificate pe anul curent)
- https://primariacontesti.ro/?p=95 — tabel cu normele de venit pe cultură vegetală (pentru partea de culturi, nu animale) + pașii de calcul

**Registrul agricol ca sursă de date / definiția "bunurilor" gospodăriei:**
- https://legislatie.just.ro/Public/DetaliiDocument/224835 — Norma tehnică de completare a registrului agricol; confirmă explicit că teren, clădiri, animale, echipamente/utilaje și mijloace de transport sunt "bunuri" înregistrate ca atare în registrul agricol


**Anexe HCL concrete (exemple de format al "fișierului oficial" de import), pentru referință de structură — linkuri direct de pe domeniile primăriilor, fără login:**
- https://primariaovidiu.ro/wp-content/uploads/2025/11/Anexe-proiect-hotarare-taxe-si-impozite-2026.pdf — exemplu clar structurat, cu capitole separate pe teren PF, clădiri PF, mijloace de transport, formule explicate articol cu articol
- https://www.primaria-iasi.ro/dm_iasi/hotarari.nsf/89AC8E819E8D33C5C2258D400051F363/$FILE/PROIECT%20HCL%20TAXE%202026.pdf?Open= — proiect HCL Iași, cu toate anexele (clădiri PF/PJ, teren intravilan/extravilan, mijloace transport, taxe speciale, scutiri)
- https://primariasincai.ro/wp-content/uploads/2025/10/Proiect-HCL-stabilire-taxe-si-impozite-locale-2026.pdf — exemplu de comună mică, format mai simplu, util ca referință de structură minimă
- https://poduturcului.ro/continut/fisiere/2025/11/ANEXA-1-TAXE-si-impozite-2026-la-HCL.pdf — anexă structurată pe capitole (III intravilan-construcții, IV intravilan-altă categorie, V extravilan), cu trimiteri exacte la articolele din Codul Fiscal pe fiecare secțiune

**Notă importantă**: paginile lege5.ro afișează fragmente din Codul Fiscal "din 2015" și pot să nu reflecte cea mai recentă modificare legislativă (precizează asta explicit pe pagină) — pentru forma 100% la zi, varianta de bază trebuie verificată pe Monitorul Oficial sau pe mfinante.gov.ro (Ministerul Finanțelor publică text legislativ consolidat oficial).

## 5. Întrebări deschise / de clarificat 

- Scope-ul inițial: doar impozitele locale (teren, clădiri), sau și impozitul pe venitul agricol (animale)? Sunt module separate cu efort de dezvoltare diferit.
- Pentru utilaje neînmatriculate: confirmați că nu există impozit propriu — vor fi tratate doar ca inventar/evidență, nu ca sursă de calcul fiscal?
- Cât de "smart" trebuie să fie importul — completare manuală după template propriu, sau parsing automat al documentelor oficiale (HCL și/sau norme ANAF)?
- Aplicația trebuie să calculeze impozitul doar informativ (estimare pentru utilizator), sau să devină sursă oficială de calcul (caz în care precizia legală trebuie validată juridic, nu doar tehnic)?
- Cum se gestionează UAT-urile/județele pentru care nu există încă date încărcate într-un an fiscal (fallback la valorile minime din Codul Fiscal? mesaj de avertizare?).
- Pentru animale: e nevoie de praguri de scutire exacte pe categorie (bovine, ovine, caprine, porcine, păsări, albine) — recomand confirmarea cifrelor curente direct din normele ANAF/Codul Fiscal în vigoare înainte de implementare, sursele găsite în research au valori care diferă ușor între ele și pot fi neactualizate.
