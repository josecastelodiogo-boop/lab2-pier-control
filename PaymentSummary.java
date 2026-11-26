package org.sky.service.billing;

/** Итог массовой оплаты. */
public record PaymentSummary(long paid, long failed) {
}
