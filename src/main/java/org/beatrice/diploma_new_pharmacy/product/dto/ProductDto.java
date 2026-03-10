package org.beatrice.diploma_new_pharmacy.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String description;
    private String manufacturer;
    private Integer categoryId;
    private String categoryName;
}

