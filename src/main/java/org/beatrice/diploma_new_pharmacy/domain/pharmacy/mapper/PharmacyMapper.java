package org.beatrice.diploma_new_pharmacy.domain.pharmacy.mapper;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PharmacyMapper {
    PharmacyResponse toDto(Pharmacy pharmacy);
}
