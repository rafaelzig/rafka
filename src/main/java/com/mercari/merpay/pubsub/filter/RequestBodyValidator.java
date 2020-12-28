package com.mercari.merpay.pubsub.filter;

import java.util.Objects;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Validator for HTTP request body.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class RequestBodyValidator extends RequestValidator {

  public RequestBodyValidator(Function<? super String, Object> decoder) {
    super(req -> Objects.nonNull(decoder.apply(req.body())), HttpStatus.BAD_REQUEST_400);
  }
}
