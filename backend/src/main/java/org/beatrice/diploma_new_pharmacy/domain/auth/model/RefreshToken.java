package org.beatrice.diploma_new_pharmacy.domain.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "refresh_tokens", schema = "pharmacy")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "token", nullable = false, length = Integer.MAX_VALUE)
    private String token;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @NotNull
    @Column(name = "is_revoked", nullable = false)
    private Boolean revoked;

    @NotNull
    @Column(name = "token_family", nullable = false)
    private UUID tokenFamily;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by")
    private RefreshToken replacedBy;

    @NotNull
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Column(name = "revoke_reason")
    private String revokeReason;

}