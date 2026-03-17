package org.beatrice.diploma_new_pharmacy.domain.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Setter
@Getter
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

}