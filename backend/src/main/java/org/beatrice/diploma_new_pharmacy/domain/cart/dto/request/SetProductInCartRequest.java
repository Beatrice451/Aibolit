package org.beatrice.diploma_new_pharmacy.domain.cart.dto.request;

import jakarta.validation.constraints.Min;

public record SetProductInCartRequest(@Min(1) Short quantity) {
}
