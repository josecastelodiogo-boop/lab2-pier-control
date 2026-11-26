package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Непроверяемое: некорректные данные аренды (цена <= 0, пустое имя и т.д.). */
@StandardException
public class InvalidSubscriptionException extends RuntimeException {
}
