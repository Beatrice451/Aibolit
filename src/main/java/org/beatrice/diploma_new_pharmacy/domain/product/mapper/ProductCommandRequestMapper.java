package org.beatrice.diploma_new_pharmacy.domain.product.mapper;


import org.beatrice.diploma_new_pharmacy.domain.product.dto.command.AddProductCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.command.UpdateProductCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.UpdateProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductCommandRequestMapper {
    AddProductCommand toAddProductCommand(AddProductRequest request);
    UpdateProductCommand toUpdateProductCommand(UpdateProductRequest request);
}
