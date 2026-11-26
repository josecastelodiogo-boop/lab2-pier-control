package org.sky.service.util;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.sky.service.billing.Billing;
import org.sky.service.exception.DuplicateSubscriptionException;
import org.sky.service.exception.InvalidEmailException;
import org.sky.service.model.Boat;
import org.sky.service.model.CorporateBoatRental;
import org.sky.service.model.IndividualBoatRental;
import org.sky.service.model.Option;
import org.sky.service.model.PromoCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** ЭТАП 3: генерация большого числа реалистичных клиентов с помощью Datafaker. */
@Slf4j
public class UserDataGenerator {

    private static final Option[] OPTION_POOL = {
            new Option("Спасательный жилет", 100.0),
            new Option("Страховка", 150.0),
            new Option("Гид", 2000.0),
            new Option("GPS-трекер", 300.0),
            new Option("Водонепроницаемая камера", 250.0),
            new Option("Запасное весло", 80.0)
    };

    private static final String[] PROMO_CODES = {"SUMMER10", "WINTER15", "SKY5", "VIP20", "FRIEND7"};
    private static final double[] PROMO_PERCENTS = {10, 15, 5, 20, 7};

    private final Faker faker;

    /** @param seed зерно генератора: одинаковый seed -> одинаковые данные (удобно для демонстрации) */
    public UserDataGenerator(long seed) {
        this.faker = new Faker(new Random(seed));
    }

    /**
     * Генерирует и регистрирует ровно count уникальных клиентов в базе Billing (HashMap).
     *
     * @return количество созданных клиентов
     */
    public int populate(Billing billing, int count) {
        int created = 0;
        int attempts = 0;
        int maxAttempts = count * 20;
        while (created < count) {
            if (++attempts > maxAttempts) {
                throw new IllegalStateException("Не удалось сгенерировать " + count + " уникальных клиентов");
            }
            String email = faker.internet().emailAddress();
            try {
                billing.registerSubscription(email, randomBoat());
                created++;
            } catch (DuplicateSubscriptionException | InvalidEmailException e) {
                // дубликат или нестандартный email, пробуем ещё раз
                log.debug("Пропущен email {}: {}", email, e.getMessage());
            }
        }
        return created;
    }

    /** Случайная аренда; примерно каждый десятый промокод может быть просрочен. */
    public Boat randomBoat() {
        return randomBoat(true);
    }

    /** Случайная аренда только с действующими промокодами (для демонстрации сценариев). */
    public Boat randomValidBoat() {
        return randomBoat(false);
    }

    private Boat randomBoat(boolean allowExpiredPromos) {
        double basePrice = faker.number().numberBetween(300, 8000);
        double balance = faker.number().numberBetween(2000, 25000);
        List<Option> options = randomOptions();
        List<PromoCode> promos = randomPromos(allowExpiredPromos);

        boolean corporate = faker.number().numberBetween(0, 100) < 30;
        if (corporate) {
            var builder = CorporateBoatRental.builder()
                    .ownerName(faker.company().name())
                    .basePrice(basePrice)
                    .balance(balance)
                    .corporateDiscount(faker.number().numberBetween(5, 26) / 100.0);
            options.forEach(builder::option);
            promos.forEach(builder::promo);
            return builder.build();
        }
        var builder = IndividualBoatRental.builder()
                .ownerName(faker.name().fullName())
                .basePrice(basePrice)
                .balance(balance);
        options.forEach(builder::option);
        promos.forEach(builder::promo);
        return builder.build();
    }

    private List<Option> randomOptions() {
        int n = faker.number().numberBetween(0, 5);
        List<Option> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(faker.options().option(OPTION_POOL));
        }
        return result;
    }

    private List<PromoCode> randomPromos(boolean allowExpired) {
        int n = faker.number().numberBetween(0, 3);
        List<PromoCode> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int idx = faker.number().numberBetween(0, PROMO_CODES.length);
            // примерно каждый десятый промокод уже просрочен (он не влияет на цену)
            boolean expired = allowExpired && faker.number().numberBetween(0, 10) == 0;
            LocalDate expiration = expired
                    ? LocalDate.now().minusDays(5)
                    : LocalDate.now().plusDays(faker.number().numberBetween(10, 365));
            result.add(new PromoCode(PROMO_CODES[idx], PROMO_PERCENTS[idx], expiration));
        }
        return result;
    }
}
