package org.sky.service.exception;

import lombok.experimental.StandardException;

/** Непроверяемое: email не соответствует формату. */
@StandardException
public class InvalidEmailException extends RuntimeException {
}
