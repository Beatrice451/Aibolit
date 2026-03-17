package org.beatrice.diploma_new_pharmacy.domain.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Setter
@Getter
@Entity
@Table(name = "medicine_active_substances", schema = "pharmacy")
public class MedicineActiveSubstance {
    @EmbeddedId
    private MedicineActiveSubstanceId id;

    @MapsId("medicineId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @MapsId("substanceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false)
    private ActiveSubstance substance;

}