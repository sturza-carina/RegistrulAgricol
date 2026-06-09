# Agriculture Register (Registrul Agricol) - Application Overview

## Introduction
The **Agriculture Register (Registrul Agricol)** is a web-based application designed to securely manage and digitize agricultural records. The system is built around a "multi-tenant" model, meaning it allows multiple administrative or regional units (referred to as **UATs**) to use the same platform while keeping their data completely isolated and secure from one another.

---

## 1. Core Functionalities

### Organization & Registration Management
- **Tenant (UAT) Creation:** Ability to register new regional offices or agricultural organizations into the system, generating an isolated workspace for them.
- **UAT Configuration:** Manage the details of the territorial units (SIRUTA code, county, name, and type of UAT) that operate within the system.

### User Management
- **Account Creation & Role Assignment:** Register new users (employees, administrators, viewers) into specific regional workspaces.
- **User Profiles:** Track user details including name, email, role, activity status, and last login time.

### Entity & Person Management
The application acts as a central repository for the people and companies operating within a specific region.
- **Physical Persons:** Register and manage individual citizens. Tracks personal details like CNP (National ID), first name, last name, and date of birth.
- **Identity Documents:** Attach and track official identification documents (ID cards, Passports, etc.) to physical persons.
- **Legal Entities:** Register and manage companies, tracking details like CUI (Company Registration Code), company name, and the legal representative.
- **Relationships:** Map complex relationships between registered entities (e.g., Parent/Child, Spouse, Shareholder, Administrator).

### Agricultural Households (Gospodării)
- Register and maintain records of agricultural households within a UAT, including their physical addresses, active status, and specific identification codes.

---

## 2. User Roles & Permissions

The application relies on three distinct access levels to ensure data privacy and operational security. What a user sees and what they can do depends entirely on their assigned role.

### 🌟 Super Administrator (`ROLE_SUPER_ADMIN`)
The Super Administrator is the system overseer. They have a global view of the application but do not handle the day-to-day agricultural data entry.
- **What they can do:**
  - Create and provision entirely new Tenants (UATs) into the system.
  - View the master list of all registered Tenants across the entire platform.
  - Access global settings and high-level reports.
- **What they see:** A global navigation menu including the "Tenants" tab and "New Registration" tools.

### 🏢 Tenant Administrator (`ROLE_ADMIN`)
The Tenant Administrator manages the system for a *single specific region or organization (UAT)*. They cannot see data belonging to other regions.
- **What they can do:**
  - Create, edit, and deactivate user accounts for their own region.
  - Assign roles (e.g., granting someone 'User' access).
  - Manage UAT-specific configurations.
  - Fully manage Persons (Physical/Legal), documents, relationships, and households within their region.
- **What they see:** All tabs *except* the global "Tenants" tab. They have access to the "Users" management tab.

### 👤 Standard User / Registrar (`ROLE_USER`)
The Standard User is typically a clerk or registrar responsible for daily data entry within their assigned region. They have no administrative privileges.
- **What they can do:**
  - View, search, and filter the list of Persons (Physical and Legal entities) registered in their region.
  - Add new Persons, update existing records, and manage relationships or documents (based on their specific operational permissions).
  - Manage Agricultural Households (Gospodării).
- **What they see:** Only the operational tabs (Persons, UATs, Reports, Dashboard). They **cannot** see the "Tenants" tab or the "Users" management tab. 

---

## 3. General Navigation & Interface
- **Dynamic Sidebar:** The left-hand navigation menu automatically adjusts based on the user's role. If a user does not have permission to access a feature (like User Management), the button is completely hidden.
- **Dashboards:** Users are greeted with tailored dashboards. For example, Super Admins see global metrics and recent tenant registrations, while Tenant Admins see their local team members and recent regional activity.
- **Filtering & Search:** Built-in tools on management pages allow users to quickly find specific records (e.g., searching for a person by CNP or name, filtering by active/inactive status).
