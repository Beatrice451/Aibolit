package org.beatrice.diploma_new_pharmacy.product.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "medicine_symptoms", schema = "pharmacy")
public class MedicineSymptom {
    @EmbeddedId
    private MedicineSymptomId id;

    @MapsId("medicineId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @MapsId("symptomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptom symptom;

    public MedicineSymptomId getId() {
        return id;
    }

    public void setId(MedicineSymptomId id) {
        this.id = id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Symptom getSymptom() {
        return symptom;
    }

    public void setSymptom(Symptom symptom) {
        this.symptom = symptom;
    }

}