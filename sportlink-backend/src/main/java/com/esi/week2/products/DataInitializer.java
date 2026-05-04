package com.esi.week2.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no products exist
        if (productRepository.count() == 0) {
            // Add sample products
            productRepository.saveAll(Arrays.asList(
                new Product("01", "Heavy vehicle", "Can be used for heavy work", BigDecimal.valueOf(1200)),
                new Product("02", "Medium vehicle", "Can be used for medium work", BigDecimal.valueOf(1800)),
                new Product("03", "Light vehicle", "Can be used for light work", BigDecimal.valueOf(2200))
            ));

            System.out.println("Sample products initialized!");
        } else {
            System.out.println("Products already exist. Skipping initialization.");
        }
    }
}
