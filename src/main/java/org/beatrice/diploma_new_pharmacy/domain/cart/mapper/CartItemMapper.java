package org.beatrice.diploma_new_pharmacy.domain.cart.mapper;


import org.beatrice.diploma_new_pharmacy.domain.cart.dto.CartItemResponse;
import org.beatrice.diploma_new_pharmacy.domain.cart.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImage", source = "product.imageUrl")
    @Mapping(target = "price", source = "product.price")
    CartItemResponse toDto(CartItem cartItem);
}
