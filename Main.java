package org.sky.service;

import org.sky.service.billing.Billing;
import org.sky.service.exception.*;
import org.sky.service.model.*;
import org.sky.service.service.NotificationService;
import org.sky.service.service.impl.EmailNotification;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Inicialização do serviço de notificação e do faturamento (Billing)
        NotificationService notificationService = new EmailNotification();
        Billing billing = new Billing(notificationService);

        System.out.println("=== Запуск демонстрации системы SkyService ===");

        // ---------- Cenário 1: Cadastro e pagamento com sucesso ----------
        System.out.println("\n--- Сценарий 1: Успешная оплата ---");
        try {
            // Criação de uma conta individual
            Boat b1 = new IndividualBoatRental("Иван Иванов", 500.0, 3000.0);
            b1.addOption(new Option("Спасательный жилет", 100.0));
            b1.addOption(new Option("Страховка", 150.0));
            b1.addPromo(new PromoCode("SUMMER10", 10.0, LocalDate.now().plusDays(30)));

            // Criação de uma conta corporativa
            Boat b2 = new CorporateBoatRental("ООО Вектор", 5000.0, 10000.0, 0.15);
            b2.addOption(new Option("Гид", 2000.0));
            b2.addPromo(new PromoCode("CORP20", 20.0, LocalDate.now().plusDays(60)));

            // Registro no banco de dados (HashMap)
            billing.registerSubscription("ivan@mail.ru", b1);
            billing.registerSubscription("corp@vector.com", b2);

            // Processamento do pagamento (sucesso)
            billing.processPaymentByEmail("ivan@mail.ru");
            billing.processPaymentByEmail("corp@vector.com");

        } catch (DuplicateSubscriptionException | SubscriptionNotFoundException | InsufficientFundsException e) {
            System.out.println("Ошибка при оплате: " + e.getMessage());
        } finally {
            System.out.println("[Блок finally] Сценарий 1 успешно завершен.");
        }

        // ---------- Cenário 2: Exceção verificada (Email não cadastrado) ----------
        System.out.println("\n--- Сценарий 2: Попытка оплаты несуществующего Email ---");
        try {
            billing.processPaymentByEmail("fake@mail.com");
        } catch (SubscriptionNotFoundException e) {
            System.out.println("[ПЕРЕХВАЧЕНО checked]: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("[ПЕРЕХВАЧЕНО checked]: " + e.getMessage());
        } finally {
            System.out.println("[Блок finally] Программа продолжает работу без сбоев.");
        }

        // ---------- Cenário 3: Exceção não verificada (Preço negativo) ----------
        System.out.println("\n--- Сценарий 3: Создание подписки с отрицательной стоимостью ---");
        try {
            new IndividualBoatRental("Петр Петров", -100.0, 1000.0);
        } catch (InvalidSubscriptionException e) {
            System.out.println("[ПЕРЕХВАЧЕНО unchecked]: " + e.getMessage());
        } finally {
            System.out.println("[Блок finally] Программа не завершилась аварийно.");
        }

        // ---------- Cenário 4: Exceção verificada (Email duplicado) ----------
        System.out.println("\n--- Сценарий 4: Попытка регистрации дубликата Email ---");
        try {
            Boat duplicate = new IndividualBoatRental("Иван Петров", 300.0, 1000.0);
            billing.registerSubscription("ivan@mail.ru", duplicate);
        } catch (DuplicateSubscriptionException e) {
            System.out.println("[ПЕРЕХВАЧЕНО checked]: " + e.getMessage());
        }

        // ---------- Cenário 5: Exceção não verificada (Código promocional inválido) ----------
        System.out.println("\n--- Сценарий 5: Создание некорректного промокода ---");
        try {
            new PromoCode("bad code!", 110, LocalDate.now().plusDays(1));
        } catch (InvalidPromoCodeException e) {
            System.out.println("[ПЕРЕХВАЧЕНО unchecked]: " + e.getMessage());
        }

        // ---------- Cenário 6: Exceção verificada (Código promocional expirado) ----------
        System.out.println("\n--- Сценарий 6: Применение истекшего промокода ---");
        try {
            PromoCode expired = new PromoCode("OLD2020", 50.0, LocalDate.now().minusDays(1));
            if (expired.isExpired()) {
                throw new PromoCodeExpiredException("Промокод " + expired.getCode() + " истек " + expired.getExpirationDate());
            }
        } catch (PromoCodeExpiredException e) {
            System.out.println("[ПЕРЕХВАЧЕНО checked]: " + e.getMessage());
        }

        // ---------- Cenário 7: Exceção verificada (Saldo insuficiente) ----------
        System.out.println("\n--- Сценарий 7: Недостаточно средств на балансе ---");
        try {
            Boat poor = new IndividualBoatRental("Бедный Клиент", 1000.0, 100.0);
            billing.registerSubscription("poor@mail.com", poor);
            billing.processPaymentByEmail("poor@mail.com");
        } catch (DuplicateSubscriptionException | SubscriptionNotFoundException e) {
            System.out.println("Системная ошибка: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("[ПЕРЕХВАЧЕНО checked]: " + e.getMessage());
        }

        System.out.println("\n=== Демонстрация завершена! Система работает стабильно. ===");
    }
}