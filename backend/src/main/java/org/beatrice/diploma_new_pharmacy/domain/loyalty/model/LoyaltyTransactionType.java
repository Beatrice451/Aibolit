package org.beatrice.diploma_new_pharmacy.domain.loyalty.model;

public enum LoyaltyTransactionType {
    ACCRUAL, // стандартное начисление баллов
    REDEMPTION, // списание
    EXPITARION, // сгорание баллов
    CORRECTION // ручная коррекция
}
