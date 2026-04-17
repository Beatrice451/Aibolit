package org.beatrice.diploma_new_pharmacy.domain.order.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderStatus {
    NEW, // заказ создан, сборка не начата
    ASSEMBLING, // сборка заказа
    READY, // готов к выдаче
    DELIVERY_PENDING, // ожидание доставки со склада в аптеку
    DELIVERY_DELAYED, // доставка со склада задерживается
    COMPLETED, // завершен
    CANCELLED_USER, // отменен пользователем
    CANCELLED_SYSTEM, // отменен системой
    EXPIRED; // истёк (не забрали заказ например)

    @JsonCreator
    public static OrderStatus from(String value) {
        return OrderStatus.valueOf(value.toUpperCase());
    }
}
