
public class LifeVest {

    private static final String DEFAULT_NAME = "Жилет без названия";
    private static final double MAX_COST = 1_000_000.0;

    private final String name;      // наименование жилета (неизменяемое)
    private double cost;            // базовая стоимость

    /**
     * Полный конструктор.
     */
    public LifeVest(String name, double cost) {
        this.name = validateName(name);
        setCost(cost);
    }

    /**
     * Удобный конструктор — создаёт жилет с названием по умолчанию.
     *
     * @param cost базовая стоимость
     */
    public LifeVest(double cost) {
        this(DEFAULT_NAME, cost);
    }

    // ---------- Валидация ----------

    private static String validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Название жилета не может быть null");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Название жилета не может быть пустым");
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Название жилета не может превышать 100 символов");
        }
        return trimmed;
    }

    private static void validateCost(double cost) {
        if (Double.isNaN(cost) || Double.isInfinite(cost)) {
            throw new IllegalArgumentException("Стоимость жилета должна быть конечным числом");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Стоимость жилета не может быть отрицательной: " + cost);
        }
        if (cost > MAX_COST) {
            throw new IllegalArgumentException(
                    "Стоимость жилета превышает максимум (" + MAX_COST + "): " + cost);
        }
    }

    // ---------- Геттеры / Сеттеры ----------

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    /**
     * Сеттер со строгой валидацией.
     *
     * @param cost новая стоимость
     * @throws IllegalArgumentException если стоимость некорректна
     */
    public void setCost(double cost) {
        validateCost(cost);
        this.cost = cost;
    }

    @Override
    public String toString() {
        return String.format("LifeVest{name='%s', cost=%.2f}", name, cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LifeVest)) return false;
        LifeVest other = (LifeVest) o;
        return Double.compare(other.cost, cost) == 0 && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, cost);
    }
}