package org.sky.service.model;

import org.sky.service.exception.InvalidSubscriptionException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Абстрактный класс аренды лодки/каяка.
 * Содержит коллекции опций и промокодов (ArrayList).
 */
public abstract class Boat {
    private static final Pattern EMPTY_PATTERN = Pattern.compile("^\\s*$");

    private final String ownerName;
    private final double basePrice;
    private double balance;

    private final List<Option> appliedOptions = new ArrayList<>();
    private final List<PromoCode> appliedPromos = new ArrayList<>();

    protected Boat(String ownerName, double basePrice, double balance) {
        if (basePrice <= 0) {
            throw new InvalidSubscriptionException(
                "Базовая стоимость должна быть больше нуля! Передано: " + basePrice);
        }
        if (ownerName == null || EMPTY_PATTERN.matcher(ownerName).matches()) {
            throw new InvalidSubscriptionException("Имя владельца не может быть пустым!");
        }
        if (balance < 0) {
            throw new InvalidSubscriptionException("Баланс не может быть отрицательным!");
        }
        this.ownerName = ownerName;
        this.basePrice = basePrice;
        this.balance = balance;
    }

    public void addOption(Option option) {
        if (option == null) {
            throw new IllegalArgumentException("Нельзя добавить null-опцию!");
        }
        appliedOptions.add(option);
    }

    public void addPromo(PromoCode promo) {
        if (promo == null) {
            throw new IllegalArgumentException("Нельзя добавить null-промокод!");
        }
        appliedPromos.add(promo);
    }

    public String getOwnerName() { return ownerName; }
    public double getBasePrice() { return basePrice; }
    public double getBalance() { return balance; }
    public List<Option> getAppliedOptions() { return appliedOptions; }
    public List<PromoCode> getAppliedPromos() { return appliedPromos; }

    public void withdraw(double amount) {
        this.balance -= amount;
    }

    /** Расчет итоговой цены — реализуется наследниками. */
    public abstract double calculateBillingPrice();

    /** Общий помощник: сумма всех опций. */
    protected double sumOptions() {
        double sum = 0;
        for (Option o : appliedOptions) {
            sum += o.getPrice();
        }
        return sum;
    }

    /** Общий помощник: применить все действующие промокоды последовательно. */
    protected double applyPromos(double price) {
        double result = price;
        for (PromoCode p : appliedPromos) {
            if (!p.isExpired()) {
                result -= result * (p.getDiscountPercent() / 100.0);
            }
        }
        return result;
    }
}
