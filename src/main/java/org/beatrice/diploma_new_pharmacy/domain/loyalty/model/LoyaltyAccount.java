package org.beatrice.diploma_new_pharmacy.domain.loyalty.model;

import jakarta.persistence.*;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "loyalty_accounts", schema = "pharmacy")
public class LoyaltyAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private LoyaltyProgram program;

    @ColumnDefault("0")
    @Column(name = "current_points", nullable = false, precision = 9, scale = 2)
    private BigDecimal currentPoints;

    @ColumnDefault("0")
    @Column(name = "total_points_earned", nullable = false, precision = 9, scale = 2)
    private BigDecimal totalPointsEarned;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LoyaltyProgram getProgram() {
        return program;
    }

    public void setProgram(LoyaltyProgram program) {
        this.program = program;
    }

    public BigDecimal getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(BigDecimal currentPoints) {
        this.currentPoints = currentPoints;
    }

    public BigDecimal getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void setTotalPointsEarned(BigDecimal totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}