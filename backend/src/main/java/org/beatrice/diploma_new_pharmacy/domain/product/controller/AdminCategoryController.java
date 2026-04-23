package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddCategoryRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.CategoryResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody AddCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.addCategory(request));
    }

}
