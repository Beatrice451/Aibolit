package org.beatrice.diploma_new_pharmacy.domain.order.mapper;


import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderItemDto;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "productId", source = "product.id")
    OrderItemDto toDto(OrderItem orderItem);
}
