package org.beatrice.diploma_new_pharmacy.domain.order.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "order_owners", schema = "pharmacy")
public class OrderOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderOwnerType ownerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_uuid")
    private UUID guestUuid;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone", length = 32)
    private String guestPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
