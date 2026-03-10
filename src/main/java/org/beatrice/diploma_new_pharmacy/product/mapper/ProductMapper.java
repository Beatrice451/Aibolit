package org.beatrice.diploma_new_pharmacy.product.mapper;

import org.beatrice.diploma_new_pharmacy.product.dto.MedicineDto;
import org.beatrice.diploma_new_pharmacy.product.dto.ProductDto;
import org.beatrice.diploma_new_pharmacy.product.model.Medicine;
import org.beatrice.diploma_new_pharmacy.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @SubclassMapping(source = Medicine.class, target = MedicineDto.class)
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);



}
