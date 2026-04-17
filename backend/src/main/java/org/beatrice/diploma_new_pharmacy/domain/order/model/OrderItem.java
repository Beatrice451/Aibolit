package org.beatrice.diploma_new_pharmacy.domain.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;

import java.math.BigDecimal;

@Builder
@Setter
@Getter
@Entity
@Table(name = "order_items", schema = "pharmacy")
@NoArgsConstructor
@AllArgsConstructor
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