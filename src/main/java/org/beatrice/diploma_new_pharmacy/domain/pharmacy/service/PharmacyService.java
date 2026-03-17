package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.PharmacyRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    public Optional<Pharmacy> getPharmacyById(Integer pharmacyId) {
        return pharmacyRepository.findById(pharmacyId);
    }
}
