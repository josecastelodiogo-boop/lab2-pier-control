package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Проверяемое: email уже есть в реестре. */
@StandardException
public class DuplicateSubscriptionException extends Exception {
}
