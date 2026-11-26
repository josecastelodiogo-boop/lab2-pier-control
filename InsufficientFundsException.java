package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Проверяемое: на балансе недостаточно средств. */
@StandardException
public class InsufficientFundsException extends Exception {
}
