package org.beatrice.diploma_new_pharmacy.domain.pharmacy.model;

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
public class StockId implements Serializable {
    @Serial
    private static final long serialVersionUID = 4289904638780402480L;
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;
}