package org.beatrice.diploma_new_pharmacy.domain.order.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.cart.model.Cart;
import org.beatrice.diploma_new_pharmacy.domain.cart.model.CartItem;
import org.beatrice.diploma_new_pharmacy.domain.cart.service.CartService;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.CreateOrderCommand;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.mapper.OrderMapper;
import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderItem;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.beatrice.diploma_new_pharmacy.domain.order.repository.OrderRepository;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.service.PharmacyService;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderOwnerService orderOwnerService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final PharmacyService pharmacyService;
    private final UserService userService;
    @PersistenceContext
    private EntityManager em;

    public List<OrderResponse> getOrdersByIdentity(OrderIdentity identity) {
        var orderOwner = orderOwnerService.resolveOrderOwner(identity);
        List<Order> orders = orderRepository.getOrdersByOrderOwner(orderOwner);
        return orderMapper.toDtoList(orders);
    }


    @Transactional
    public OrderResponse createOrder(CreateOrderCommand cmd) {
        Order order = create(cmd);
        em.flush();
        em.refresh(order);
        return orderMapper.toDto(order);
    }


    private Order create(CreateOrderCommand cmd) {
        String phone;
        String email;

        if (cmd.identity().isUser()) {
            User user = userService.getUserById(cmd.identity().userId());
            phone = user.getPhone();
            email = user.getEmail();
        } else {
            phone = cmd.phone();
            email = cmd.email();

            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("Phone number is required for guest");
            }
        }

        Cart cart = cartService.getCart(cmd.identity());
        List<CartItem> cartItems = cart.getItems();
        if (cartItems.isEmpty()) throw new IllegalArgumentException("Cart is empty, cannot create an order");

        Pharmacy pharmacy = pharmacyService.getPharmacyById(cmd.pharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy with id " + cmd.pharmacyId() + "not found")); // TODO replace with custom exception
        var totalAmount = cartService.countTotalCartAmount(cart);
        var discount = getDiscount();

        Order order = Order.builder()
                .orderOwner(cart.getOrderOwner())
                .pharmacy(pharmacy)
                .orderStatus(OrderStatus.NEW)
                .totalAmount(totalAmount)
                .discount(discount)
                .phone(phone)
                .email(email)
                .build();


        List<OrderItem> orderItems = mapListCartItem(cartItems, order);

        order.setOrderItems(orderItems);

        cartService.truncateCart(cart);
        return orderRepository.saveAndFlush(order);

    }


    private OrderItem mapCartItem(CartItem cartItem, Order order) {
        return OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .priceAtSale(cartItem.getProduct().getPrice())
                .build();

    }

    private List<OrderItem> mapListCartItem(List<CartItem> cartItems, Order order) {
        return cartItems.stream()
                .map(cartItem -> mapCartItem(cartItem, order))
                .toList();

    }


    private BigDecimal getDiscount() { // TODO I DONT KNOW WHAT TO DO WITH THIS SHIT
        return BigDecimal.ZERO;
    }

}
