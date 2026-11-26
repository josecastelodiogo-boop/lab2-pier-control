package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Проверяемое: срок действия промокода истёк. */
@StandardException
public class PromoCodeExpiredException extends Exception {
}
