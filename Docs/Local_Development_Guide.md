# Local Development Guide (Without Docker)

This guide explains how to run the Multi-Tenant Backend and Angular Frontend completely independently on your local machine using the Command Line Interface (CLI) and native tools. 

Because the project configuration gracefully falls back to `localhost` defaults when Docker environment variables are missing, no code changes are required to run natively!

---

## 1. Database Setup (Native PostgreSQL)

Since you are not using Docker to spin up the database, you must install and run PostgreSQL natively on your Windows machine.

1. Install **PostgreSQL** (version 15+ is recommended).
2. Ensure the PostgreSQL service is running on the default port: `5432`.
3. Open pgAdmin or your preferred SQL client and create the following credentials and database (these match the default fallbacks in `application.yml`):
   - **Database Name**: `multi_tenant_db`
   - **Username**: `admin`
   - **Password**: `admin_password`

---

## 2. Starting the Backend (Spring Boot)

The backend natively defaults to `jdbc:postgresql://localhost:5432/multi_tenant_db`, so it will automatically hook into your local PostgreSQL instance.

### From the Command Line
1. Open your terminal (PowerShell or Command Prompt).
2. Navigate to the backend directory:
   ```bash
   cd src/backend
   ```
3. Run the Spring Boot application using Maven:
   ```bash
   mvn spring-boot:run
   ```
*(Flyway will automatically run the migrations and seed the `multi_tenant_db` database on startup. The server will be available at `http://localhost:8080`.)*

### From an IDE (IntelliJ, VS Code, Eclipse)
- Navigate to `src/backend/src/main/java/com/multitenant/BackendApplication.java`.
- Click the green **Play / Run** button provided by your IDE next to the `main` method.

---

## 3. Starting the Frontend (Angular)

The Angular project uses a `proxy.conf.json` file which is already registered inside `angular.json`. This means that any request made to `/api` is automatically routed to your locally running backend at `http://localhost:8080`.

### From the Command Line
1. Open a new terminal.
2. Navigate to the frontend directory:
   ```bash
   cd src/frontend
   ```
3. Install dependencies (if you haven't already):
   ```bash
   npm install
   ```
4. Start the development server:
   ```bash
   npm start
   ```
*(This triggers `ng serve` and opens the application on `http://localhost:4200` with hot-reloading enabled.)*

### From an IDE
- Open `src/frontend/package.json`.
- Find the `"scripts"` block and click the **Play** button next to the `"start"` script.
