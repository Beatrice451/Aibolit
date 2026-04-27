package org.beatrice.diploma_new_pharmacy.domain.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Entity
@Getter
@Setter
@Table(name = "orders", schema = "pharmacy")
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_owner_id", nullable = false)
    private OrderOwner orderOwner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderStatus orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount", precision = 11, scale = 2)
    private BigDecimal discount;

    @Column(name = "final_amount", precision = 11, scale = 2, insertable = false, updatable = false)
    private BigDecimal finalAmount;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "pickup_code", length = 6)
    private String pickupCode;

    @Column(name = "pickup_code_generated_at")
    private Instant pickupCodeGeneratedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;


}