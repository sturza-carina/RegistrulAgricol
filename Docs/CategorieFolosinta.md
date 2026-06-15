# Categorie de Folosință

## Ce este

Categoria de folosință descrie modul în care este utilizat un teren agricol (ex: Arabil, Pășune, Livadă). Fiecare teren poate avea una sau mai multe categorii înregistrate, cu denumire și descriere opțională.

## Unde se găsește în aplicație

Secțiunea este integrată în pagina de parcele a unui teren:

**Gospodării → click pe o gospodărie → click pe un teren → panoul stâng**

Ruta: `/terenuri/:id/parcele`

Secțiunea apare deasupra listei de parcele, sub metadata terenului (Tip Teren / Coordonate).

## Funcționalități

| Acțiune | Cum |
|---|---|
| Vizualizare categorii | Vizibile direct în panoul stâng |
| Adăugare | Buton **+ Adaugă Categorie** → completează Denumire + Descriere → Salvează |
| Editare | Butonul ✎ din dreptul categoriei |
| Ștergere | Butonul ✕ din dreptul categoriei (cu confirmare) |

## Fișiere implicate

### Frontend

| Fișier | Rol |
|---|---|
| `src/frontend/src/app/models/categorie-folosinta.model.ts` | Interfața `CategorieFolosinta` (id, denumire, descriere, terenId) |
| `src/frontend/src/app/services/categorie-folosinta.service.ts` | Serviciu HTTP pentru CRUD |
| `src/frontend/src/app/pages/teren-parcele/teren-parcele.component.ts` | Logica CRUD (loadCategorii, saveCategorie, deleteCategorie) |
| `src/frontend/src/app/pages/teren-parcele/teren-parcele.component.html` | UI — lista + formular adăugare/editare |
| `src/frontend/src/app/pages/teren-parcele/teren-parcele.component.css` | Stiluri pentru `.categorie-block`, `.categorie-item`, `.btn-icon`, `.add-categorie-form` |

### Backend

| Fișier | Rol |
|---|---|
| `src/backend/src/main/java/com/multitenant/model/registru/CategorieFolosinta.java` | Entitate JPA |
| `src/backend/src/main/java/com/multitenant/repository/CategorieFolosintaRepository.java` | Repository Spring Data |
| `src/backend/src/main/java/com/multitenant/service/CategorieFolosintaService.java` | Serviciu business logic |
| `src/backend/src/main/java/com/multitenant/controller/CategorieFolosintaController.java` | REST Controller |
| `src/backend/src/main/resources/db/tenant/V11__create_categorie_folosinta_table.sql` | Migrare Flyway — creare tabel |

## Endpoint-uri API

```
GET    /api/terenuri/{terenId}/categorii-folosinta   — lista categoriilor unui teren
POST   /api/terenuri/{terenId}/categorii-folosinta   — creare categorie nouă
PUT    /api/categorii-folosinta/{id}                 — actualizare categorie
DELETE /api/categorii-folosinta/{id}                 — ștergere categorie
```

Toate endpoint-urile necesită autentificare (`ROLE_USER`, `ROLE_ADMIN` sau `ROLE_SUPER_ADMIN`) și funcționează în contextul tenant-ului curent.

## Relație cu parcela

Pe lângă categoriile dinamice per-teren, parcela are și un câmp `categorieFolosinta` (string) ales dintr-o listă statică hardcodată în componentă:

```
Arabil, Pășune, Fânețe, Livadă, Vii, Pădure, Ape, Alte
```

Cele două sunt independente — categoriile dinamice sunt entități proprii legate de teren, nu sunt folosite ca sursă pentru selectul de pe parcelă.
