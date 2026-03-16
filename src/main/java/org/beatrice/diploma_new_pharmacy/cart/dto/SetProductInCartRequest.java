package org.beatrice.diploma_new_pharmacy.cart.dto;

import jakarta.validation.constraints.Min;

public record SetProductInCartRequest(@Min(1) Short quantity) {
}
