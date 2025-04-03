package me.wugs.judy.exception;

/**
 * This exception, when raised within the context of GlobalExceptionHandler, causes an Unauthorized
 * response. It is otherwise identical to a RuntimeException
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
