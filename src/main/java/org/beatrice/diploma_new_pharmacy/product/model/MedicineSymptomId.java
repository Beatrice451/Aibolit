package org.beatrice.diploma_new_pharmacy.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MedicineSymptomId implements Serializable {
    private static final long serialVersionUID = 5752570941188253713L;
    @Column(name = "medicine_id", nullable = false)
    private Integer medicineId;

    @Column(name = "symptom_id", nullable = false)
    private Integer symptomId;

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getSymptomId() {
        return symptomId;
    }

    public void setSymptomId(Integer symptomId) {
        this.symptomId = symptomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MedicineSymptomId entity = (MedicineSymptomId) o;
        return Objects.equals(this.medicineId, entity.medicineId) &&
                Objects.equals(this.symptomId, entity.symptomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicineId, symptomId);
    }

}