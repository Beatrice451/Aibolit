package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import org.beatrice.diploma_new_pharmacy.domain.product.dto.MedicineDto;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.ProductDto;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.ProductFilter;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable
    ) {
        ProductFilter filter = new ProductFilter(
                search, categoryId, null, null, manufacturer, minPrice, maxPrice
        );
        Page<ProductDto> products = productService.getProducts(filter, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineDto>> getAllMedicines() {
        return ResponseEntity.ok(productService.getAllMedicines());
    }
}
