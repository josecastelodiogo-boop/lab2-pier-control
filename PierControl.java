
public class PierControl {

    private static int totalOrdersCount = 0;
    private static double totalRevenue = 0.0;

    private static final double MAX_PAYMENT = 100_000_000.0;

    // ---------- Статические геттеры ----------

    public static int getTotalOrdersCount() {
        return totalOrdersCount;
    }

    public static double getTotalRevenue() {
        return totalRevenue;
    }

    public static void resetStats() {
        totalOrdersCount = 0;
        totalRevenue = 0.0;
    }

    //Контролируемая фиксация оплаты.
    public static void registerPayment(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Сумма оплаты должна быть конечным числом");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Сумма оплаты не может быть отрицательной: " + amount);
        }
        if (amount > MAX_PAYMENT) {
            throw new IllegalArgumentException(
                    "Сумма оплаты превышает максимум (" + MAX_PAYMENT + "): " + amount);
        }
        totalOrdersCount++;
        totalRevenue += amount;
    }
    public static void printGlobalStats() {
        System.out.println("=========================================");
        System.out.println("       ГЛОБАЛЬНАЯ СТАТИСТИКА      ");
        System.out.println("=========================================");
        System.out.printf("Общее число заказов : %d%n", totalOrdersCount);
        System.out.printf("Суммарная выручка   : %.2f руб.%n", totalRevenue);
        System.out.println("=========================================");
    }
    // ---------- Точка входа ----------

    public static void main(String[] args) {
        // 1. Создать экземпляр Option (LifeVest)
        LifeVest standardVest = new LifeVest("Стандартный жилет", 500.0);
        System.out.println();

        // 2. Создать не менее 3-х Target (Boat) разными конструкторами
        Boat singleBoat = new Boat("Каяк одноместный", 1000.0);
        Boat doubleBoat = new Boat("Лодка двухместная", 1500.0, "DOUBLE");
        Boat familyBoat = new Boat("Семейный катамаран", 2500.0, "FAMILY");

        // 3. Продемонстрировать три перегруженных варианта расчёта
        double price1 = singleBoat.calculateFinalPrice();
        System.out.printf("SINGLE, без жилета: %.2f руб.%n", price1);
        registerPayment(price1);

        double price2 = doubleBoat.calculateFinalPrice(standardVest);
        System.out.printf("DOUBLE + стандартный жилет: %.2f руб.%n", price2);
        registerPayment(price2);

        double price3 = familyBoat.calculateFinalPrice(standardVest, "XL");
        System.out.printf("FAMILY + жилет XL: %.2f руб.%n", price3);
        registerPayment(price3);

        System.out.println();

        // 4. Статистика обновлена вызовами registerPayment() выше

        // 5. Итоговый отчёт
        printGlobalStats();
    }
}