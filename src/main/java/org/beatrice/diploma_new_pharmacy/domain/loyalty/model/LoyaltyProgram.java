package org.beatrice.diploma_new_pharmacy.domain.loyalty.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loyalty_programs", schema = "pharmacy")
public class LoyaltyProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "points_per_rub", nullable = false, precision = 5, scale = 2)
    private BigDecimal pointsPerRub;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPointsPerRub() {
        return pointsPerRub;
    }

    public void setPointsPerRub(BigDecimal pointsPerRub) {
        this.pointsPerRub = pointsPerRub;
    }

}