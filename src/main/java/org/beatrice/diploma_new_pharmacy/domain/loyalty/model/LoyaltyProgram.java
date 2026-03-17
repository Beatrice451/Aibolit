package org.beatrice.diploma_new_pharmacy.domain.loyalty.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
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

}