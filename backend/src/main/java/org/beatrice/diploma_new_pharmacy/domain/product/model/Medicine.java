package org.beatrice.diploma_new_pharmacy.domain.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "medicines", schema = "pharmacy")
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Medicine extends Product {
    @Column(name = "dosage", nullable = false)
    private Integer dosage;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "requires_prescription", nullable = false)
    private Boolean requiresPrescription = false;

    @Column(name = "form", nullable = false, length = Integer.MAX_VALUE)
    private String form;

    @Column(name = "quantity", nullable = false)
    private Short quantity;

}