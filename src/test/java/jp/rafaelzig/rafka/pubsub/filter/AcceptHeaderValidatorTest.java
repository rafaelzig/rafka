package jp.rafaelzig.rafka.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jp.rafaelzig.rafka.pubsub.mock.MockRequest;
import jp.rafaelzig.rafka.pubsub.mock.MockResponse;
import jp.rafaelzig.rafka.pubsub.routing.Routing;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import spark.HaltException;
import spark.Request;

class AcceptHeaderValidatorTest {

  private static final MockResponse RESPONSE = MockResponse.builder().build();

  @Test
  void handleNoMediaType() {
    // Given
    Request request = MockRequest.builder().build();

    // When
    Executable exec = () -> new AcceptHeaderValidator(Routing.SUPPORTED_MEDIA_TYPE).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleEmptyMediaType() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .headers(Map.of(HttpHeader.ACCEPT.toString(), ""))
        .build();

    // When
    Executable exec = () -> new AcceptHeaderValidator(Routing.SUPPORTED_MEDIA_TYPE).handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.NOT_ACCEPTABLE_406);
    }
  }


  @Test
  void handleSameMediaType() {
    // Given
    Request request = MockRequest.builder()
        .headers(Map.of(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE))
        .build();

    // When
    Executable exec = () -> new AcceptHeaderValidator(Routing.SUPPORTED_MEDIA_TYPE).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleDifferentMediaType() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .headers(Map.of(HttpHeader.ACCEPT.toString(), Type.TEXT_PLAIN.toString()))
        .build();
    MockResponse response = MockResponse.builder().build();

    // When
    Executable exec = () -> new AcceptHeaderValidator(Routing.SUPPORTED_MEDIA_TYPE).handle(request, response);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.NOT_ACCEPTABLE_406);
    }
  }
}