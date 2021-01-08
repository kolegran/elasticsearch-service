package com.github.kolegran.elasticsearch.migration;

import com.github.kolegran.elasticsearch.product.ProductsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductsMigrationController {

    private final ProductsService productsService;

    public ProductsMigrationController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @PostMapping("/migrate")
    public void migrateProducts() {
        productsService.migrateProducts();
    }
}
