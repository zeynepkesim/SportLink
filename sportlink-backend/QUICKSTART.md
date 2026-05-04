# 🚀 Quick Start Guide

## Prerequisites Check
✅ Java 21 - Already installed
✅ PostgreSQL - Already installed and running
✅ Maven - Included as Maven Wrapper (./mvnw)

## 5-Minute Setup

### Step 1: Create Database (30 seconds)
```bash
cd sportlink-backend
./setup-database.sh
```

Or manually:
```bash
sudo -u postgres psql -c "CREATE DATABASE esi2023;"
```

### Step 2: Run the Application (60 seconds)
```bash
./mvnw spring-boot:run
```

Wait for: `Started Week2Application in X seconds`

### Step 3: Open Swagger UI (10 seconds)
Open in browser:
```
http://localhost:8080/swagger-ui.html
```

### Step 4: Test the API (2 minutes)

#### Option A: Using Swagger UI (Easiest)
1. Click on "GET /products" → "Try it out" → "Execute"
2. You should see 3 sample products

#### Option B: Using REST Client File
1. Open `RestClientFile.rest` in VS Code
2. Click "Send Request" above any request

#### Option C: Using curl
```bash
# Get all products
curl http://localhost:8080/products

# Create an order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "01",
    "quantity": 5,
    "totalPrice": 6000,
    "orderDate": "2026-05-04T10:30:00",
    "status": "PENDING"
  }'

# Get all orders
curl http://localhost:8080/orders
```

### Step 5: Run Tests (30 seconds)
```bash
./mvnw test
```

Expected: `Tests run: 14, Failures: 0, Errors: 0`

## 🎯 What You Have Now

### Products API (5 endpoints)
```
GET    /products        - List all products
GET    /products/{id}   - Get product by ID
POST   /products        - Create product
PUT    /products/{id}   - Update product
DELETE /products/{id}   - Delete product
```

### Orders API (6 endpoints)
```
GET    /orders                  - List all orders
GET    /orders/{id}             - Get order by ID
GET    /orders/product/{id}     - Get orders for a product
POST   /orders                  - Create order
PUT    /orders/{id}             - Update order
DELETE /orders/{id}             - Delete order
```

## 📝 Sample Data

### Products (Pre-loaded)
```json
[
  { "id": "01", "name": "Heavy vehicle", "price": 1200 },
  { "id": "02", "name": "Medium vehicle", "price": 1800 },
  { "id": "03", "name": "Light vehicle", "price": 2200 }
]
```

## 🧪 Quick Test Sequence

### 1. Create an Order
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
  "productId": "01",
  "quantity": 5,
  "totalPrice": 6000,
  "orderDate": "2026-05-04T10:30:00",
  "status": "PENDING"
}
```

### 2. Get All Orders
```http
GET http://localhost:8080/orders
```

### 3. Update the Order (change order ID to match created order)
```http
PUT http://localhost:8080/orders/1
Content-Type: application/json

{
  "productId": "01",
  "quantity": 10,
  "totalPrice": 12000,
  "orderDate": "2026-05-04T12:00:00",
  "status": "COMPLETED"
}
```

### 4. Delete the Order
```http
DELETE http://localhost:8080/orders/1
```

## 🔧 Troubleshooting

### Port 8080 already in use
```bash
# Find and kill the process
lsof -ti:8080 | xargs kill -9
# Or change port in application.properties
echo "server.port=8081" >> src/main/resources/application.properties
```

### Database connection error
```bash
# Check PostgreSQL is running
systemctl status postgresql

# Restart PostgreSQL
sudo systemctl restart postgresql
```

### Tests failing
```bash
# Clean and rebuild
./mvnw clean install
```

## 📚 Documentation

- **SETUP.md** - Detailed setup instructions
- **SWAGGER-SETUP.md** - Swagger/OpenAPI guide
- **REFACTORING.md** - Code refactoring details
- **IMPLEMENTATION-SUMMARY.md** - Complete overview

## 🎉 You're Ready!

Your Spring Boot REST API is now running with:
- ✅ 11 REST endpoints
- ✅ PostgreSQL database
- ✅ Swagger documentation
- ✅ 14 unit tests
- ✅ Best practices implementation

**Next:** Explore the Swagger UI at http://localhost:8080/swagger-ui.html
