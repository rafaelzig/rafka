package com.mercari.merpay.pubsub.filter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import org.apache.logging.log4j.util.TriConsumer;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

/**
 * Generic exception handler for custom logic.
 *
 * @param <E> Type of exception.
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UncaughtExceptionHandler<E extends Exception> implements ExceptionHandler<E> {

  @Default
  TriConsumer<E, Request, Response> consumer = (e, request, response) -> {
  };
  @NonNull Function<Object, String> encoder;
  @NonNull String contentType;
  int status;

  @Override
  public void handle(E exception, Request request, Response response) {
    consumer.accept(exception, request, response);
    response.type(contentType);
    response.status(status);
    response.body(encoder.apply(
        Map.of("error", Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().getSimpleName()))));
  }
}
