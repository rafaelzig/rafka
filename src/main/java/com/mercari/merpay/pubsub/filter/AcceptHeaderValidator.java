package com.mercari.merpay.pubsub.filter;

import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;

/**
 * Validator for HTTP Accept request header.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AcceptHeaderValidator extends RequestValidator {

  public AcceptHeaderValidator(String supportedMediaType) {
    super(getRequestPredicate(supportedMediaType), HttpStatus.NOT_ACCEPTABLE_406);
  }

  private static Predicate<Request> getRequestPredicate(String supportedMediaType) {
    return req -> {
      String header = req.headers(HttpHeader.ACCEPT.toString());
      return header == null || supportedMediaType.equalsIgnoreCase(header);
    };
  }
}
