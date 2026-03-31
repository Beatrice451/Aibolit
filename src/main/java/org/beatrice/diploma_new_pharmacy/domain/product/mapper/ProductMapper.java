package org.beatrice.diploma_new_pharmacy.domain.product.mapper;

import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.UpdateProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.MedicineResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Medicine;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    @SubclassMapping(source = Medicine.class, target = MedicineResponse.class)
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toDto(Product product);

    List<ProductResponse> toDtoList(List<Product> products);

    void updateFromRequest(UpdateProductRequest request, @MappingTarget Product entity);


}
