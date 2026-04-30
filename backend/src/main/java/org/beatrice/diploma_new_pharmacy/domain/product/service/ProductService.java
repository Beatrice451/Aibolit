package org.beatrice.diploma_new_pharmacy.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.UpdateProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.MedicineResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.ProductNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.product.mapper.ProductMapper;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Medicine;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.MedicineRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.specification.ProductFilter;
import org.beatrice.diploma_new_pharmacy.domain.product.specification.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MedicineRepository medicineRepository;
    private final CategoryService categoryService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProductResponse addProduct(AddProductRequest request) {
        Product newProduct = Product.builder()
                .price(request.price())
                .description(request.description())
                .name(request.name())
                .manufacturer(request.manufacturer())
                .imageUrl(request.imageUrl())
                .category(categoryService.getCategoryEntityById(request.categoryId()))
                .build();

        return productMapper.toDto(productRepository.save(newProduct));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteProduct(Integer productId) {
        productRepository.findProductById(productId)
                .ifPresent(product -> {
                    product.setIsActive(false);
                    productRepository.save(product);
                });
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProductResponse updateProduct(Integer productId, UpdateProductRequest request) {
        log.debug("Product update request received: {}", request);
        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        productMapper.updateFromRequest(request, product);

        log.debug("Product updated: {}", product);

        return productMapper.toDto(productRepository.save(product));
    }


    public List<MedicineResponse> getAllMedicines() {
        List<Medicine> medicines = medicineRepository.findAll();
        return medicines.stream()
                .map(medicine -> (MedicineResponse) productMapper.toDto(medicine))
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByFilter(ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = buildSpecification(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(productMapper::toDto);
    }

    public ProductResponse getProductById(Integer id) {
        return productMapper.toDto(productRepository.findProductById(id)
                                           .orElseThrow(() -> new ProductNotFoundException("Product not found")));
    }

    public Product getProductEntityById(Integer id) {
        return productRepository.findProductById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }


    private Specification<Product> buildSpecification(ProductFilter filter) {
        Specification<Product> spec = Specification.where(ProductSpecifications.activeOnly());

        if (filter.search() != null && !filter.search().isBlank()) {
            spec = spec.and(ProductSpecifications.search(filter.search()));
        }

        if (filter.categoryId() != null) {
            spec = spec.and(ProductSpecifications.hasCategoryId(filter.categoryId()));
        }

        if (filter.manufacturer() != null && !filter.manufacturer().isBlank()) {
            spec = spec.and(ProductSpecifications.manufacturerLike(filter.manufacturer()));
        }

        if (filter.minPrice() != null || filter.maxPrice() != null) {
            spec = spec.and(ProductSpecifications.priceBetween(filter.minPrice(), filter.maxPrice()));
        }

        return spec;

    }


}
