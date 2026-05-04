# OpenAPI/Swagger Documentation Setup

## ✅ What Was Added

### 1. Maven Dependency (pom.xml)
Added the SpringDoc OpenAPI dependency:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

### 2. OpenAPI Configuration Bean (Week2Application.java)
Added custom OpenAPI configuration:
```java
@Bean
public OpenAPI springShopOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("Products API")
        .description("Spring Boot REST API for managing products")
        .version("v1.0.0")
        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
        .externalDocs(new ExternalDocumentation()
        .description("Products API Documentation")
        .url("http://localhost:8080/swagger-ui.html"));
}
```

## 📚 Accessing the API Documentation

Once your application is running, access the documentation at:

### Swagger UI (Interactive Documentation)
```
http://localhost:8080/swagger-ui.html
```
or
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification (JSON)
```
http://localhost:8080/v3/api-docs
```

### OpenAPI Specification (YAML)
```
http://localhost:8080/v3/api-docs.yaml
```

## 🎯 Features of Swagger UI

The Swagger UI provides:

1. **Interactive Testing**
   - Test all API endpoints directly from the browser
   - No need for Postman or curl commands
   - Send requests and see responses in real-time

2. **Complete Documentation**
   - All endpoints automatically documented
   - Request/response schemas
   - Parameter descriptions
   - HTTP status codes

3. **Model Schemas**
   - View the Product model structure
   - See required vs optional fields
   - Data type information

4. **Authentication** (if configured)
   - Test authenticated endpoints
   - Add API keys or tokens

## 🚀 How to Use Swagger UI

1. **Start the application:**
   ```bash
   cd sportlink-backend
   ./mvnw spring-boot:run
   ```

2. **Open Swagger UI in your browser:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **Test an endpoint:**
   - Click on any endpoint (e.g., GET /products)
   - Click "Try it out" button
   - Click "Execute" button
   - View the response

4. **Test POST/PUT with request body:**
   - Click on POST /products or PUT /products/{id}
   - Click "Try it out"
   - Edit the JSON request body
   - Click "Execute"
   - View the response

## 📋 Available Endpoints in Swagger

When you open Swagger UI, you'll see all these endpoints documented:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | Get all products |
| GET | `/products/{id}` | Get a product by ID |
| POST | `/products` | Create a new product |
| PUT | `/products/{id}` | Update a product |
| DELETE | `/products/{id}` | Delete a product |

## 🔧 Customizing the Documentation

You can enhance the API documentation by adding annotations to your controller:

### Example: Add descriptions to endpoints
```java
@Operation(summary = "Get all products", description = "Retrieves a list of all products")
@GetMapping
public List<Product> getAllProducts() {
    return productService.getAllProducts();
}
```

### Example: Add parameter descriptions
```java
@Operation(summary = "Get product by ID")
@GetMapping("/{id}")
public Optional<Product> getProduct(
    @Parameter(description = "Product ID") @PathVariable String id) {
    return productService.getProduct(id);
}
```

### Example: Document response codes
```java
@Operation(summary = "Create a new product")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Product created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
})
@PostMapping
public void addProduct(@RequestBody Product product) {
    productService.addProduct(product);
}
```

## 🎨 UI Customization (Optional)

You can customize Swagger UI appearance in `application.properties`:

```properties
# Swagger UI settings
springdoc.swagger-ui.path=/api-docs
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
```

## 📦 Benefits of OpenAPI/Swagger

1. **Automatic Documentation** - No need to manually write API docs
2. **Always Up-to-Date** - Documentation updates automatically with code changes
3. **Interactive Testing** - Test APIs without additional tools
4. **Client Generation** - Generate client libraries in various languages
5. **API Contract** - Clear contract between frontend and backend teams
6. **Standardized Format** - OpenAPI is an industry standard

## 🔍 Troubleshooting

### Swagger UI not loading?
1. Make sure the application is running
2. Check the console for any errors
3. Try: `http://localhost:8080/swagger-ui/index.html`

### Endpoints not showing?
1. Ensure `@RestController` annotation is present on controllers
2. Verify Spring Boot can scan the controller package
3. Check application logs for errors

### Custom path not working?
Add to `application.properties`:
```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui-custom.html
```

## 📖 Additional Resources

- [SpringDoc Official Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
