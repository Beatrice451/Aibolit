package org.beatrice.diploma_new_pharmacy.product.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDto extends ProductDto {
     private Integer dosage;
     private Boolean requiresPrescription;
     private String form;
     private Short quantity;
}
