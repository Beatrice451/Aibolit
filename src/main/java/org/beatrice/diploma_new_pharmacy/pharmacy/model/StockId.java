package org.beatrice.diploma_new_pharmacy.pharmacy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StockId implements Serializable {
    private static final long serialVersionUID = 4289904638780402480L;
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Integer warehouseId) {
        this.warehouseId = warehouseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        StockId entity = (StockId) o;
        return Objects.equals(this.productId, entity.productId) &&
                Objects.equals(this.warehouseId, entity.warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, warehouseId);
    }

}