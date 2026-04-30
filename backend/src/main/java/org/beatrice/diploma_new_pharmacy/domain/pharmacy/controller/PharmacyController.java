package org.beatrice.diploma_new_pharmacy.domain.pharmacy.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.service.PharmacyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/pharmacies")
@RequiredArgsConstructor
class PharmacyController {


    private final PharmacyService pharmacyService;

    @GetMapping("/{id}")
    public ResponseEntity<PharmacyResponse> getPharmacy(@PathVariable Integer id) {
        return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
    }

    // TODO: придумать как по-нормальному обыграть это говно
    @GetMapping
    public ResponseEntity<List<PharmacyResponse>> getAllPharmacies(
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive,
            @AuthenticationPrincipal SecurityUser user
    ) {
        if (includeInactive && (user == null || !hasRole(user, "ADMIN"))) {
            throw new AccessDeniedException("Only admins can view inactive pharmacies");
        }

        List<PharmacyResponse> pharmacies = includeInactive
                ? pharmacyService.getAllPharmaciesIncludingInactive()
                : pharmacyService.getPharmacies();

        return ResponseEntity.ok(pharmacies);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacyResponse> addPharmacy(@RequestBody PharmacyRequest request) {
        var pharmacy = pharmacyService.createPharmacy(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{pharmacyId}")
                .buildAndExpand(pharmacy.id())
                .toUri();
        return ResponseEntity.created(location).body(pharmacy);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacyResponse> updatePharmacy(
            @PathVariable Integer id,
            @RequestBody PharmacyRequest request
    ) {
        return ResponseEntity.ok(pharmacyService.updatePharmacy(request, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePharmacy(@PathVariable Integer id) {
        pharmacyService.deletePharmacy(id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasRole(SecurityUser user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}
