package jp.rafaelzig.rafka.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jp.rafaelzig.rafka.pubsub.mock.MockRequest;
import jp.rafaelzig.rafka.pubsub.mock.MockResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import spark.HaltException;
import spark.Request;

class ContentLengthHeaderValidatorTest {

  private static final MockResponse RESPONSE = MockResponse.builder().build();

  @Test
  void handleEmptyContentLengthAllowed() {
    // Given
    Request request = MockRequest.builder()
        .contentLength(0)
        .build();

    // When
    Executable exec = () -> new ContentLengthHeaderValidator(request.contentLength()).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleSmallContent() {
    // Given
    Request request = MockRequest.builder()
        .contentLength(0)
        .build();

    // When
    Executable exec = () -> new ContentLengthHeaderValidator(request.contentLength() + 1).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleSameContent() {
    // Given
    Request request = MockRequest.builder()
        .contentLength(1)
        .build();

    // When
    Executable exec = () -> new ContentLengthHeaderValidator(request.contentLength()).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleBigContent() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .contentLength(2)
        .build();

    // When
    Executable exec = () -> new ContentLengthHeaderValidator(request.contentLength() - 1).handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.PAYLOAD_TOO_LARGE_413);
    }
  }
}