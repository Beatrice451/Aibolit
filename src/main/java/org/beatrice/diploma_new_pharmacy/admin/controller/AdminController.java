package org.beatrice.diploma_new_pharmacy.admin.controller;


import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.admin.service.AdminService;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.command.UpdateOrderStatusCommand;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.UpdateOrderStatusRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.specification.OrderFilter;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.command.AddCategoryCommand;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddCategoryRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.UpdateProductRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.CategoryResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.mapper.ProductCommandRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final ProductCommandRequestMapper productCommandRequestMapper;

    @PostMapping("/product")
    public ResponseEntity<ProductResponse> addProduct(@RequestBody AddProductRequest request) {

        var cmd = productCommandRequestMapper.toAddProductCommand(request);

        return ResponseEntity.
                status(HttpStatus.CREATED).
                body(adminService.addProduct(cmd));
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/product/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer id,
            @RequestBody UpdateProductRequest request
    ) {

        var cmd = productCommandRequestMapper.toUpdateProductCommand(request);

        return ResponseEntity.ok(adminService.updateProduct(id, cmd));
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody AddCategoryRequest request) {

        var cmd = AddCategoryCommand.builder()
                .name(request.name())
                .parentId(request.parentId())
                .build();

        return ResponseEntity.ok(adminService.addCategory(cmd));
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(adminService.getCategories());
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getCategoryById(id));
    }

    @GetMapping("/order")
    public ResponseEntity<Page<OrderResponse>> getOrders(@ModelAttribute OrderFilter filter, Pageable pageable) {
        return ResponseEntity.ok(adminService.getOrdersByFilter(filter, pageable));
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getOrderById(id));
    }

    @PatchMapping("/order/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @RequestBody UpdateOrderStatusRequest request,
            @PathVariable Integer id
    ) {
        UpdateOrderStatusCommand cmd = new UpdateOrderStatusCommand(request.status());
        return ResponseEntity.ok(adminService.updateOrderStatus(id, cmd));
    }
}
