# Spring Boot REST API - Setup Guide

## Project Overview
This is a Spring Boot application that provides a REST API for managing products with PostgreSQL database.

## Prerequisites
- Java 21 (✓ Already installed)
- PostgreSQL (✓ Already installed and running)
- Maven (included as Maven Wrapper)

## Database Setup

### Step 1: Create the Database
Run the following command to create the database:

```bash
sudo -u postgres psql -c "CREATE DATABASE esi2023;"
```

If you get a password prompt, enter your PostgreSQL password.

**Alternative:** Open pgAdmin and create a database named `esi2023` manually.

### Step 2: Verify Database Configuration
The database configuration is already set in `src/main/resources/application.properties`:
- Database: `esi2023`
- Username: `postgres`
- Password: `postgres`
- Port: `5432`

If your PostgreSQL credentials are different, update the `application.properties` file.

## Running the Application

### Option 1: Using Maven Wrapper (Recommended)
```bash
cd sportlink-backend
./mvnw spring-boot:run
```

### Option 2: Build and Run JAR
```bash
cd sportlink-backend
./mvnw clean package
java -jar target/week2-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Documentation (Swagger/OpenAPI)

Once the application is running, you can access the interactive API documentation at:

**Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```
or
```
http://localhost:8080/swagger-ui/index.html
```

**OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```

The Swagger UI provides:
- Interactive API documentation
- Ability to test endpoints directly from the browser
- Request/response examples
- Schema definitions

## Testing the API

### Using the REST Client File
Open `RestClientFile.rest` in VS Code with the REST Client extension installed, then click "Send Request" above each request.

### Using curl

1. **Get all products:**
```bash
curl http://localhost:8080/products
```

2. **Get a product by id:**
```bash
curl http://localhost:8080/products/01
```

3. **Create a new product:**
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "04",
    "name": "Super Heavy vehicle",
    "description": "Can be used for very heavy work",
    "price": 3500
  }'
```

4. **Update a product (Task 2 & 3):**
```bash
curl -X PUT http://localhost:8080/products/01 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "01",
    "name": "Heavy vehicle - Updated",
    "description": "Updated description for heavy work",
    "price": 1500
  }'
```

5. **Delete a product (Task 5):**
```bash
curl -X DELETE http://localhost:8080/products/03
```

## Project Structure

```
sportlink-backend/
├── src/main/java/com/esi/week2/
│   ├── Week2Application.java          # Main Spring Boot application
│   └── products/
│       ├── Product.java                # Entity class (JPA)
│       ├── ProductRepository.java      # JPA Repository interface
│       ├── ProductService.java         # Service layer (Tasks 1 & 4)
│       ├── ProductController.java      # REST Controller (Tasks 2 & 5)
│       └── DataInitializer.java        # Initializes sample data
├── src/main/resources/
│   └── application.properties          # Database configuration
└── RestClientFile.rest                 # API test requests

```

## Completed Tasks

### ✅ Task 1: Update Product in ProductService
```java
public void updateProduct(String id, Product product) {
    product.setId(id);
    productRepository.save(product);
}
```

### ✅ Task 2: Update Product in ProductController
```java
@PutMapping("/{id}")
public void updateProduct(@RequestBody Product product, @PathVariable String id) {
    productService.updateProduct(id, product);
}
```

### ✅ Task 3: Test using RestClientFile.rest
See the PUT request in `RestClientFile.rest`

### ✅ Task 4: Delete Product in ProductService
```java
public void deleteProduct(String id) {
    productRepository.deleteById(id);
}
```

### ✅ Task 5: Delete Product in ProductController
```java
@DeleteMapping("/{id}")
public void deleteProduct(@PathVariable String id) {
    productService.deleteProduct(id);
}
```

## Initial Sample Data
The application automatically initializes with three sample products:
- 01: Heavy vehicle - $1200
- 02: Medium vehicle - $1800
- 03: Light vehicle - $2200

## Troubleshooting

### Port 8080 already in use
Change the port in `application.properties`:
```properties
server.port=8081
```

### Cannot connect to database
1. Verify PostgreSQL is running: `systemctl status postgresql`
2. Check credentials in `application.properties`
3. Ensure database `esi2023` exists

### Build errors
Clean and rebuild:
```bash
./mvnw clean install
```
