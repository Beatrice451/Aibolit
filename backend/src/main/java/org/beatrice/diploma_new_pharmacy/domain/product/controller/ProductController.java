package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddReviewRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.MedicineResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ProductService;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ReviewService;
import org.beatrice.diploma_new_pharmacy.domain.product.specification.ProductFilter;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Товары", description = "Эндпоинты для получения информации о товарах и отзывах")
class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @Operation(
            summary = "Получить список товаров",
            description = "Возвращает страницу товаров с возможностью фильтрации. Поддерживается пагинация через параметры page, size и sort."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список товаров получен", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @ModelAttribute ProductFilter filter,
            Pageable pageable
    ) {
        Page<ProductResponse> products = productService.getProductsByFilter(filter, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Получить все лекарства",
            description = "Возвращает список всех лекарств (товаров с категорией 'Лекарства')."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список лекарств получен", content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(productService.getAllMedicines());
    }

    @Operation(
            summary = "Получить товар по ID",
            description = "Возвращает информацию о конкретном товаре по его идентификатору."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар найден", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(
            summary = "Получить отзывы о товаре",
            description = "Возвращает страницу отзывов для конкретного товара. Поддерживается пагинация."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список отзывов получен", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(id, pageable));
    }

    @Operation(
            summary = "Добавить отзыв о товаре",
            description = "Добавляет отзыв к товару. Требуется аутентификация. Пользователь может оставить только один отзыв к товару."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Отзыв успешно добавлен", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь не аутентифицирован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь уже оставлял отзыв к этому товару", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Integer id, @RequestBody AddReviewRequest request, @AuthenticationPrincipal
            SecurityUser user
    ) {
        ReviewResponse review = reviewService.addReview(user.user().getId(), id, request.comment(), request.rating());
        URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{reviewId}")
        .buildAndExpand(review.reviewId())
        .toUri();
        return ResponseEntity.created(location).body(review);
    }

}
