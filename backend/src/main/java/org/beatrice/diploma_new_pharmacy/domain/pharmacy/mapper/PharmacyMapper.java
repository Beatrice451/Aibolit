package org.beatrice.diploma_new_pharmacy.domain.pharmacy.mapper;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PharmacyMapper {
    PharmacyResponse toDto(Pharmacy pharmacy);

    void updateFromRequest(PharmacyRequest request, @MappingTarget Pharmacy entity);
}
