package org.beatrice.diploma_new_pharmacy.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Setter
@Getter
@Entity
@Table(name = "medicines", schema = "pharmacy")
public class Medicine {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    private Product products;

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