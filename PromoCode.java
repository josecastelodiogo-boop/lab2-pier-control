package org.sky.service.model;

import org.sky.service.exception.InvalidPromoCodeException;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class PromoCode {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]+$");

    private final String code;
    private final double discountPercent;
    private final LocalDate expirationDate;

    public PromoCode(String code, double discountPercent, LocalDate expirationDate) {
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidPromoCodeException(
                "Некорректный промокод: '" + code + "'. Разрешены только заглавные латинские буквы и цифры, без пробелов.");
        }
        if (discountPercent <= 0 || discountPercent > 100) {
            throw new InvalidPromoCodeException(
                "Скидка должна быть в диапазоне (0; 100]. Передано: " + discountPercent);
        }
        if (expirationDate == null) {
            throw new InvalidPromoCodeException("Дата истечения промокода не задана.");
        }
        this.code = code;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
    }

    public String getCode() { return code; }
    public double getDiscountPercent() { return discountPercent; }
    public LocalDate getExpirationDate() { return expirationDate; }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    @Override
    public String toString() {
        return code + " (-" + discountPercent + "%, до " + expirationDate + ")";
    }
}
