package org.sky.service.billing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sky.service.exception.DuplicateSubscriptionException;
import org.sky.service.exception.InsufficientFundsException;
import org.sky.service.exception.InvalidEmailException;
import org.sky.service.exception.PromoCodeExpiredException;
import org.sky.service.exception.SubscriptionNotFoundException;
import org.sky.service.model.Boat;
import org.sky.service.model.PromoCode;
import org.sky.service.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Реестр клиентов (HashMap по email), оплата и аналитика на Stream API. */
@Slf4j
@RequiredArgsConstructor
public class Billing {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final Map<String, Boat> database = new HashMap<>();
    private final NotificationService notificationService;

    // ---------------- ЛР3 ч1: регистрация и оплата ----------------

    public void registerSubscription(String email, Boat boat) throws DuplicateSubscriptionException {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("Некорректный email: " + email);
        }
        if (boat == null) {
            throw new IllegalArgumentException("Аренда не может быть null");
        }
        if (database.containsKey(email)) {
            throw new DuplicateSubscriptionException("Клиент с email " + email + " уже зарегистрирован!");
        }
        database.put(email, boat);
    }

    public void processPaymentByEmail(String email)
            throws SubscriptionNotFoundException, InsufficientFundsException, PromoCodeExpiredException {
        Boat boat = database.get(email);
        if (boat == null) {
            throw new SubscriptionNotFoundException("Пользователь с email " + email + " не зарегистрирован в SkyService!");
        }

        Optional<PromoCode> expired = boat.getAppliedPromos().stream()
                .filter(PromoCode::isExpired)
                .findFirst();
        if (expired.isPresent()) {
            throw new PromoCodeExpiredException("Промокод " + expired.get().getCode()
                    + " истёк " + expired.get().getExpirationDate());
        }

        double price = boat.calculateBillingPrice();
        if (boat.getBalance() < price) {
            throw new InsufficientFundsException(String.format(Locale.US,
                    "Недостаточно средств: баланс %.2f, требуется %.2f", boat.getBalance(), price));
        }
        boat.withdraw(price);
        notificationService.sendNotification(
                String.format(Locale.US, "Оплата %.2f руб. прошла успешно. Остаток: %.2f", price, boat.getBalance()),
                email);
    }

    public int size() {
        return database.size();
    }

    /** Email-ключи базы в алфавитном порядке (стабильный порядок для демонстрации). */
    public List<String> getEmails() {
        return database.keySet().stream().sorted().toList();
    }

    // ---------------- ЛР3 ч2: Stream API ----------------

    /** VIP-клиенты: итоговая стоимость строго больше minPrice. */
    public List<Boat> findVipClients(double minPrice) {
        return database.values().stream()
                .filter(boat -> boat.calculateBillingPrice() > minPrice)
                .toList();
    }

    /** Общая выручка по всей базе (с округлением до копеек). */
    public double calculateTotalRevenue() {
        double sum = database.values().stream()
                .mapToDouble(Boat::calculateBillingPrice)
                .sum();
        return Math.round(sum * 100.0) / 100.0;
    }

    /** Количество аренд заданного типа (IndividualBoatRental / CorporateBoatRental). */
    public long countByRentalType(Class<? extends Boat> rentalClass) {
        return database.values().stream()
                .filter(rentalClass::isInstance)
                .count();
    }

    /** Группировка по типу аренды: имя класса -> количество. */
    public Map<String, Long> groupCountByRentalType() {
        return database.values().stream()
                .collect(Collectors.groupingBy(boat -> boat.getClass().getSimpleName(),
                        TreeMap::new,
                        Collectors.counting()));
    }

    /** Выручка по типу аренды: имя класса -> сумма. */
    public Map<String, Double> revenueByRentalType() {
        return database.values().stream()
                .collect(Collectors.groupingBy(boat -> boat.getClass().getSimpleName(),
                        TreeMap::new,
                        Collectors.summingDouble(Boat::calculateBillingPrice)));
    }

    /** Оплата для всех клиентов базы (без циклов). */
    public PaymentSummary processAllPayments() {
        Map<Boolean, Long> result = database.keySet().stream()
                .collect(Collectors.partitioningBy(this::tryPay, Collectors.counting()));
        return new PaymentSummary(result.get(true), result.get(false));
    }

    private boolean tryPay(String email) {
        try {
            processPaymentByEmail(email);
            return true;
        } catch (InsufficientFundsException | SubscriptionNotFoundException | PromoCodeExpiredException e) {
            log.debug("Оплата не выполнена для {}: {}", email, e.getMessage());
            return false;
        }
    }
}
