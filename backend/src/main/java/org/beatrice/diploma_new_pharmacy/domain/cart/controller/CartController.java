package org.beatrice.diploma_new_pharmacy.domain.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.AddProductToCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.SetProductInCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.service.CartService;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Корзина", description = "Эндпоинты для работы с корзиной покупок")
class CartController {


    private final CartService cartService;

    @Operation(
            summary = "Получить корзину",
            description = "Возвращает текущее содержимое корзины пользователя или гостя (по sessionId)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно получена", content = @Content(schema = @Schema(implementation = CartResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart(OrderIdentity identity) {
        CartResponse response = cartService.getCartResponse(identity);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Добавить товар в корзину",
            description = "Добавляет товар в корзину или увеличивает его количество, если товар уже в корзине. quantity должно быть >= 1."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар добавлен в корзину", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (quantity < 1)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным productId не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CartResponse> addItemToCart(@Valid @RequestBody AddProductToCartRequest request, OrderIdentity identity) {
        var response = cartService.addItemToCart(identity, request.productId(), request.quantity());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Установить количество товара",
            description = "Устанавливает точное количество товара в корзине. quantity должно быть >= 1."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Количество товара обновлено", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (quantity < 1)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар с указанным productId не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{productId}")
    public ResponseEntity<?> setItemInCart(@PathVariable Integer productId, @Valid @RequestBody SetProductInCartRequest request, OrderIdentity identity) {
        var response = cartService.setItemInCart(identity, productId, request.quantity());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Очистить корзину",
            description = "Удаляет все товары из корзины пользователя или гостя."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно очищена")
    })
    @DeleteMapping
    public ResponseEntity<?> deleteCart(OrderIdentity identity) {
        cartService.truncateCart(identity);
        return ResponseEntity.ok().build();
    }
}
