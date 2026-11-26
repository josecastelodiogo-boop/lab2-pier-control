package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Непроверяемое: некорректный промокод. */
@StandardException
public class InvalidPromoCodeException extends RuntimeException {
}
