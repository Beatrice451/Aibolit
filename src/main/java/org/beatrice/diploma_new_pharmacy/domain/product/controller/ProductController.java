package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.MedicineResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.ProductFilter;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable
    ) {
        ProductFilter filter = new ProductFilter(
                search, categoryId, null, null, manufacturer, minPrice, maxPrice // TODO
        );
        Page<ProductResponse> products = productService.getProducts(filter, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(productService.getAllMedicines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
