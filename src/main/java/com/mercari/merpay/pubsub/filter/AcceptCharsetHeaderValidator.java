package com.mercari.merpay.pubsub.filter;

import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;

/**
 * Validator for HTTP Accept-Charset request header.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AcceptCharsetHeaderValidator extends RequestValidator {

  public AcceptCharsetHeaderValidator(String supportedCharset) {
    super(getPredicate(supportedCharset), HttpStatus.NOT_ACCEPTABLE_406);
  }

  private static Predicate<Request> getPredicate(String supportedCharset) {
    return req -> {
      String header = req.headers(HttpHeader.ACCEPT_CHARSET.toString());
      return header == null || supportedCharset.equalsIgnoreCase(header);
    };
  }
}
