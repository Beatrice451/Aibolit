package org.beatrice.diploma_new_pharmacy.domain.order.specification;

import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

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
}
