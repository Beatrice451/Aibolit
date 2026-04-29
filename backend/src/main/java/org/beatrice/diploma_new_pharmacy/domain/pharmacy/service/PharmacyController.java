package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
@RequiredArgsConstructor
class PharmacyController {
    private final PharmacyService pharmacyService;

    @GetMapping
    public ResponseEntity<List<PharmacyResponse>> getPharmacies() {
        return ResponseEntity.ok(pharmacyService.getPharmacies());
    }
}
