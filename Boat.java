package org.sky.service.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.sky.service.exception.InvalidSubscriptionException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Абстрактная аренда лодки / каяка (аналог Subscription из задания). */
@Getter
@ToString
public abstract class Boat {
    private static final Pattern EMPTY_PATTERN = Pattern.compile("^\\s*$");

    private final String ownerName;
    private final double basePrice;

    @Setter(AccessLevel.PROTECTED)
    private double balance;

    private final List<Option> appliedOptions = new ArrayList<>();
    private final List<PromoCode> appliedPromos = new ArrayList<>();

    protected Boat(String ownerName, double basePrice, double balance) {
        if (ownerName == null || EMPTY_PATTERN.matcher(ownerName).matches()) {
            throw new InvalidSubscriptionException("Имя владельца не может быть пустым");
        }
        if (basePrice <= 0) {
            throw new InvalidSubscriptionException("Базовая стоимость должна быть больше нуля: " + basePrice);
        }
        if (balance < 0) {
            throw new InvalidSubscriptionException("Баланс не может быть отрицательным: " + balance);
        }
        this.ownerName = ownerName;
        this.basePrice = basePrice;
        this.balance = balance;
    }

    public void addOption(Option option) {
        if (option == null) {
            throw new IllegalArgumentException("Опция не может быть null");
        }
        appliedOptions.add(option);
    }

    public void addPromo(PromoCode promo) {
        if (promo == null) {
            throw new IllegalArgumentException("Промокод не может быть null");
        }
        appliedPromos.add(promo);
    }

    public void withdraw(double amount) {
        setBalance(round2(balance - amount));
    }

    public abstract double calculateBillingPrice();

    /** Сумма цен всех опций. */
    protected double sumOptions() {
        return appliedOptions.stream().mapToDouble(Option::getPrice).sum();
    }

    /** Последовательно применяет все непросроченные промокоды. */
    protected double applyPromos(double price) {
        double factor = appliedPromos.stream()
                .filter(promo -> !promo.isExpired())
                .mapToDouble(promo -> 1 - promo.getDiscountPercent() / 100.0)
                .reduce(1.0, (a, b) -> a * b);
        return price * factor;
    }

    /** Округление денежной суммы до копеек. */
    protected static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
