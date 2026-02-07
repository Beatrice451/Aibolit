package org.beatrice.diploma_new_pharmacy.order.model;


import jakarta.persistence.*;
import org.beatrice.diploma_new_pharmacy.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_owners", schema = "pharmacy")
public class OrderOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OrderOwnerType ownerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone", length = 32)
    private String guestPhone;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
