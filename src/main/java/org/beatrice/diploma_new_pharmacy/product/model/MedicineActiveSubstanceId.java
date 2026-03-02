package org.beatrice.diploma_new_pharmacy.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@EqualsAndHashCode
@Embeddable
public class MedicineActiveSubstanceId implements Serializable {
    @Serial
    private static final long serialVersionUID = 9031812477411948780L;
    @Column(name = "medicine_id", nullable = false)
    private Integer medicineId;

    @Column(name = "substance_id", nullable = false)
    private Integer substanceId;


}