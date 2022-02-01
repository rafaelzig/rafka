package jp.rafaelzig.rafka.pubsub.filter;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Validator for HTTP Content-Length entity header.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ContentLengthHeaderValidator extends RequestValidator {

  public ContentLengthHeaderValidator(int maxBytes) {
    super(r -> r.contentLength() <= maxBytes, HttpStatus.PAYLOAD_TOO_LARGE_413);
  }
}
