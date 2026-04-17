package org.beatrice.diploma_new_pharmacy.domain.cart.mapper;


import org.beatrice.diploma_new_pharmacy.domain.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.domain.cart.model.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {


    CartResponse toDto(Cart cart);
}
