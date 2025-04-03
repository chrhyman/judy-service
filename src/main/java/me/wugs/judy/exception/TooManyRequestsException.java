package me.wugs.judy.exception;

/**
 * This exception, when raised within the context of GlobalExceptionHandler, causes a
 * TooManyRequests response. It is otherwise identical to a RuntimeException
 */
public class TooManyRequestsException extends RuntimeException {
  public TooManyRequestsException(String message) {
    super(message);
  }
}
