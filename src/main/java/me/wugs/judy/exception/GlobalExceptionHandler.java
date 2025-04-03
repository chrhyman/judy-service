package me.wugs.judy.exception;

import me.wugs.judy.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Defines ExceptionHandlers to gracefully handle them and provide relevant information to the user
 * when possible.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Transforms BadRequestException into a BadRequest with the message in the body
   *
   * @param ex the Exception to handle
   * @return BadRequest ResponseEntity
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponseDto("Bad Request", ex.getMessage()));
  }

  /**
   * Transforms ForbiddenException into a Forbidden response with the message in the body
   *
   * @param ex the Exception to handle
   * @return Forbidden ResponseEntity
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponseDto> handleForbiddenException(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponseDto("Forbidden", ex.getMessage()));
  }

  /**
   * Transforms UnauthorizedException into an Unauthorized response with the message in the body
   *
   * @param ex the Exception to handle
   * @return Unauthorized ResponseEntity
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponseDto> handleUnauthorizedException(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponseDto("Unauthorized", ex.getMessage()));
  }

  /**
   * Transforms all other exceptions into an InternalServerError. The message is not sent to the
   * user, as it may contain unexpected information that may be too sensitive to leak.
   *
   * @param ex the Exception to handle
   * @return InternalServerError ResponseEntity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponseDto("Internal Server Error", "An unexpected error occurred."));
  }
}
