# Registrul Agricol

A multi-tenant web application for managing the Romanian Agricultural Register (_Registrul Agricol_), built with Spring Boot, PostgreSQL, and Angular.

---

## Tech Stack

- **Backend:** Java 17+, Spring Boot, Spring Security (JWT), Hibernate/JPA
- **Database:** PostgreSQL
- **Frontend:** Angular
- **Infrastructure:** Docker, Docker Compose

---

## Prerequisites

Make sure you have the following installed before running the project:

- [Git](https://git-scm.com/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)
- [Node.js and npm](https://nodejs.org/) (for running the frontend locally)
- [Angular CLI](https://angular.io/cli): `npm install -g @angular/cli`

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-org>/RegistrulAgricol.git
cd RegistrulAgricol
```

### 2. Start the backend and database with Docker

From the root of the project (where `docker-compose.yml` is located):

```bash
docker compose up --build
```

This will start:
- The **PostgreSQL** database
- The **Spring Boot** backend on `http://localhost:8080`

To run it in the background:

```bash
docker compose up --build -d
```

To stop the containers:

```bash
docker compose down
```

### 3. Run the frontend

Open a new terminal and navigate to the frontend folder:

```bash
cd src/frontend
npm install
ng serve
```

The Angular app will be available at `http://localhost:4200`.

---

## Default Accounts

After startup, the following default accounts are available for testing:

| Username | Password | Role |
|---|---|---|
| `superadmin` | `superadmin` | SUPER_ADMIN |

> A SUPER_ADMIN can create tenants and admin users via the API.

---

## API Overview

The backend exposes a REST API at `http://localhost:8080/api`.

| Endpoint | Method | Role Required | Description |
|---|---|---|---|
| `/api/auth/signin` | POST | — | Login and receive JWT token |
| `/api/tenants` | POST | SUPER_ADMIN | Create a new tenant |
| `/api/tenants` | GET | SUPER_ADMIN | List all tenants |
| `/api/users` | POST | ADMIN, SUPER_ADMIN | Create a new user |
| `/api/users` | GET | ADMIN, SUPER_ADMIN | List users for a tenant |

**Authentication:** All protected endpoints require a `Bearer` token in the `Authorization` header.

**Multi-tenancy:** Tenant-scoped endpoints require the `X-Tenant-ID` header set to the tenant's UUID.

---

## Functional Overview

### What It Is

The **Registrul agricol** (Agricultural Register) is a fundamental public administrative document maintained by Romanian local authorities (primării — town halls/mayoralties) that records comprehensive data about the agricultural and property situation of households and individuals within a territorial-administrative unit (comună, oraș, municipiu). It serves simultaneously as a **census instrument, a property record, and a statistical base** for the agricultural sector.

---

### Legal Basis

The core legal framework has evolved through several acts:

- **Ordinance no. 28/2008** on the Agricultural Register (the primary normative act), approved and amended by Law no. 98/2009
- **Government Decision no. 1064/2011** — the methodological norms for applying O.G. 28/2008
- Subsequent amendments aligning it with EU agricultural policy requirements and digitization efforts
- References in the **Civil Code** and **Land Fund legislation** further anchor its legal significance

---

### Who Maintains It

The register is held and managed by the **local public administration authority** — specifically the mayor's office (_primăria_), through designated civil servants (_registratori agricoli_). Every commune, town, and municipality is legally obligated to maintain it.

---

### What It Records

The register captures data across several categories:

**1. Persons / Households**

- Identity of owners/holders (individuals, legal entities, associations)
- Domicile and household composition

**2. Land**

- Total agricultural area held, broken down by: arable land, pastures, hayfields, vineyards, orchards, forests, and other categories
- Land located within or outside the territorial unit
- Land ownership vs. usage title (ownership, lease, concession, usufruct, etc.)

**3. Buildings**

- Dwellings and agricultural/ancillary constructions (barns, silos, greenhouses, etc.)
- Their surface area and construction materials

**4. Agricultural machinery and equipment**

- Tractors, combines, trailers, irrigation equipment, etc.

**5. Livestock**

- Numbers by species and category (cattle, pigs, sheep, goats, poultry, horses, bees, fish, etc.)

**6. Vegetable production**

- Crop types sown/planted on arable land

---

### How Data Is Collected

Holders of agricultural land or livestock are **legally obligated to declare** their assets to the local authority. Declarations are made:

- **Annually**, within set deadlines (typically January–March for carry-over data, and specific windows after planting/harvesting)
- In person, in writing, or — increasingly — through digital means
- False or omitted declarations carry administrative sanctions

Local authorities may also cross-check data with the **Land Cadastre** (ANCPI), the National Agency for Fiscal Administration (ANAF), and veterinary records.

---

### Key Administrative Functions

| Function | Description |
|---|---|
| **Proof of property/possession** | Extracts from the register (_adeverințe_) serve as legal proof of land holding for various administrative purposes |
| **Agricultural subsidies** | Data feeds into APIA (Agency for Payments and Intervention in Agriculture) for EU/national subsidy eligibility |
| **Taxation** | Basis for calculating local agricultural taxes and fees |
| **Zoning and planning** | Input for territorial planning and land-use decisions |
| **Statistics** | Source data for the National Institute of Statistics (INS) agricultural censuses |
| **Credit and insurance** | Banks and insurers use register extracts to verify agricultural assets |
| **Inheritance and transactions** | Required document in many notarial procedures involving agricultural land |

---

### The Register Extract (_Adeverința din Registrul Agricol_)

A key practical output is the **adeverință** (certificate/extract), issued by the primărie upon request. This document is routinely required for:

- Applying for agricultural subsidies
- Notarial acts (sale, inheritance, donation of land)
- Obtaining agricultural loans
- School enrollment benefits in rural areas
- Proof of rural/farmer status for various social or professional purposes

---

### Digitization

Romania has made efforts to migrate the register from paper/spreadsheet formats to a **unified national digital platform**. The goal is interoperability between local authorities, APIA, ANCPI (cadastre), and ANAF, reducing fraud and administrative duplication. Progress has been uneven across municipalities, with urban areas generally more advanced than rural communes.

---

### Significance

The _Registru agricol_ is, in essence, the **foundational administrative record of rural and agricultural Romania**. It sits at the intersection of property law, agricultural policy, local taxation, and EU fund management, making it one of the most practically consequential documents in Romanian local administration — even if largely invisible to those outside the agricultural or legal sphere.
