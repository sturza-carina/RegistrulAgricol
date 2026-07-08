# Raport Modificări - Contracte de Utilizare și Scheduler

Acest document rezumă modificările făcute în proiectul **Registru Agricol** pentru contractele de utilizare și, în special, pentru automatizarea expirării contractelor la miezul nopții.

---

## 1. Modificări Backend

### 1.1. Schimbarea relațiilor contractului

Am schimbat modelul **ContractUtilizare** astfel încât:
- `locatorProprietar` să fie legat de `Persoana`
- `locatorUtilizator` să fie legat de `Persoana`
- `utilizatorOperare` să fie legat tot de `Persoana`

Asta înseamnă că aplicația nu mai depinde de `User` pentru aceste câmpuri din contract.

### 1.2. DTO pentru creare și actualizare

Pentru a evita problemele de deserializare JSON cu `Persoana` (care este o clasă abstractă și folosește polymorphism), am introdus:
- `ContractUtilizareDTO`

Acesta trimite doar ID-urile:
- `terenId`
- `locatorProprietarId`
- `locatorUtilizatorId`
- `utilizatorOperareId`

Backend-ul caută entitățile direct din bază și le atașează contractului.

### 1.3. Scheduler pentru expirarea automată

Am adăugat un job programat:
- clasa `ContractExpirationScheduler`
- activat prin `@EnableScheduling`

Jobul rulează zilnic la:
- `00:00`
- zona: `Europe/Bucharest`

Ce face concret:
1. ia lista de tenanturi
2. comută pe fiecare tenant prin `TenantContext`
3. caută contractele active cu `dataSfarsit < ziua curentă`
4. le marchează automat:
   - `statusContract = EXPIRAT`
   - `esteActiv = false`

### 1.4. Query de update în repository

În `ContractUtilizareRepository` am introdus un update query SQL/JPA care face actualizarea în masă a contractelor expirate fără să le încarce pe toate manual.

---

## 2. Modificări Frontend

### 2.1. Formular contracte

În pagina de management contracte:
- dropdown-urile pentru proprietar, utilizator și operare folosesc acum **persoane**
- payload-ul trimis la backend conține doar ID-uri, nu obiecte complete

### 2.2. Serviciul Angular

`contract-utilizare.service.ts` a fost ajustat pentru:
- tipuri de request separate
- referințe simple pentru datele trimise la backend

### 2.3. Afișare date

În ecranul de detalii contract:
- numele persoanei se afișează din `Persoana`
- nu mai sunt afișați utilizatori de sistem pentru aceste câmpuri

---

## 3. Integrarea Scheduler-ului

Scheduler-ul este important deoarece:
- elimină nevoia de actualizare manuală
- păstrează starea contractelor corectă în sistem
- actualizează contractele în toate tenanturile
- folosește aceeași logică pentru toate schemele

Fluxul este:
1. contractul are o dată de sfârșit
2. la miezul nopții, jobul compară data curentă cu `dataSfarsit`
3. dacă termenul a trecut, contractul devine `EXPIRAT`
4. contractul este marcat și ca inactiv

---

## 4. Observații

- contractele cu `dataSfarsit = azi` nu expiră imediat; expiră după trecerea zilei, la jobul de la miezul nopții
- statusurile `REZILIAT` și `SUSPENDAT` nu sunt modificate de scheduler
- mecanismul rulează pe toate tenanturile existente

