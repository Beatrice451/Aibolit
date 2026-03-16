package org.beatrice.diploma_new_pharmacy.cart.dto;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record CartIdentity(
        @Nullable Integer userId,
        @Nullable UUID guestUuid
) {
    public static CartIdentity forUser(Integer userId) {
        return new CartIdentity(userId, null);
    }

    public static CartIdentity forGuest(UUID guestUuid) {
        return new CartIdentity(null, guestUuid);
    }

    public boolean isUser() {
        return userId != null;
    }

    public boolean isGuest() {
        return guestUuid != null;
    }


}
