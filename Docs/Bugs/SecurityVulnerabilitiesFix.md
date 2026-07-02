# Security Vulnerabilities & Fixes Report

This document details two critical security vulnerabilities discovered in the multitenant architecture of the Agriculture Register system, how they functioned, and how they were resolved.

---

## 1. Tenant Isolation Bypass & Cross-Tenant Transfer

### How the Bug Worked
Initially, the backend used an HTTP request header (`X-Tenant-ID`) to determine which tenant database schema to switch to for the current request context:

```
[Frontend Client] ---> (X-Tenant-ID: uat_cluj) ---> [TenantFilter] ---> TenantContext (uat_cluj)
```

1. **Isolation Bypass:** Because this header was client-controlled, any authenticated user (e.g., `ROLE_USER` from Cluj) could bypass database isolation entirely and access/modify another tenant's schema (e.g., Bucharest) simply by sending `X-Tenant-ID: uat_bucuresti` in the headers.
2. **SQL Injection:** The header value was concatenated directly into database connection search-path queries (`SET search_path TO uat_<tenantId>, public`), allowing malicious headers to execute arbitrary SQL injection commands.

### The Incomplete Fix
A previous attempt to resolve this in `TenantFilter.java` removed the `X-Tenant-ID` header check entirely, opting to derive the tenant ID server-side from the user's JWT. 

* **The Regression:** However, this broke the legitimate cross-tenant animal transfer feature. When transferring animals, a user from one tenant must query the destination tenant's households and owners list to select the target destination. Because the filter completely ignored `X-Tenant-ID` and resolved context solely from the user's local JWT, the frontend dropdowns loaded local data instead of destination data, rendering transfers broken.

### The Safe Solution (Implemented)
We designed **Option B** to fix the vulnerability without breaking the transfer functionality:

1. **Strict General Isolation:** The global filter [TenantFilter.java](file:///d:/info/projects/registru-agricol/src/backend/src/main/java/com/multitenant/config/tenant/TenantFilter.java) remains strictly JWT-based. Standard endpoints cannot be bypassed via headers.
2. **Dedicated Lookup Endpoints:** We introduced a new, secure controller: [CrossTenantLookupController.java](file:///d:/info/projects/registru-agricol/src/backend/src/main/java/com/multitenant/controller/CrossTenantLookupController.java).
   - Rather than allowing generic endpoints to change schemas, this controller provides dedicated endpoints (`/api/transfers/destinations/gospodarii` and `/api/transfers/destinations/persons`) specifically for transfer destinations.
   - It takes the target tenant ID as a explicit request parameter (`targetTenantId`), validates it, programmatically switches the tenant context inside a `try-finally` block, fetches only the needed fields, and restores the original context securely:
   ```java
   String originalTenantId = TenantContext.getCurrentTenant();
   try {
       TenantContext.setCurrentTenant(targetTenantId);
       // execute scoped query...
   } finally {
       TenantContext.setCurrentTenant(originalTenantId);
   }
   ```
3. **Frontend Alignment:** We updated [animal.service.ts](file:///d:/info/projects/registru-agricol/src/frontend/src/app/services/animal.service.ts#L135-L160) to query the new endpoints, passing the target tenant as a parameter instead of relying on the custom header.

---

## 2. Password Hash Exposure in API Responses

### How the Bug Worked
Password hashes stored in the database were being exposed to the client in two ways:

1. **Authentication Principal Serialization:**
   During a login or profile request, the `/api/auth/me` endpoint in [AuthController.java](file:///d:/info/projects/registru-agricol/src/backend/src/main/java/com/multitenant/controller/AuthController.java) returned the `UserDetailsImpl` object representing the logged-in user.
   Because `UserDetailsImpl` holds the password (which is loaded from the database during authentication), and because the field did not have any serialization exclusions, the BCrypt password hash was serialized and sent in the JSON response.
   
2. **Direct Entity Exposure Risk:**
   The base `User` entity had the `password` field declared as a raw string with no serialization guards. Even though most controller endpoints map to DTOs, returning the `User` entity directly in future code would immediately leak the password hash.

### How We Fixed It

We applied Jackson annotations to prevent the password hashes from being serialized on output, while still allowing the backend to ingest passwords on input (e.g. for registration and password changes):

1. **Exclude Auth Context Passwords:**
   In [UserDetailsImpl.java](file:///d:/info/projects/registru-agricol/src/backend/src/main/java/com/multitenant/security/UserDetailsImpl.java), we added `@JsonIgnore` to the `password` field:
   ```java
   @JsonIgnore
   private String password;
   ```
   This ensures Jackson completely ignores the field when serializing the principal to `/api/auth/me`.

2. **Make Entity Passwords Write-Only:**
   In [User.java](file:///d:/info/projects/registru-agricol/src/backend/src/main/java/com/multitenant/model/core/User.java), we annotated the field with `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`:
   ```java
   @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
   private String password;
   ```
   This allows incoming JSON payloads to map the password during registration or updates (write), but prevents the password hash from ever being serialized on JSON output (read).
