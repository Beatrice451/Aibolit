package org.beatrice.diploma_new_pharmacy.admin.controller;


import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.admin.dto.AddProductCommand;
import org.beatrice.diploma_new_pharmacy.admin.dto.AddProductRequest;
import org.beatrice.diploma_new_pharmacy.admin.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/product")
    public ResponseEntity<?> addProduct(@RequestBody AddProductRequest request) {
        var cmd = AddProductCommand.builder()
                .price(request.price())
                .name(request.name())
                .description(request.description())
                .manufacturer(request.manufacturer())
                .categoryName(request.categoryName())
                .build();

        return ResponseEntity.
                status(HttpStatus.CREATED).
                body(adminService.addProduct(cmd));
    }
}
