# Soil Type Classification in the Agricultural Registry (Registrul Agricol)

## Overview

This document explains the source and rationale for the soil type (`tipSol`) values used in the `CulturaParcela` entity of the Agricultural Registry application. The dropdown list implemented on the frontend is based on the **Romanian Soil Taxonomy System (SRTS-2012)**, the current official national standard for soil classification in Romania.

---

## Data Source: SRTS-2012 (Sistemul Român de Taxonomie a Solurilor)

The soil types listed in the application are derived from **SRTS-2012**, Romania's official soil taxonomy system, developed and maintained by Romanian soil scientists.

### What is SRTS-2012?

SRTS-2012 is the second edition of the Romanian Soil Taxonomy System, published in 2012 as an improvement over SRTS-2003. It is organized as a **two-level hierarchical taxonomy**:

- **High level:** 12 soil classes, 29 genetic soil types, and 67 soil subtypes
- **Low level:** soil variety, species, family, and variant

The system is based on diagnostic horizons, properties, and characteristics, and is correlated with international systems including FAO-UNESCO (1988), WRB-SR (2006), and USDA Soil Taxonomy (2006).

### References

| Source | Description | Link |
|--------|-------------|------|
| Ţărău et al. (2012) | Peer-reviewed paper on SRTS-2012 published in the *Research Journal of Agricultural Science*, Vol. 44(3) — Banat's University of Agricultural Sciences and Veterinary Medicine, Timișoara | [PDF — rjas.ro](https://www.rjas.ro/download/paper_version.paper_file.ac85b655227b8ca7.313436372e706466.pdf) |
| Academia.edu | Full SRTS-2012 document (Romanian) | [academia.edu](https://www.academia.edu/19956270/sistemul_roman_de_taxonomie_a_solurilor) |
| ResearchGate | Correlation between SRCS-1980, SRTS-2003, and SRTS-2012 | [researchgate.net](https://www.researchgate.net/publication/273756612) |

---

## Soil Types Used in the Application

### Classification Approach

The Agricultural Registry application is designed for **administrative use by local authorities (primării) and farmers**, not for scientific pedological studies. In practice, a farmer or municipal clerk recording land data will describe soil using familiar, everyday terms ("pământ argilos", "cernoziom") rather than formal taxonomy identifiers ("Eutricambosol districambic").

For this reason, the frontend dropdown uses a **simplified classification for administrative use**, combining:
- **Common genetic types** from SRTS-2012 that are widely recognized by farmers (e.g. Cernoziom, Podzol)
- **Texture descriptors** that reflect how soil is practically described in the field (e.g. Nisipos, Argilos, Lutos)
- **Characteristic descriptors** used in agricultural practice (e.g. Aluvial, Sărăturat)

This approach is consistent with how agricultural data is collected at the local administration level in Romania, where the end users are clerks and farmers, not soil scientists.

### Values

| Soil Type | Category | Description | Source |
|-----------|----------|-------------|--------|
| Cernoziom | Genetic type (SRTS-2012 — Cernisoluri) | Most fertile type; dominant in Romanian plains; widely recognized by farmers | [Ţărău et al., 2012 — rjas.ro](https://www.rjas.ro/download/paper_version.paper_file.ac85b655227b8ca7.313436372e706466.pdf) |
| Podzol | Genetic type (SRTS-2012 — Spodosoluri) | Sandy, acidic soils; found in forested and mountainous areas | [Ţărău et al., 2012 — rjas.ro](https://www.rjas.ro/download/paper_version.paper_file.ac85b655227b8ca7.313436372e706466.pdf) |
| Aluvial | Characteristic (origin) | Young soils formed on river alluvium; common along major rivers; divided into subtypes: typical, hydric, vertic, and turbian | [agromonitor.ro](https://www.agromonitor.ro/soluri-romania-clasificare-principalele-tipuri-de-sol-pe-terenuri-agricole/) |
| Nisipos | Texture class (coarse/light) | Sandy soil; low water retention; easy to work but low fertility | [saparotativa.ro](https://saparotativa.ro/tipuri-de-sol/) |
| Lutos | Texture class (medium) | Loamy soil; balanced texture; best water and nutrient retention; most suitable for agriculture | [saparotativa.ro](https://saparotativa.ro/tipuri-de-sol/) |
| Argilos | Texture class (fine) | Clay soil; plastic when wet, hard when dry; high water retention but poor drainage | [saparotativa.ro](https://saparotativa.ro/tipuri-de-sol/) |
| Sărăturat | Characteristic (salinity) | Salt-affected soil containing excess mineral salts; widely distributed across Romania; difficult to cultivate | [Wikipedia RO — Sărătură](https://ro.wikipedia.org/wiki/S%C4%83r%C4%83tur%C4%83) |
| Altul | Catch-all | For any soil type not covered by the above options | — |

> **Note on texture classes:** *Nisipos*, *Lutos*, and *Argilos* correspond to the three standard soil texture classes recognized in agronomy (coarse/light, medium, fine), as described in Romanian agricultural literature.

> **Note on grammar:** The neutral adjectival form *Lutos* is used (rather than *Lutoasă*) for consistency with the other descriptors in the list (*Nisipos*, *Argilos*).

---

## Legal Context: Agricultural Registry Regulations

The `CulturaParcela` entity models data recorded in the Romanian Agricultural Registry, which is regulated by national legislation. The concept of a **parcelă agricolă** (agricultural parcel) — the parent entity — is defined in official norms as:

> *"A continuous area of agricultural land belonging to a single farmer, with the same category of use, on which a single group of crops is cultivated."*

### Relevant Legal Documents

| Document | Description | Link |
|----------|-------------|------|
| ANCPI — Technical Norms for the Agricultural Registry | Official norms defining parcels, data structures, and registration procedures | [ancpi.ro (PDF)](https://ran.ancpi.ro/portal/documents/23732/a6a457fe-bbfb-490b-944a-e3b713a287dc) |
| Government Decision — Agricultural Registry 2025-2029 | Latest regulatory framework for the Agricultural Registry period 2025–2029 | [sgg.gov.ro (PDF)](https://sgg.gov.ro/1/wp-content/uploads/2024/12/ANEXE-1.pdf) |
| MADR — Ministry of Agriculture and Rural Development | Official ministry overseeing agricultural land registration and norms | [madr.ro](https://www.madr.ro) |

---

## Implementation Note

In the current implementation, `tipSol` is stored as a free-text `String` field in the `CulturaParcela` entity (both in the Java DTO and the database). The frontend enforces the taxonomy-based list via a `<select>` dropdown, ensuring data consistency while maintaining schema simplicity. If stricter validation is required in the future, `tipSol` could be converted to an `enum` type in Java, with values mapped directly from the SRTS-2012 classification table.

---

## Summary

| Item | Detail |
|------|--------|
| Standard used | SRTS-2012 (Sistemul Român de Taxonomie a Solurilor) |
| Number of classes in SRTS-2012 | 12 |
| Number of genetic types in SRTS-2012 | 29 |
| Types included in the app dropdown | 8 (simplified for administrative use) |
| Regulatory basis for the Registrul Agricol | ANCPI Technical Norms + Government Decision on Agricultural Registry 2025–2029 |
| Implementation | `String` field with frontend `<select>` dropdown |
