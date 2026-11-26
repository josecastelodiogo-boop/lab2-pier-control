package org.sky.service.model;

import lombok.Getter;
import lombok.ToString;
import org.sky.service.exception.InvalidPromoCodeException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/** Промокод со скидкой в процентах и датой окончания. */
@Getter
@ToString
public class PromoCode {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]+$");

    private final String code;
    private final double discountPercent;
    private final LocalDate expirationDate;

    // Конструктор написан вручную, т.к. содержит валидацию (Lombok её не генерирует)
    public PromoCode(String code, double discountPercent, LocalDate expirationDate) {
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidPromoCodeException("Недопустимый код промокода: '" + code + "'");
        }
        if (discountPercent <= 0 || discountPercent > 100) {
            throw new InvalidPromoCodeException("Скидка должна быть в диапазоне (0; 100], получено: " + discountPercent);
        }
        if (expirationDate == null) {
            throw new InvalidPromoCodeException("Не задана дата окончания промокода");
        }
        this.code = code;
        this.discountPercent = discountPercent;
        this.expirationDate = expirationDate;
    }

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDate.now());
    }
}
