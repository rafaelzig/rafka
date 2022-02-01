package jp.rafaelzig.rafka.pubsub.filter;

import java.util.Optional;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Validator for HTTP Content-Type entity header.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ContentTypeHeaderValidator extends RequestValidator {

  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  public ContentTypeHeaderValidator(String supportedContentType) {
    super(req -> Optional.ofNullable(req.contentType())
            .filter(contentType -> !contentType.isBlank())
            .map(WHITESPACE_PATTERN::matcher)
            .map(matcher -> matcher.replaceAll(""))
            .filter(supportedContentType::equalsIgnoreCase)
            .isPresent(),
        HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
  }
}
