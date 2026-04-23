package org.beatrice.diploma_new_pharmacy.domain.product.controller;


import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.UpdateProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody AddProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addProduct(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer id,
            @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

}
