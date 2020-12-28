package com.mercari.merpay.pubsub.filter;

import static spark.Spark.halt;

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Validator for HTTP requests.
 */
@Getter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RequestValidator implements Filter {

  private final @NonNull Predicate<Request> predicate;
  private final int status;

  @Override
  public void handle(Request request, Response response) {
    try {
      if (!predicate.test(request)) {
        halt(status);
      }
    } catch (Exception e) {
      halt(status);
    }
  }
}
