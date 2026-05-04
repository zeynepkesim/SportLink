package com.esi.week2.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // JpaRepository provides all CRUD operations
    // Custom query methods can be added here if needed
}
