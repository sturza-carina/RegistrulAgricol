# Registru Agricol - Application Documentation

Welcome to the **Registru Agricol** documentation! This document outlines the current state of the application and provides instructions on how to get it running.

## Current State of the App

The application is a modern, full-stack multi-tenant system designed for agricultural management.

### Technology Stack
- **Backend**: Java 21, Spring Boot 3.3.0, Spring Security, Hibernate ORM
- **Frontend**: Angular 18, TypeScript, Nginx (for serving and proxying)
- **Database**: PostgreSQL 15, Flyway (for database migrations)
- **Containerization**: Docker, Docker Compose

### Implemented Features
1. **Multi-Tenancy**: The application uses a "Shared Database, Separate Schema" architecture. Each tenant is assigned a separate PostgreSQL schema ensuring complete data isolation, while a shared `public` schema stores common configuration and user mappings.
2. **Authentication & Authorization**: JWT-based stateless authentication. It supports various roles including `SUPERADMIN`, `ADMIN`, and normal users.
3. **Database Migrations**: Flyway handles automatic initialization of the database structure on startup.
4. **Docker Infrastructure**: Fully containerized environment with backend, frontend, and database instances orchestrated via `docker-compose`. Nginx is configured to serve the frontend and proxy `/api/` requests to the backend.

*(Note: The backend infrastructure is fully operational. The frontend application is currently initialized and set up with basic routing, but the graphical user interface components are pending implementation.)*

## How to Start the App

### Prerequisites
- Docker and Docker Compose installed on your system.
- Port `80` (for frontend), `8080` (for backend), and `5432` (for database) must be available.

### Step-by-Step Guide

1. **Clone or Open the Project**
   Ensure you are in the root directory of the project where the `docker-compose.yml` file is located

2. **Build and Start the Containers**
   Run the following command in your terminal (PowerShell, Command Prompt, or bash):
   ```bash
   docker-compose up -d --build
   ```
   This command will:
   - Start the PostgreSQL database (`db` container).
   - Compile the Java 21 Spring Boot application using Maven and start the backend (`backend` container).
   - Build the Angular 18 application and serve it via Nginx (`frontend` container).

3. **Verify Deployment**
   - Wait a few seconds for the containers to fully start and Flyway to run the initial migrations.
   - You can check the logs using `docker-compose logs -f backend` to ensure the application started successfully.

4. **Access the Application**
   - **Frontend UI**: Open your browser and navigate to [http://localhost](http://localhost).
   - **Backend API**: The API is accessible directly at [http://localhost:8080/api/](http://localhost:8080/api/) or via the reverse proxy at [http://localhost/api/](http://localhost/api/).

5. **First Login**
   The database is initialized with a default superadmin account:
   - **Username**: `superadmin`
   - **Password**: `superadmin`
   
   You can verify the authentication endpoint works by running a POST request to `http://localhost/api/auth/signin` with the credentials above in a JSON payload.

### Stopping the App
To stop the application without destroying the data:
```bash
docker-compose stop
```
To stop and completely remove the containers:
```bash
docker-compose down
```
*(Note: To reset the database entirely, you can remove the associated Docker volume or add the `-v` flag to the down command: `docker-compose down -v`)*
