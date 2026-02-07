package org.beatrice.diploma_new_pharmacy.product.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    public MedicineActiveSubstanceId getId() {
        return id;
    }

    public void setId(MedicineActiveSubstanceId id) {
        this.id = id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public ActiveSubstance getSubstance() {
        return substance;
    }

    public void setSubstance(ActiveSubstance substance) {
        this.substance = substance;
    }

}