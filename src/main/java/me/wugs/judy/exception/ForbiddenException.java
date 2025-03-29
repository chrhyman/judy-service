package me.wugs.judy.exception;

/**
 * This exception, when raised within the context of GlobalExceptionHandler, causes a Forbidden
 * response. It is otherwise identical to an RuntimeException
 */
public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }
}
