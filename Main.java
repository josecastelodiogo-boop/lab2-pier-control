package org.sky.service;

import org.sky.service.billing.Billing;
import org.sky.service.exception.DuplicateSubscriptionException;
import org.sky.service.exception.InsufficientFundsException;
import org.sky.service.exception.InvalidPromoCodeException;
import org.sky.service.exception.InvalidSubscriptionException;
import org.sky.service.exception.PromoCodeExpiredException;
import org.sky.service.exception.SubscriptionNotFoundException;
import org.sky.service.model.Boat;
import org.sky.service.model.CorporateBoatRental;
import org.sky.service.model.IndividualBoatRental;
import org.sky.service.model.Option;
import org.sky.service.model.PromoCode;
import org.sky.service.service.impl.EmailNotification;
import org.sky.service.util.UserDataGenerator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Main {

    private static final int USERS_COUNT = 50;
    private static final double VIP_THRESHOLD = 5000.0;

    public static void main(String[] args) {
        Billing billing = new Billing(new EmailNotification());
        UserDataGenerator generator = new UserDataGenerator(2026L);

        generateUsers(billing, generator);
        streamAnalytics(billing);
        exceptionScenarios(billing, generator);

        System.out.println("\nПрограмма завершена без ошибок.");
    }

    // ======================================================================
    // ЭТАП 3: все клиенты генерируются Datafaker'ом (ручных объектов нет)
    // ======================================================================
    private static void generateUsers(Billing billing, UserDataGenerator generator) {
        title("Datafaker: генерация " + USERS_COUNT + " клиентов");
        int created = generator.populate(billing, USERS_COUNT);
        System.out.println("Сгенерировано и добавлено в HashMap: " + created
                + " (размер базы: " + billing.size() + ")");

        System.out.println("Первые 5 записей:");
        billing.getEmails().stream()
                .limit(5)
                .forEach(email -> System.out.println("  " + email));
    }

    // ======================================================================
    // ЭТАП 4: аналитика только через Stream API
    // ======================================================================
    private static void streamAnalytics(Billing billing) {
        title("Аналитика Stream API");

        System.out.println("Общая выручка (mapToDouble + sum): "
                + money(billing.calculateTotalRevenue()) + " руб.");

        System.out.println("\nКорпоративных аренд (filter + count): "
                + billing.countByRentalType(CorporateBoatRental.class));
        System.out.println("Индивидуальных аренд (filter + count): "
                + billing.countByRentalType(IndividualBoatRental.class));

        System.out.println("\nГруппировка по типу аренды (Collectors.groupingBy):");
        billing.groupCountByRentalType().forEach((type, cnt) ->
                System.out.println("  " + type + ": " + cnt + " клиентов"));

        System.out.println("\nВыручка по типу аренды:");
        billing.revenueByRentalType().forEach((type, sum) ->
                System.out.println("  " + type + ": " + money(sum) + " руб."));

        List<Boat> vip = billing.findVipClients(VIP_THRESHOLD);
        System.out.println("\nVIP-клиенты (чек > " + money(VIP_THRESHOLD) + "): найдено "
                + vip.size() + ". Топ-5:");
        vip.stream()
                .sorted(Comparator.comparingDouble(Boat::calculateBillingPrice).reversed())
                .limit(5)
                .forEach(b -> System.out.println("  " + b.getOwnerName()
                        + " [" + b.getClass().getSimpleName() + "] — " + money(b.calculateBillingPrice()) + " руб."));
    }

    // ======================================================================
    // Сценарии исключений ЛР3 ч1 — работают на сгенерированных клиентах.
    // Вручную создаются только заведомо НЕКОРРЕКТНЫЕ данные (они и должны падать).
    // ======================================================================
    private static void exceptionScenarios(Billing billing, UserDataGenerator generator) {
        title("Сценарии исключений (ЛР3 ч1)");
        List<String> emails = billing.getEmails();

        // Сценарий 1: успешная оплата двух сгенерированных клиентов (@Slf4j + уведомление)
        System.out.println("--- Сценарий 1: оплата существующих клиентов ---");
        try {
            billing.processPaymentByEmail(emails.get(0));
            billing.processPaymentByEmail(emails.get(1));
        } catch (SubscriptionNotFoundException | InsufficientFundsException | PromoCodeExpiredException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            System.out.println("[finally] Сценарий 1 завершён\n");
        }

        // Сценарий 2: SubscriptionNotFoundException (проверяемое)
        System.out.println("--- Сценарий 2: несуществующий email ---");
        try {
            billing.processPaymentByEmail("fake@mail.com");
        } catch (SubscriptionNotFoundException | InsufficientFundsException | PromoCodeExpiredException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            System.out.println("[finally] Программа продолжает работу\n");
        }

        // Сценарий 3: InvalidSubscriptionException (непроверяемое)
        System.out.println("--- Сценарий 3: отрицательная стоимость ---");
        try {
            IndividualBoatRental.builder()
                    .ownerName("Пётр").basePrice(-100.0).balance(500.0)
                    .build();
        } catch (InvalidSubscriptionException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        // Сценарий 4: дубликат — пытаемся зарегистрировать уже существующий email
        System.out.println("--- Сценарий 4: повторная регистрация ---");
        try {
            billing.registerSubscription(emails.get(0), generator.randomValidBoat());
        } catch (DuplicateSubscriptionException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        // Сценарий 5: некорректный промокод
        System.out.println("--- Сценарий 5: некорректный промокод ---");
        try {
            new PromoCode("bad code!", 110, LocalDate.now().plusDays(1));
        } catch (InvalidPromoCodeException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        // Сценарий 6: просроченный промокод обнаруживается при оплате
        System.out.println("--- Сценарий 6: просроченный промокод ---");
        try {
            Boat withOldPromo = generator.randomValidBoat();
            withOldPromo.addPromo(new PromoCode("OLD2020", 10, LocalDate.now().minusDays(1)));
            billing.registerSubscription("old@mail.ru", withOldPromo);
            billing.processPaymentByEmail("old@mail.ru");
        } catch (DuplicateSubscriptionException | SubscriptionNotFoundException
                 | InsufficientFundsException | PromoCodeExpiredException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        // Сценарий 7: недостаточно средств — сгенерированной аренде добавляем очень дорогую опцию
        System.out.println("--- Сценарий 7: недостаточно средств ---");
        try {
            Boat expensive = generator.randomValidBoat();
            expensive.addOption(new Option("Эксклюзивный тур", 1_000_000.0));
            billing.registerSubscription("poor@mail.ru", expensive);
            billing.processPaymentByEmail("poor@mail.ru");
        } catch (DuplicateSubscriptionException | SubscriptionNotFoundException
                 | InsufficientFundsException | PromoCodeExpiredException e) {
            System.out.println("Перехвачено " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void title(String text) {
        System.out.println("\n==================================================");
        System.out.println(text);
        System.out.println("==================================================");
    }

    private static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
