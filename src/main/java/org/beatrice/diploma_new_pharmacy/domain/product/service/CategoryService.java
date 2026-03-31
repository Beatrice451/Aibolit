package org.beatrice.diploma_new_pharmacy.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddCategoryRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.CategoryResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.CategoryAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.CategoryNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.product.mapper.CategoryMapper;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Category;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryResponse addCategory(AddCategoryRequest request) {
        Category parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository.findCategoryById(request.parentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        }
        Category newCategory = Category.builder()
                .name(request.name())
                .parent(parent)
                .build();

        try {
            return CategoryMapper.toResponse(categoryRepository.save(newCategory));
        } catch (DataIntegrityViolationException e) {
            throw new CategoryAlreadyExistsException("Category already exists");
        }
    }

    public List<CategoryResponse> getCategories() {
        return CategoryMapper.toTree(categoryRepository.findAll());
    }

    public CategoryResponse getCategoryById(Integer id) {
        List<CategoryResponse> tree = CategoryMapper.toTree(categoryRepository.findAll());
        CategoryResponse inTree = findInTree(tree, id);
        if (inTree == null) throw new CategoryNotFoundException("Category not found");
        return inTree;
    }

    public Category getCategoryEntityById(Integer id) {
        return categoryRepository.findCategoryById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    }


    private CategoryResponse findInTree(List<CategoryResponse> nodes, Integer id) {
        for (CategoryResponse node : nodes) {
            if (node.id().equals(id)) {
                return node;
            }
            CategoryResponse found = findInTree(node.children(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
