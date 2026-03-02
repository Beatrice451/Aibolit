package org.beatrice.diploma_new_pharmacy.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.product.model.Product;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "order_items", schema = "pharmacy")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Short quantity;

    @Column(name = "price_at_sale", nullable = false, precision = 9, scale = 2)
    private BigDecimal priceAtSale;

}