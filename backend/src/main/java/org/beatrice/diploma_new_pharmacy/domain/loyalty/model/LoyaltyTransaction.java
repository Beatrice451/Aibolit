package org.beatrice.diploma_new_pharmacy.domain.loyalty.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "loyalty_transactions", schema = "pharmacy")
public class LoyaltyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    private LoyaltyAccount loyaltyAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LoyaltyTransactionType loyaltyTransactionType;

    @Column(name = "loyalty_points", nullable = false, precision = 9, scale = 2)
    private BigDecimal loyaltyPoints;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}