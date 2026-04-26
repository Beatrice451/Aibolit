package org.beatrice.diploma_new_pharmacy.domain.order.mapper;


import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.UpdateOrderStatusRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderAmountResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.mapper.PharmacyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PharmacyMapper.class, OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "amount", expression = "java(mapAmount(order))")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "clientName", expression = "java(order.getFirstName() + \" \" + order.getLastName())")
    OrderResponse toDto(Order order);

    List<OrderResponse> toDtoList(List<Order> orders);

    @Mapping(target = "orderStatus", source = "status")
    void updateFromRequest(UpdateOrderStatusRequest request, @MappingTarget Order entity);


    default OrderAmountResponse mapAmount(Order order) {
        return new OrderAmountResponse(
                order.getTotalAmount(),
                order.getDiscount(),
                order.getFinalAmount()
        );
    }

    }
