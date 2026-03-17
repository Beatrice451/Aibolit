package org.beatrice.diploma_new_pharmacy.domain.order.dto;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record OrderIdentity(
        @Nullable Integer userId,
        @Nullable UUID guestUuid
) {
    public static OrderIdentity forUser(Integer userId) {
        return new OrderIdentity(userId, null);
    }

    public static OrderIdentity forGuest(UUID guestUuid) {
        return new OrderIdentity(null, guestUuid);
    }

    public boolean isUser() {
        return userId != null;
    }

    public boolean isGuest() {
        return guestUuid != null;
    }


}
