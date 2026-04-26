package org.beatrice.diploma_new_pharmacy.domain.order.specification;

import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

public class OrderSpecifications {
    public static Specification<Order> hasOrderStatus(OrderStatus orderStatus) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("orderStatus"), orderStatus));
    }

    public static Specification<Order> hasPharmacyId(Integer pharmacyId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("pharmacy").get("id"), pharmacyId));
    }

    public static Specification<Order> hasUserId(Integer userId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("orderOwner").get("id"), userId));
    }

    public static Specification<Order> hasEmail(String email) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("email"), email));
    }

    public static Specification<Order> hasPhone(String phone) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("phone"), phone));
    }

    public static Specification<Order> excludeCompleted(Boolean exclude) {
        if (exclude == null || !exclude) return null;
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("orderStatus"), OrderStatus.COMPLETED));
    }

    public static Specification<Order> excludeCancelled(Boolean exclude) {
    if (exclude == null || !exclude) return null;

    return (root, query, criteriaBuilder) -> {
        List<OrderStatus> excludedStatuses = Arrays.asList(
            OrderStatus.CANCELLED_USER,
            OrderStatus.CANCELLED_SYSTEM,
            OrderStatus.EXPIRED
        );

        return root.get("orderStatus").in(excludedStatuses).not();
    };
}
}
