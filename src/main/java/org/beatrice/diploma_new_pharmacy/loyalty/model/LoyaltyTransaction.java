package org.beatrice.diploma_new_pharmacy.loyalty.model;

import jakarta.persistence.*;
import org.beatrice.diploma_new_pharmacy.order.model.Order;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LoyaltyAccount getLoyaltyAccount() {
        return loyaltyAccount;
    }

    public void setLoyaltyAccount(LoyaltyAccount loyaltyAccount) {
        this.loyaltyAccount = loyaltyAccount;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(BigDecimal loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}