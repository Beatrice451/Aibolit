package org.beatrice.diploma_new_pharmacy.domain.pharmacy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;

@Setter
@Getter
@Entity
@Table(name = "stocks", schema = "pharmacy")
public class Stock {
    @EmbeddedId
    private StockId id;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @MapsId("warehouseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

}