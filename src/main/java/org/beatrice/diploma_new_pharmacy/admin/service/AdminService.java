package org.beatrice.diploma_new_pharmacy.admin.service;


import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.admin.dto.AddProductCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Category;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.CategoryRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Product addProduct(AddProductCommand cmd) {
        Product newProduct = Product.builder()
                .price(cmd.price())
                .description(cmd.description())
                .name(cmd.name())
                .manufacturer(cmd.manufacturer())
                .category(categoryRepository.findCategoryByName(cmd.categoryName()))
                .build();

        return productRepository.save(newProduct);
    }

    public Category addCategory(String categoryName) {
        Category newCategory = Category.builder()
                .name(categoryName)
                .build();

        return categoryRepository.save(newCategory);
    }
}
