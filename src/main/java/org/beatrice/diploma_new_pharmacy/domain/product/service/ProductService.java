package org.beatrice.diploma_new_pharmacy.domain.product.service;

import org.beatrice.diploma_new_pharmacy.domain.product.dto.MedicineDto;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.ProductDto;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.ProductFilter;
import org.beatrice.diploma_new_pharmacy.domain.product.mapper.ProductMapper;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Medicine;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.MedicineRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.specification.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MedicineRepository medicineRepository;

    ProductService(ProductRepository productRepository, ProductMapper productMapper, MedicineRepository medicineRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.medicineRepository = medicineRepository;
    }

    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.toDtoList(products);
    }

    public List<MedicineDto> getAllMedicines() {
        List<Medicine> medicines = medicineRepository.findAll();
        return medicines.stream()
                .map(medicine -> (MedicineDto) productMapper.toDto(medicine))
                .collect(Collectors.toList());
    }

    public Page<ProductDto> getProducts(ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = buildSpecification(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(productMapper::toDto);
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
