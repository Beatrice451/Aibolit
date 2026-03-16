package org.beatrice.diploma_new_pharmacy.cart.mapper;


import org.beatrice.diploma_new_pharmacy.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.cart.model.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {


    CartResponse toDto(Cart cart);
}
