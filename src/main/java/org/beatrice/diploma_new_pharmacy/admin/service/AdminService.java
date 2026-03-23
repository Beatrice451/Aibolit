package org.beatrice.diploma_new_pharmacy.admin.service;


import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.command.AddCategoryCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.command.AddProductCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.command.UpdateProductCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.CategoryResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.service.CategoryService;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductResponse addProduct(AddProductCommand cmd) {
        return productService.addProduct(cmd);
    }

    public ProductResponse updateProduct(Integer productId, UpdateProductCommand cmd) {
        return productService.updateProduct(productId, cmd);
    }

    public void deleteProduct(Integer productId) {
        productService.deleteProduct(productId);
    }

    public CategoryResponse addCategory(AddCategoryCommand cmd) {
        return categoryService.addCategory(cmd);
    }

    public List<CategoryResponse> getCategories() {
        return categoryService.getCategories();
    }

    public CategoryResponse getCategoryById(Integer id) {
        return categoryService.getCategoryById(id);
    }

}
