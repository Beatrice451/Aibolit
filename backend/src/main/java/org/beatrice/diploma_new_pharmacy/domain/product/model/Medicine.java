package org.beatrice.diploma_new_pharmacy.domain.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Setter
@Getter
@Entity
@Table(name = "medicines", schema = "pharmacy")
public class Medicine extends Product {
    @Column(name = "dosage", nullable = false)
    private Integer dosage;

    @ColumnDefault("false")
    @Column(name = "requires_prescription", nullable = false)
    private Boolean requiresPrescription = false;

    @Column(name = "form", nullable = false, length = Integer.MAX_VALUE)
    private String form;

    @Column(name = "quantity", nullable = false)
    private Short quantity;

}