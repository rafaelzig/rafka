package jp.rafaelzig.rafka.pubsub.filter;

import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Validator for spark.Route path parameters.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class RequestParamValidator extends RequestValidator {

  public RequestParamValidator(String param, Predicate<? super String> predicate) {
    super(req -> predicate.test(req.params(param)), HttpStatus.BAD_REQUEST_400);
  }
}
