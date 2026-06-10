# Agriculture Register - Change Log v1.1

## Changes & Updates

### 1. Renamed Classes
- Refactored **all** model and controller classes across the entire domain to use standard Romanian naming conventions. This comprehensive transition ensures absolute consistency with agricultural register terminology (e.g., renaming `Person` to `Persoana`, `Household` to `Gospodarie`, `Address` to `Adresa`, `IdentityDocument` to `ActIdentitate`, etc.).
- Corresponding controllers and services were also updated to match the new entity names.

### 2. JWT Token Update
- Overhauled the JWT authentication mechanism to strictly enforce tenant separation and improve security:
  - **Custom Claims for Multi-Tenancy**: The token generation (`JwtUtils.java`) was updated to embed the `tenantId` directly into the JWT payload as a custom claim alongside the subject (username). This ensures the tenant context is securely preserved across HTTP requests.
  - **Modernized Signature Verification**: Refactored token signing and parsing to use the latest `io.jsonwebtoken` Builder API, utilizing `Keys.hmacShaKeyFor` and `HS256` for robust symmetric key encryption.
  - **Filter Enhancements**: Updated `JwtAuthenticationFilter` to safely extract the `Bearer` token, validate its signature, verify the user's active status (`isEnabled()`), and properly construct the `UsernamePasswordAuthenticationToken` for the Spring Security Context.

### 3. Controller Updates
- Updated the API endpoints within the controllers (including `UserController` and `PersoanaController`) to reflect the new class names.
- Adjusted payload handling and routing to support the updated entity structures and secure authentication flow.
