package org.beatrice.diploma_new_pharmacy.domain.order.mapper;


import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderAmountResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.mapper.PharmacyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PharmacyMapper.class, OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "amount", expression = "java(mapAmount(order))")
    OrderResponse toDto(Order order);

    List<OrderResponse> toDtoList(List<Order> orders);

    default OrderAmountResponse mapAmount(Order order) {
        return new OrderAmountResponse(
                order.getTotalAmount(),
                order.getDiscount(),
                order.getFinalAmount()
        );
    }
}
