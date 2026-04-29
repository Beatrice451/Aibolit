package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.mapper.PharmacyMapper;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.PharmacyRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapper pharmacyMapper;

    public Pharmacy getPharmacyById(Integer pharmacyId) {
        return pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new NotFoundException("Pharmacy with id " + pharmacyId + "not found"));
    }

    public List<PharmacyResponse> getPharmacies() {
        return pharmacyRepository.findAll()
                .stream().map(pharmacyMapper::toDto)
                .toList();
    }

}
