# Raport Modificări - Implementare ContractUtilizare

Acest document descrie modificările și adăugările realizate în cadrul proiectului **Registru Agricol** pentru a implementa funcționalitatea completă de tip CRUD (Creare, Citire, Actualizare, Ștergere) a contractelor de utilizare (arendă, comodat, etc.). 

Funcționalitatea este expusă în mod securizat exclusiv pentru utilizatorul cu rol de `SUPER_ADMIN` la nivel de sistem.

---

## 1. Baza de Date (Schema Tenant)

Deoarece contractele de utilizare sunt asociate cu terenuri (`Teren`), care sunt specifice fiecărei primării (UAT / Tenant), datele contractelor trebuie izolate.

- **Adăugat**: Fișierul de migrare Flyway **[V11__create_contract_utilizare_table.sql](file:///d:/RADU/Registru-Agricol/src/backend/src/main/resources/db/tenant/V11__create_contract_utilizare_table.sql)**.
- **Descriere**: Creează tabela `contract_utilizare` în schema fiecărui tenant cu relații de chei externe (`REFERENCES`) către:
  - Tabela `teren` (în schema tenantului respectiv, cu regulă de ștergere în cascadă).
  - Tabela `public.users` (chei externe inter-schema pentru locator_proprietar, locator_utilizator și utilizator_operare).

---

## 2. Backend (Spring Boot Java)

Am creat structura standard din arhitectura Spring Boot:

- **Enums**:
  - **[TipContractUtilizare.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/registru/TipContractUtilizare.java)**: definește tipurile de contract (`ARENDA`, `COMODAT`, `CONCESIUNE`, `INCHIRIERE`, `ALTELE`).
  - **[StatusContractUtilizare.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/registru/StatusContractUtilizare.java)**: definește stările posibile (`ACTIV`, `EXPIRAT`, `REZILIAT`, `SUSPENDAT`).
- **Entity**:
  - **[ContractUtilizare.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/model/registru/ContractUtilizare.java)**: Clasă JPA mapată pe tabelă, adnotată cu `@Enumerated(EnumType.STRING)` și `@ManyToOne` pentru relațiile cu `Teren` și `User`.
- **Repository**:
  - **[ContractUtilizareRepository.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/repository/ContractUtilizareRepository.java)**: Extinde `JpaRepository` pentru lucrul cu tabela de contracte.
- **Service**:
  - **[ContractUtilizareService.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/service/ContractUtilizareService.java)**: Conține metodele de business (validări pentru chei externe și operații de salvare/actualizare/ștergere). Pe salvare/actualizare înregistrează automat utilizatorul curent din contextul de securitate (`SecurityContextHolder`) în câmpul `utilizatorOperare`.
- **Controller**:
  - **[ContractUtilizareController.java](file:///d:/RADU/Registru-Agricol/src/backend/src/main/java/com/multitenant/controller/ContractUtilizareController.java)**: Expune API-ul REST sub ruta `/api/contracte`. Protejat la nivel de clasă/metodă cu `@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")` și dispune de validări pentru a preveni rularea operațiunilor în afara unui context de tenant valid (nu permite operarea direct în schema `public`).

---

## 3. Frontend (Angular)

Implementarea frontend respectă sistemul modular Standalone din Angular 18:

- **Service**:
  - **[contract-utilizare.service.ts](file:///d:/RADU/Registru-Agricol/src/frontend/src/app/services/contract-utilizare.service.ts)**: Expune metode HTTP pentru comunicarea cu backend-ul.
- **Componentă (Pagina)**:
  - **[contract-management.component.ts](file:///d:/RADU/Registru-Agricol/src/frontend/src/app/pages/contract-management/contract-management.component.ts)**: Logica Angular care preia rolul super-admin-ului, încarcă lista de tenanți disponibili, permite selectarea/impersonarea unui tenant, încarcă terenurile și utilizatorii disponibili pe tenantul activ și gestionează stările formularelor de CRUD.
  - **[contract-management.component.html](file:///d:/RADU/Registru-Agricol/src/frontend/src/app/pages/contract-management/contract-management.component.html)**: Template cu design premium. Permite selectarea UAT-ului dintr-un dropdown, afișează tabelul cu filtre/căutare, ecran de vizualizare detalii și formular complet de adăugare/editare.
  - **[contract-management.component.scss](file:///d:/RADU/Registru-Agricol/src/frontend/src/app/pages/contract-management/contract-management.component.scss)**: Stilurile specifice layouts pentru tabele și elemente de formulare.
- **Navigare și Rute**:
  - **[app.routes.ts](file:///d:/RADU/Registru-Agricol/src/frontend/src/app/app.routes.ts)**: Înregistrează ruta `/contracte` mapată pe componenta de contracte.
  - **[sidebar.component.html](file:///d:/RADU/Registru-Agricol/src/frontend/src/app/components/sidebar/sidebar.component.html)**: Adaugă opțiunea "Contracte Utilizare" în meniul lateral, protejată de Directiva `*ngIf="user?.role === 'ROLE_SUPER_ADMIN'"`.

---

## 4. Integrare Multi-tenancy

Pentru a permite unui `SUPER_ADMIN` (care nu aparține unui tenant specific implicit) să gestioneze contractele dintr-o primărie specifică:
1. Super Admin navighează la "Contracte Utilizare".
2. Alege primăria dorită din dropdown-ul "Alegeți Tenant / UAT".
3. Acest dropdown apelează `AuthService.setImpersonation(tenantId)`.
4. `jwtInterceptor` interceptează toate apelurile HTTP ulterioare către backend și le adaugă header-ul `X-Tenant-ID: <tenantId>`.
5. Backend-ul comută dinamic schema conexiunii bazei de date pe schema tenantului selectat (`uat_<cod>`) și rulează query-ul corespunzător.
