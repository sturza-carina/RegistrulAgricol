# Registru Agricol - Code Review & Security Analysis

After reviewing the current state of the codebase (both frontend and backend), the following security vulnerabilities, bugs, and areas for improvement have been identified. 

---

## 🚨 High Security Risks

### 1. Missing Authorization Checks on Business Controllers
**Risk Level: High**
- **Issue**: Controllers like `PersoanaController` only have class-level `@PreAuthorize` annotations (e.g., `hasRole('ROLE_USER')`). This allows any authenticated user to hit create (`POST`), update (`PUT`), and delete (`DELETE`) endpoints without restriction. 
- **Impact**: Any authenticated user can modify or delete records within their tenant, violating the principle of least privilege.
- **Recommendation**: Apply granular `@PreAuthorize` checks at the method level. Typically, read operations might be open to `ROLE_USER`, but write/delete endpoints must be restricted to `ROLE_ADMIN` or specific data-entry roles.

### 2. Hardcoded Secrets & Credentials
**Risk Level: High**
- **Issue**: `application.yml` contains a hardcoded `jwt.secret` and a default database password (`admin_password`).
- **Impact**: If this repository is ever made public or the source code is leaked, attackers can easily forge valid JWTs and gain full Super Admin access.
- **Recommendation**: Do not commit secrets to source control. Use environment variables exclusively, and ensure the fallback defaults are not used in production (e.g., fail fast if secrets are missing).

---

## ⚠️ Medium Security Risks

### 1. Disabled CORS & CSRF
**Risk Level: Medium**
- **Issue**: `SecurityConfig.java` has `cors(cors -> cors.disable())`. 
- **Impact**: Disabling the CORS configuration might inadvertently allow browsers to accept requests from malicious origins.
- **Recommendation**: Explicitly define a `CorsConfigurationSource` restricting allowed origins, methods, and headers, instead of disabling it entirely.

---

## 🐛 Bugs & Logical Flaws

### 1. Unvalidated Tenant Impersonation
- **Issue**: `AuthController.impersonateTenant` allows a `ROLE_SUPER_ADMIN` to generate a JWT for any `tenantId` requested. It does not query the database to verify if the target `tenantId` actually exists.
- **Recommendation**: Validate the requested `tenantId` against the `TenantRepository` before generating the impersonation token.

### 2. Collection Manipulation in Hibernate (Orphan Removal Bug)
- **Issue**: In `PersoanaService.updatePerson`, existing collections are modified using `.clear()` (e.g., `existingPhysical.getIdentityDocuments().clear()`) and then repopulated. 
- **Impact**: While sometimes acceptable, this can frequently cause Hibernate `PersistentCollection` orphan-removal exceptions depending on how `cascade` and `orphanRemoval` are configured.
- **Recommendation**: Avoid `.clear()`. Instead, find the differences and remove individual items, or ensure proper `cascade = CascadeType.ALL, orphanRemoval = true` configuration on the `@OneToMany` relationships and maintain the reference to the original collection.

---

## 🛠️ Code Quality & Architecture Improvements

### 1. Missing Pagination
- **Issue**: Endpoints like `getAllPersons` return unpaginated lists (`List<PersoanaDTO>`).
- **Recommendation**: As the database grows, this will cause memory exhaustion (OutOfMemory errors) and slow response times. Implement Spring Data `Pageable` and return `Page<T>` for all collection endpoints.


---

## ✅ Resolved Issues

These critical vulnerabilities were identified previously but have since been **successfully resolved**:

1. **Privilege Escalation & Broken Access Control**: `ROLE_USER` accounts could previously escalate their privileges to Admin by submitting a PUT request to the `UserController`. This has been fixed by implementing strict SpEL `@PreAuthorize` checks on `updateUser`.
2. **JWT Tokens Stored in LocalStorage**: The JWT token was previously stored in browser `localStorage`, making it highly vulnerable to Cross-Site Scripting (XSS) attacks. The application has been migrated to use `HttpOnly` and `SameSite` cookies, neutralizing this threat.
3. **Repetitive Tenant Context Checks**: The boilerplate `if ("public".equals(TenantContext.getCurrentTenant()))` checks present across all business controllers have been completely removed. This was resolved by creating a custom `@TenantRequired` annotation backed by a Spring AOP Aspect, adhering to DRY principles.
4. **Use of `System.out.println` for Logging**: Replaced hardcoded `System.out.println` statements with SLF4J logging (`@Slf4j`) in `AuthController.java` to ensure logs are properly formatted and leveled.
