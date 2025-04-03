package me.wugs.judy.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import me.wugs.judy.enums.RateLimitedEndpoint;
import me.wugs.judy.exception.TooManyRequestsException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RateLimitingInterceptor {

  private final ConcurrentMap<String, Bucket> registrationBuckets = new ConcurrentHashMap<>();

  private Bucket getBucket(String key, RateLimitedEndpoint endpoint) {
    return switch (endpoint) {
      case RateLimitedEndpoint.register ->
          registrationBuckets.computeIfAbsent(
              key,
              k ->
                  Bucket.builder()
                      .addLimit(
                          Bandwidth.builder()
                              .capacity(1)
                              .refillIntervally(1, Duration.ofMinutes(5))
                              .build())
                      .build());
    };
  }

  public void checkRateLimit(String ip, RateLimitedEndpoint endpoint) {
    Bucket bucket = getBucket(ip, endpoint);
    if (!bucket.tryConsume(1)) {
      throw new TooManyRequestsException("Rate limit exceeded for your request type.");
    }
  }
}
