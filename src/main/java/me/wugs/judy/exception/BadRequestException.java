package me.wugs.judy.exception;

/**
 * This exception, when raised within the context of GlobalExceptionHandler, causes a BadRequest
 * response. It is otherwise identical to an RuntimeException
 */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
