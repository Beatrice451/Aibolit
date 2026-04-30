package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.mapper.PharmacyMapper;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.PharmacyRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyMapper pharmacyMapper;

    public Pharmacy getPharmacyEntityById(Integer pharmacyId) {
        return pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new NotFoundException("Pharmacy with id " + pharmacyId + " not found"));
    }

    public PharmacyResponse getPharmacyById(Integer pharmacyId) {
        return pharmacyMapper.toDto(getPharmacyEntityById(pharmacyId));
    }

    public List<PharmacyResponse> getPharmacies() {
        return pharmacyRepository.findByIsActiveTrue()
                .stream().map(pharmacyMapper::toDto)
                .toList();
    }

    public List<PharmacyResponse> getAllPharmaciesIncludingInactive() {
        return pharmacyRepository.findAll()
                .stream().map(pharmacyMapper::toDto)
                .toList();
    }

    public PharmacyResponse createPharmacy(PharmacyRequest request) {
        var pharmacy = new Pharmacy(request.phoneNumber(), request.address(), request.name());
        pharmacy.setIsActive(request.isActive() != null ? request.isActive() : true);
        return pharmacyMapper.toDto(pharmacyRepository.save(pharmacy));
    }

    public void deletePharmacy(Integer pharmacyId) {
        var pharmacy = getPharmacyEntityById(pharmacyId);
        pharmacy.setIsActive(false);
        pharmacyRepository.save(pharmacy);
    }

    public PharmacyResponse updatePharmacy(PharmacyRequest request, Integer pharmacyId) {
        var pharmacy = getPharmacyEntityById(pharmacyId);
        pharmacyMapper.updateFromRequest(request, pharmacy);
        return pharmacyMapper.toDto(pharmacy);
    }
}
