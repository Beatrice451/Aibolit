package org.beatrice.diploma_new_pharmacy.cart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.order.model.OrderOwner;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "carts", schema = "pharmacy")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_owner_id", nullable = false)
    private OrderOwner orderOwner;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    @CreationTimestamp
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}