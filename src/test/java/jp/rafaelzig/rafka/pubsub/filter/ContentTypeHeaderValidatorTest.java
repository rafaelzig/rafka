package jp.rafaelzig.rafka.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jp.rafaelzig.rafka.pubsub.mock.MockRequest;
import jp.rafaelzig.rafka.pubsub.mock.MockResponse;
import jp.rafaelzig.rafka.pubsub.routing.Routing;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import spark.HaltException;
import spark.Request;

class ContentTypeHeaderValidatorTest {

  private static final MockResponse RESPONSE = MockResponse.builder().build();

  @Test
  void handleNoContentType() throws Throwable {
    // Given
    Request request = MockRequest.builder().build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
    }
  }

  @Test
  void handleEmptyContentType() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .contentType("")
        .build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
    }
  }

  @Test
  void handleSameContentType() {
    // Given
    Request request = MockRequest.builder()
        .contentType(Routing.SUPPORTED_CONTENT_TYPE)
        .build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleSameContentWithUpperCase() {
    // Given
    Request request = MockRequest.builder()
        .contentType(Routing.SUPPORTED_CONTENT_TYPE.toUpperCase(Locale.ENGLISH))
        .build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleSameContentWithLowerCase() {
    // Given
    Request request = MockRequest.builder()
        .contentType(Routing.SUPPORTED_CONTENT_TYPE.toLowerCase(Locale.ENGLISH))
        .build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleSameContentTypeWithWhitespace() {
    // Given
    String delimeter = ";";
    Request request = MockRequest.builder()
        .contentType(Arrays.stream(Routing.SUPPORTED_CONTENT_TYPE.split(delimeter))
            .map(str -> "   " + str + "   ")
            .collect(Collectors.joining(delimeter)))
        .build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleDifferentContentType() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .contentType(Type.TEXT_PLAIN.toString())
        .build();

    // When
    Executable exec = () -> new ContentTypeHeaderValidator(Routing.SUPPORTED_CONTENT_TYPE).handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
    }
  }
}