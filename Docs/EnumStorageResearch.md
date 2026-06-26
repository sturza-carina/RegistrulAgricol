# Research: Best Practices for Managing Enumerated Values in a Multi-Tenant Agricultural Registry

## Context

The application is a **multi-tenant Spring Boot + Angular + PostgreSQL** system (Registrul Agricol) using **schema-per-tenant** isolation managed via **Flyway** migrations.

### Relevant entities and fields

Three fields currently use hardcoded dropdown lists on the frontend:

| Field                | Entity           | DB Column             | Type in DB |
|----------------------|------------------|-----------------------|------------|
| `tipSol`             | `CulturaParcela` | `tip_sol`             | `VARCHAR(100)` |
| `categorieFolosinta` | `Parcela`        | `categorie_folosinta` | `VARCHAR(100)` |
| `tipSursaApa`        | `SursaApa`       | `tip_sursa`           | `VARCHAR(100)` |

Both are plain `String` fields in Java with no DB-level constraint — any string can be persisted. The frontend enforces the allowed values via a `<select>` dropdown backed by a hardcoded TypeScript array.

### Important distinction: `CategorieFolosinta` entity

The project also has a separate `CategorieFolosinta` entity (table `categorie_folosinta`) which represents **user-defined categories per teren** — this is a completely different concept from the `categorieFolosinta` string field on `Parcela`. The two are unrelated; the research below concerns only the two hardcoded string fields above.

### The question

**Where and how should these enumerated values be managed?** Three approaches are compared below.

---

## Option 1: Hardcoded List on the Frontend (Status Quo)

The values exist only in the Angular component as a TypeScript array:

```typescript
tipuriSol = ['Cernoziom', 'Podzol', 'Aluvial', 'Nisipos', 'Lutos', 'Argilos', 'Sărăturat', 'Altul'];
```

### Advantages
- **Zero backend/DB changes** — no migrations, no new endpoints
- **Fast to implement** — already done
- Simple to understand for a small, stable list

### Disadvantages
- **No backend validation** — a malicious or incorrect API call can insert any string into the database; the constraint exists only in the UI layer
- **Requires redeployment to change values** — adding a new soil type means modifying source code and redeploying the frontend
- **Duplicated if used in multiple places** — if another component or a future mobile client needs the same list, it must be duplicated
- **Not scalable** — as more hardcoded lists accumulate (soil type, land use category, crop type, etc.), the frontend becomes a growing source of truth for business data

### When it's acceptable
For a proof-of-concept or a university project with a fixed, never-changing list of values that will never need backend validation.

---

## Option 2: Java Enum + PostgreSQL Enum Type (Per-Tenant Schema)

Define a Java enum and map it to a PostgreSQL `ENUM` type inside each tenant schema:

```java
public enum TipSol {
    CERNOZIOM, PODZOL, ALUVIAL, NISIPOS, LUTOS, ARGILOS, SARATURAT, ALTUL
}
```

```sql
-- In each tenant's Flyway migration
CREATE TYPE tip_sol AS ENUM ('CERNOZIOM', 'PODZOL', 'ALUVIAL', 'NISIPOS', 'LUTOS', 'ARGILOS', 'SARATURAT', 'ALTUL');
ALTER TABLE cultura_parcela ADD COLUMN tip_sol tip_sol;
```

### Advantages
- **Type safety at the Java level** — the compiler catches invalid values
- **DB-level constraint** — PostgreSQL rejects any value not in the enum definition
- **Compact storage** — PostgreSQL stores enum values as 4-byte integers internally, not as strings

### Disadvantages

#### Critical issue: PostgreSQL enums are painful to modify
PostgreSQL `ENUM` types cannot be easily altered. Adding or removing a value requires workarounds:

```sql
-- You cannot simply do: ALTER TYPE tip_sol ADD VALUE 'NEW_VALUE' inside a transaction
-- This is a known PostgreSQL limitation for ALTER TYPE inside transactions
```

As documented in PostgreSQL community discussions, modifying an enum type requires complex workarounds: drop the column, drop the type, recreate both — which is risky on production data. [(Source: postgresql.org)](https://www.postgresql.org/message-id/49DCDA27.4090901@megafon.hr)

#### Critical issue: Multi-tenant Flyway cost
In a schema-per-tenant architecture, **every Flyway migration runs once per tenant schema**. If you have 50 tenants, a migration that alters a PostgreSQL enum type runs 50 times. As the number of tenants grows, this becomes:

- A deployment risk (failure on tenant #23 leaves schemas in inconsistent states)
- A slow deployment process
- A maintenance burden every time business requirements change a value

This is documented in multi-tenant Flyway architecture guides: each schema gets its own `flyway_schema_history` table and migrations must be applied to all of them consistently. [(Source: red-gate.com)](https://www.red-gate.com/blog/handling-multiple-schemas-in-the-same-database-with-flyway/)

#### Enum vs. lookup table — industry consensus
A widely cited PostgreSQL best practices guide states that enums are only appropriate when:
> *"The values represent an internal state machine the product team does not negotiate. The set of values changes maybe once a year, after a real engineering review."*

For product-facing values like soil types or land use categories — which a business admin might reasonably want to update — the recommendation is a lookup table. [(Source: monpg.app)](https://monpg.app/blog/postgresql-enum-vs-lookup-table)

### When it's acceptable
Internal state machine fields that **never change** and are **not visible to end users** as configurable options (e.g., `OrderStatus: PENDING → SHIPPED → DELIVERED`).

---

## Option 3: Lookup Tables in the `public` Schema ✅ Recommended

Create simple reference tables in the shared `public` schema (where the `tenants` table already lives), populated once via a single Flyway migration:

```sql
-- V{n}__add_lookup_tables.sql  (runs ONCE, in the public schema only)
CREATE TABLE public.tip_sol (
    id   SERIAL PRIMARY KEY,
    nume VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO public.tip_sol (nume) VALUES
    ('Cernoziom'), ('Podzol'), ('Aluvial'),
    ('Nisipos'), ('Lutos'), ('Argilos'),
    ('Sărăturat'), ('Altul');

CREATE TABLE public.categorie_folosinta_ref (
    id   SERIAL PRIMARY KEY,
    nume VARCHAR(50) NOT NULL UNIQUE
);

-- Note: named categorie_folosinta_ref to avoid collision with the existing
-- per-tenant categorie_folosinta entity table (user-defined categories per teren)
INSERT INTO public.categorie_folosinta_ref (nume) VALUES
    ('Arabil'), ('Pășune'), ('Fânețe'), ('Livadă'), ('Vii'), ('Pădure'), ('Ape'), ('Alte');
```

The `tip_sol` column in each tenant's `cultura_parcela` table stores the string value (or optionally a foreign key to `public.tip_sol` if cross-schema FK is acceptable in your setup).

The backend exposes lightweight read-only endpoints:

```java
// Repository — native query to public schema
@Query(value = "SELECT nume FROM public.tip_sol ORDER BY nume", nativeQuery = true)
List<String> findAllTipuriSol();

@Query(value = "SELECT nume FROM public.categorie_folosinta ORDER BY nume", nativeQuery = true)
List<String> findAllCategoriiFolosinta();
```

```java
// Controller — one endpoint per lookup
@GetMapping("/api/enums/tip-sol")
public List<String> getTipuriSol() { return tipSolRepository.findAll(); }

@GetMapping("/api/enums/categorie-folosinta")
public List<String> getCategoriiF() { return categorieFolosintaRefRepository.findAll(); }
```

```typescript
// Angular — fetch at component init
ngOnInit(): void {
  this.enumService.getTipuriSol().subscribe(values => this.tipuriSol = values);
}
```

### Advantages

- **Single source of truth** — all tenants read from the same `public` lookup tables; no duplication across schemas
- **One migration, not N** — the Flyway script runs once in `public`, not once per tenant schema; adding a new tenant automatically inherits the lookup values without any additional migration
- **Easy to change values** — adding a new soil type is a single `INSERT` statement, with no redeployment needed
- **Backend validation is possible** — the service layer can validate incoming values against the DB list before persisting
- **Scalable** — any future enumerated field (crop type, irrigation method, ownership type, etc.) follows the same pattern: one new table in `public`, one new endpoint
- **Consistent with the existing architecture** — the `public` schema already plays the role of shared metadata (tenants table); lookup tables fit naturally there
- **No Flyway complexity per tenant** — as documented in multi-tenant Spring Boot + Flyway guides, keeping shared data in a single metadata schema and tenant-specific data in tenant schemas is the recommended separation of concerns [(Source: cloudflight.io)](https://engineering.cloudflight.io/database-migrations-using-flyway-in-dynamic-multi-tenant-spring-boot-applications)

### Disadvantages
- **Requires backend changes** — new repository, service method, and controller endpoint for each lookup table (though minimal — ~20 lines of code per lookup)
- **Extra HTTP request on frontend init** — the Angular component must fetch the list on load instead of having it inline; negligible performance impact, can be cached
- **Cross-schema FK limitation** — if you want a hard FK constraint from `tenant_schema.cultura_parcela.tip_sol_id → public.tip_sol.id`, PostgreSQL supports this but Hibernate/JPA makes it awkward; the simpler approach is to store the string value and validate at the service layer

### When to use it
Whenever the list of values is **user-visible**, **might change over time**, or is **shared across multiple tenants** — which is true for both `tipSol` and `categoriefolosinta`.

---

## Comparison Summary

| Criterion | Frontend Hardcode | Java/PG Enum | Lookup Table in `public` |
|-----------|:-----------------:|:------------:|:------------------------:|
| Backend validation | ❌ None | ✅ Strong | ✅ Service-level |
| Easy to add values | ❌ Redeploy needed | ❌ Complex migration | ✅ Single INSERT |
| Flyway cost (multi-tenant) | ✅ Zero | ❌ Runs per tenant | ✅ Runs once in `public` |
| Single source of truth | ❌ | ❌ Per-tenant copy | ✅ Shared in `public` |
| Implementation effort | ✅ Already done | ❌ High | 🟡 Low–Medium |
| Scales to more lists | ❌ | ❌ | ✅ |
| Works well with Flyway | ✅ | ❌ | ✅ |

---

## Recommendation

**Implement Option 3 (lookup tables in `public`) for both `tipSol` and `categoriefolosinta`.**

This is the approach most consistent with:
- The existing multi-tenant architecture (schema-per-tenant + shared `public`)
- Flyway best practices for multi-tenant Spring Boot applications
- PostgreSQL best practices for product-facing enumerated values
- Future scalability (new enumerated fields follow the same pattern)

The implementation effort is low: one Flyway migration on the `public` schema, two repository methods, two controller endpoints, and two Angular service calls — replacing the existing hardcoded arrays.

---

## Sources

| Source | Topic |
|--------|-------|
| [CYBERTEC PostgreSQL — Lookup table or enum type?](https://www.cybertec-postgresql.com/en/lookup-table-or-enum-type/) | Detailed comparison of string, enum, and lookup table approaches in PostgreSQL with benchmarks |
| [MonPG — PostgreSQL ENUMs vs Lookup Tables](https://monpg.app/blog/postgresql-enum-vs-lookup-table) | Decision guide: when enums are fine vs. when lookup tables always win |
| [postgresql.org — ENUM vs DOMAIN vs FK lookup](https://www.postgresql.org/message-id/49DCDA27.4090901@megafon.hr) | Community discussion on the pain of modifying PostgreSQL enum types |
| [cloudflight.io — Flyway in dynamic multi-tenant Spring Boot](https://engineering.cloudflight.io/database-migrations-using-flyway-in-dynamic-multi-tenant-spring-boot-applications) | How to separate public (metadata) and tenant migrations in Flyway |
| [red-gate.com — Multiple schemas with Flyway](https://www.red-gate.com/blog/handling-multiple-schemas-in-the-same-database-with-flyway) | How Flyway handles schema-per-tenant migrations and history tables |
| [sultanov.dev — Schema-based multi-tenancy with Spring Data + Flyway](https://sultanov.dev/blog/schema-based-multi-tenancy-with-spring-data/) | Full example of public vs. tenant schema separation in Spring Boot |
| [reflectoring.io — Flyway + Spring Boot multitenancy](https://reflectoring.io/flyway-spring-boot-multitenancy/) | Running Flyway across multiple tenant datasources at startup |
