
public class Boat {

    // Коэффициенты тарифов (без «магических чисел»)
    private static final double COEF_SINGLE = 1.0;
    private static final double COEF_DOUBLE = 1.5;
    private static final double COEF_FAMILY = 2.0;

    // Надбавка за размер жилета XL
    private static final double XL_SURCHARGE = 1.2;

    private static final double MAX_BASE_COST = 10_000_000.0;

    // Допустимые тарифы и размеры (для валидации и switch)
    private static final java.util.Set<String> VALID_TARIFFS =
            java.util.Set.of("SINGLE", "DOUBLE", "FAMILY");
    private static final java.util.Set<String> VALID_SIZES =
            java.util.Set.of("S", "M", "L", "XL");

    private final String name;              // название (неизменяемое)
    private final double baseCost;          // базовая стоимость (неизменяемая)
    private final String tariff;            // тариф (неизменяемый)
    private LifeVest appliedVest = null;    // агрегация: по умолчанию отсутствует

    /**
     * Основной конструктор — принимает все параметры.
     *
     * @param name     название (не null и не пустое)
     * @param baseCost базовая стоимость (не отрицательная, конечная)
     * @param tariff   тариф ("SINGLE", "DOUBLE" или "FAMILY")
     * @throws IllegalArgumentException если параметр некорректен
     */
    public Boat(String name, double baseCost, String tariff) {
        this.name = validateName(name);
        this.baseCost = validateBaseCost(baseCost);
        this.tariff = validateTariff(tariff);
    }

    /**
     * Дополнительный конструктор — только имя и базовая стоимость.
     * Тариф по умолчанию: "SINGLE".
     *
     * @param name     название
     * @param baseCost базовая стоимость
     */
    public Boat(String name, double baseCost) {
        this(name, baseCost, "SINGLE");
    }

    // ---------- Валидация ----------

    private static String validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Название лодки не может быть null");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Название лодки не может быть пустым");
        }
        if (trimmed.length() > 120) {
            throw new IllegalArgumentException("Название лодки не может превышать 120 символов");
        }
        return trimmed;
    }

    private static double validateBaseCost(double baseCost) {
        if (Double.isNaN(baseCost) || Double.isInfinite(baseCost)) {
            throw new IllegalArgumentException("Базовая стоимость должна быть конечным числом");
        }
        if (baseCost < 0) {
            throw new IllegalArgumentException("Базовая стоимость не может быть отрицательной: " + baseCost);
        }
        if (baseCost > MAX_BASE_COST) {
            throw new IllegalArgumentException(
                    "Базовая стоимость превышает максимум (" + MAX_BASE_COST + "): " + baseCost);
        }
        return baseCost;
    }

    private static String validateTariff(String tariff) {
        if (tariff == null) {
            throw new IllegalArgumentException("Тариф не может быть null");
        }
        String upper = tariff.trim().toUpperCase();
        if (!VALID_TARIFFS.contains(upper)) {
            throw new IllegalArgumentException(
                    "Недопустимый тариф: '" + tariff + "'. Допустимые значения: " + VALID_TARIFFS);
        }
        return upper;
    }

    private static String validateSize(String size) {
        if (size == null) {
            throw new IllegalArgumentException("Размер жилета не может быть null");
        }
        String upper = size.trim().toUpperCase();
        if (!VALID_SIZES.contains(upper)) {
            throw new IllegalArgumentException(
                    "Недопустимый размер жилета: '" + size + "'. Допустимые значения: " + VALID_SIZES);
        }
        return upper;
    }

    private static void validateVest(LifeVest vest) {
        if (vest == null) {
            throw new IllegalArgumentException("Жилет не может быть null");
        }
    }

    // ---------- Геттеры ----------

    public String getName() {
        return name;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public String getTariff() {
        return tariff;
    }

    public LifeVest getAppliedVest() {
        return appliedVest;
    }

    // ---------- Перегруженные методы расчёта стоимости ----------

    /**
     * Вариант 1: расчёт стоимости на основе базовой цены и коэффициента тарифа.
     *
     * @return итоговая стоимость (всегда ≥ 0)
     */
    public double calculateFinalPrice() {
        double coefficient = switch (tariff) {
            case "SINGLE" -> COEF_SINGLE;
            case "DOUBLE" -> COEF_DOUBLE;
            case "FAMILY" -> COEF_FAMILY;
            default -> throw new IllegalStateException(
                    "Несогласованное состояние: сохранён недопустимый тариф: " + tariff);
        };
        return baseCost * coefficient;
    }

    /**
     * Вариант 2: связывает переданный жилет с текущей лодкой и возвращает суммарную стоимость.
     *
     * @param vest спасжилет (не null)
     * @return суммарная стоимость
     * @throws IllegalArgumentException если жилет null
     */
    public double calculateFinalPrice(LifeVest vest) {
        validateVest(vest);
        this.appliedVest = vest;
        return calculateFinalPrice() + vest.getCost();
    }

    /**
     * Вариант 3: связывает жилет и добавляет 20% при размере "XL".
     *
     * @param vest спасжилет (не null)
     * @param size размер ("S", "M", "L" или "XL")
     * @return суммарная стоимость с учётом надбавки за размер
     * @throws IllegalArgumentException если жилет null или размер некорректен
     */
    public double calculateFinalPrice(LifeVest vest, String size) {
        validateVest(vest);
        String normalizedSize = validateSize(size);
        this.appliedVest = vest;
        double vestCost = vest.getCost();
        if ("XL".equals(normalizedSize)) {
            vestCost *= XL_SURCHARGE;
        }
        return calculateFinalPrice() + vestCost;
    }

    @Override
    public String toString() {
        return String.format(
                "Boat{name='%s', baseCost=%.2f, tariff='%s', appliedVest=%s}",
                name, baseCost, tariff, appliedVest);
    }
}