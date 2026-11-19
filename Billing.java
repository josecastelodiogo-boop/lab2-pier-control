package org.sky.service.billing;

import org.sky.service.exception.*;
import org.sky.service.model.Boat;
import org.sky.service.service.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Billing {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final Map<String, Boat> database = new HashMap<>();
    private final NotificationService notificationService;

    public Billing(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Регистрация клиента: email — ключ в HashMap. */
    public void registerSubscription(String email, Boat boat)
            throws DuplicateSubscriptionException {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("Некорректный email: " + email);
        }
        if (database.containsKey(email)) {
            throw new DuplicateSubscriptionException(
                "Пользователь с email " + email + " уже зарегистрирован!");
        }
        database.put(email, boat);
    }

    /** Оплата по email. */
    public void processPaymentByEmail(String email)
            throws SubscriptionNotFoundException, InsufficientFundsException {
        Boat boat = database.get(email);
        if (boat == null) {
            throw new SubscriptionNotFoundException(
                "Пользователь с email " + email + " не зарегистрирован в SkyService!");
        }

        double finalPrice = boat.calculateBillingPrice();
        if (boat.getBalance() < finalPrice) {
            throw new InsufficientFundsException(String.format(
                "Недостаточно средств у %s: баланс %.2f, требуется %.2f",
                email, boat.getBalance(), finalPrice));
        }
        boat.withdraw(finalPrice);

        notificationService.sendNotification(
            String.format("Уважаемый(ая) %s, списание %.2f руб. прошло успешно. Остаток: %.2f руб.",
                boat.getOwnerName(), finalPrice, boat.getBalance()),
            email);
    }

    public int size() { return database.size(); }
}
