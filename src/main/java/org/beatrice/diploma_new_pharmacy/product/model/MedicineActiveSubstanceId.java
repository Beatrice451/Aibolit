package org.beatrice.diploma_new_pharmacy.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MedicineActiveSubstanceId implements Serializable {
    @Serial
    private static final long serialVersionUID = 9031812477411948780L;
    @Column(name = "medicine_id", nullable = false)
    private Integer medicineId;

    @Column(name = "substance_id", nullable = false)
    private Integer substanceId;

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getSubstanceId() {
        return substanceId;
    }

    public void setSubstanceId(Integer substanceId) {
        this.substanceId = substanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MedicineActiveSubstanceId entity = (MedicineActiveSubstanceId) o;
        return Objects.equals(this.medicineId, entity.medicineId) &&
                Objects.equals(this.substanceId, entity.substanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicineId, substanceId);
    }

}