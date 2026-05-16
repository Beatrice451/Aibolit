package org.beatrice.diploma_new_pharmacy.domain.pharmacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.service.PharmacyService;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
@RequiredArgsConstructor
@Tag(name = "Аптеки", description = "Управление аптеками")
class PharmacyController {

    private final PharmacyService pharmacyService;

    @GetMapping
    @Operation(
            summary = "Получить список аптек",
            description = "Возвращает список активных аптек. Если указан параметр includeInactive=true — возвращает все аптеки (требуется роль ADMIN)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список аптек получен", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (includeInactive=true, но не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PharmacyResponse>> getAllPharmacies(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        List<PharmacyResponse> pharmacies = includeInactive
                ? pharmacyService.getAllPharmaciesIncludingInactive()
                : pharmacyService.getPharmacies();
        return ResponseEntity.ok(pharmacies);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить аптеку по ID",
            description = "Возвращает информацию о конкретной аптеке."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аптека найдена", content = @Content(schema = @Schema(implementation = PharmacyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Аптека с указанным ID не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PharmacyResponse> getPharmacy(@PathVariable Integer id) {
        return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать аптеку",
            description = "Создает новую аптеку. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Аптека успешно создана", content = @Content(schema = @Schema(implementation = PharmacyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (не указаны обязательные поля)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PharmacyResponse> addPharmacy(@Valid @RequestBody PharmacyRequest request) {
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
    @Operation(
            summary = "Обновить аптеку",
            description = "Частично обновляет данные аптеки. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аптека обновлена", content = @Content(schema = @Schema(implementation = PharmacyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Аптека с указанным ID не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PharmacyResponse> updatePharmacy(
            @PathVariable Integer id,
            @Valid @RequestBody PharmacyRequest request
    ) {
        return ResponseEntity.ok(pharmacyService.updatePharmacy(request, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Деактивировать аптеку",
            description = "Помечает аптеку как неактивную (без физического удаления). Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Аптека деактивирована"),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Аптека с указанным ID не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePharmacy(@PathVariable Integer id) {
        pharmacyService.deletePharmacy(id);
        return ResponseEntity.noContent().build();
    }
}
