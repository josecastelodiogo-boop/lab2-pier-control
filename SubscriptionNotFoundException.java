package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Проверяемое: аренда с таким email не найдена в реестре. */
@StandardException
public class SubscriptionNotFoundException extends Exception {
}
