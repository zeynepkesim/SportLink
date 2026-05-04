# Code Refactoring Documentation

## Overview
This document describes the refactoring work done to follow best practices for Spring Boot REST APIs, including the implementation of DTOs (Data Transfer Objects), Lombok usage, and proper test coverage.

## ✅ Completed Refactoring Work

### 1. Added Dependencies

#### Lombok Dependency
Added to `pom.xml`:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

**Benefits:**
- Eliminates boilerplate code (getters, setters, constructors)
- Cleaner, more maintainable code
- Reduces potential for bugs

### 2. Package Structure Reorganization

Created a proper package structure following best practices:

```
com.esi.week2
├── products
│   ├── Product.java (Entity)
│   ├── ProductRepository.java
│   ├── ProductService.java (Refactored with DTOs)
│   ├── ProductController.java (Refactored with DTOs)
│   ├── DataInitializer.java (Updated)
│   └── dto
│       ├── ProductRequestDTO.java
│       ├── ProductResponseDTO.java
│       └── ProductMapper.java
└── orders
    ├── Order.java (Entity)
    ├── OrderRepository.java
    ├── OrderService.java
    ├── OrderController.java
    └── dto
        ├── OrderRequestDTO.java
        ├── OrderResponseDTO.java
        └── OrderMapper.java
```

### 3. Entity Refactoring with Lombok

#### Product Entity (Product.java)
**Before:**
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;

    // Constructor, getters, setters (60+ lines)
}
```

**After:**
```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // Lombok generates getters, setters, toString, equals, hashCode
}
```

**Improvements:**
- Reduced code by ~70%
- Added relationship with Order entity
- Used Lombok annotations for boilerplate code

#### Order Entity (Order.java)
**New entity with Lombok from the start:**
```java
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
}
```

### 4. DTO Pattern Implementation

#### Why DTOs?
1. **Separation of Concerns** - API layer separated from database layer
2. **Security** - Control what data is exposed to clients
3. **Flexibility** - Different representations for requests and responses
4. **Performance** - Avoid lazy loading issues and circular references

#### Product DTOs

**ProductRequestDTO.java** - For creating/updating products:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}
```

**ProductResponseDTO.java** - For returning product data:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}
```

**ProductMapper.java** - For converting between entity and DTOs:
```java
@Component
public class ProductMapper {
    public ProductResponseDTO toResponseDTO(Product product);
    public Product toEntity(ProductRequestDTO dto);
    public void updateEntityFromDTO(ProductRequestDTO dto, Product product);
}
```

#### Order DTOs
Similar structure with additional fields for product information.

### 5. Service Layer Refactoring

#### ProductService (Before)
```java
public List<Product> getAllProducts() {
    return productRepository.findAll();
}

public void addProduct(Product product) {
    productRepository.save(product);
}
```

#### ProductService (After)
```java
public List<ProductResponseDTO> getAllProducts() {
    return productRepository.findAll().stream()
            .map(productMapper::toResponseDTO)
            .collect(Collectors.toList());
}

@Transactional
public ProductResponseDTO addProduct(ProductRequestDTO productRequestDTO) {
    Product product = productMapper.toEntity(productRequestDTO);
    Product savedProduct = productRepository.save(product);
    return productMapper.toResponseDTO(savedProduct);
}
```

**Improvements:**
- Uses DTOs instead of exposing entities
- Added `@Transactional` for data consistency
- Proper error handling with RuntimeException
- Returns created objects for better API design

### 6. Controller Layer Refactoring

#### ProductController (Before)
```java
@GetMapping
public List<Product> getAllProducts() {
    return productService.getAllProducts();
}

@PostMapping
public void addProduct(@RequestBody Product product) {
    productService.addProduct(product);
}
```

#### ProductController (After)
```java
@GetMapping
public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
    List<ProductResponseDTO> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
}

@PostMapping
public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO productRequestDTO) {
    ProductResponseDTO createdProduct = productService.addProduct(productRequestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
}
```

**Improvements:**
- Uses DTOs for request/response
- Returns `ResponseEntity` with proper HTTP status codes
- Returns created objects (RESTful best practice)
- Better error handling capabilities

### 7. Complete Order Implementation

Created a complete CRUD API for orders:

**OrderController Endpoints:**
- `GET /orders` - Get all orders
- `GET /orders/{id}` - Get order by ID
- `GET /orders/product/{productId}` - Get all orders for a product
- `POST /orders` - Create new order
- `PUT /orders/{id}` - Update order
- `DELETE /orders/{id}` - Delete order

**Features:**
- Full DTO implementation
- Proper error handling
- Transactional operations
- Relationship with Product entity

### 8. Test Coverage

#### ProductControllerTests.java
Created comprehensive unit tests using `@WebMvcTest`:

```java
@WebMvcTest(ProductController.class)
class ProductControllerTests {
    @Test void testGetAllProducts()
    @Test void testGetProductById()
    @Test void testCreateProduct()
    @Test void testUpdateProduct()      // ✅ PUT test case implemented
    @Test void testDeleteProduct()
    @Test void testGetProductNotFound()
}
```

**Key Testing Concepts:**
- `@WebMvcTest` - Only loads web layer (faster tests)
- `@MockBean` - Mocks the service layer
- `MockMvc` - Simulates HTTP requests
- Verifies HTTP status codes, JSON responses, and service method calls

#### OrderControllerTests.java
Complete test suite for OrderController:

```java
@WebMvcTest(OrderController.class)
class OrderControllerTests {
    @Test void testGetAllOrders()
    @Test void testGetOrderById()
    @Test void testGetOrdersByProduct()
    @Test void testCreateOrder()
    @Test void testUpdateOrder()
    @Test void testDeleteOrder()
    @Test void testGetOrderNotFound()
    @Test void testCreateOrderWithInvalidProduct()
}
```

### 9. Updated REST Client File

Enhanced `RestClientFile.rest` with:
- All product CRUD operations
- All order CRUD operations
- Comments explaining each request
- Sample data for testing

## 📋 How to Test the Application

### 1. Setup Database
```bash
cd sportlink-backend
./setup-database.sh
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

### 3. Test Using Swagger UI
Open: `http://localhost:8080/swagger-ui.html`

### 4. Test Using REST Client
Open `RestClientFile.rest` in VS Code and click "Send Request" above each request.

### 5. Run Unit Tests
```bash
./mvnw test
```

## 🔄 Refactoring Best Practices Followed

### 1. Incremental Refactoring
✅ Refactored one service at a time (GET all, GET by ID, POST, PUT, DELETE)

### 2. Kept Working Copies
✅ Old code kept as comments in refactored files for reference

### 3. Service Before Controller
✅ Refactored `@Service` layer first, then `@RestController`

### 4. Test After Each Change
✅ Tested each modification using `RestClientFile.rest`

## 🎯 Benefits of Refactoring

| Aspect | Before | After |
|--------|--------|-------|
| Code Lines | ~200 per entity | ~100 per entity |
| Boilerplate | Manual getters/setters | Lombok generated |
| API Design | Returns entities | Returns DTOs |
| HTTP Status | Void/implicit 200 | Proper ResponseEntity |
| Error Handling | None | RuntimeException with messages |
| Testing | No tests | Full test coverage |
| Relationships | None | Product ↔ Order |
| Transactions | None | @Transactional |

## 🔧 Testing Order Operations

### Create Orders for Products
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

### Fetch Orders for a Product
```http
GET http://localhost:8080/orders/product/01
```

### Update an Order
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

### Delete an Order
```http
DELETE http://localhost:8080/orders/2
```

## 📝 Key Learnings

1. **DTOs vs Entities** - Never expose JPA entities directly in REST APIs
2. **Lombok Benefits** - Reduces code by 50-70% while improving readability
3. **ResponseEntity** - Always return proper HTTP status codes
4. **Testing Strategy** - Use `@WebMvcTest` for fast, focused controller tests
5. **Transactions** - Always use `@Transactional` for operations that modify data
6. **Error Handling** - Return meaningful error messages to clients
7. **Mappers** - Centralize entity-DTO conversion logic

## 🚀 Next Steps

1. **Global Exception Handler** - Implement `@ControllerAdvice` for better error handling
2. **Validation** - Add `@Valid` annotations and validation rules to DTOs
3. **Pagination** - Implement pagination for GET all endpoints
4. **API Documentation** - Add `@Operation` annotations for better Swagger docs
5. **Integration Tests** - Add `@SpringBootTest` for full integration testing

## 📚 References

- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Lombok Documentation](https://projectlombok.org/)
- [Spring Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html)
- [RESTful API Design](https://restfulapi.net/)
