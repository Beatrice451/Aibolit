package org.beatrice.diploma_new_pharmacy.domain.cart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;

@Setter
@Getter
@Entity
@Table(name = "cart_items", schema = "pharmacy")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Short quantity;

}