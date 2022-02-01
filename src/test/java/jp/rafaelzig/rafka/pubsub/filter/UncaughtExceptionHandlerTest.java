package jp.rafaelzig.rafka.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jp.rafaelzig.rafka.pubsub.mock.MockRequest;
import jp.rafaelzig.rafka.pubsub.mock.MockResponse;
import jp.rafaelzig.rafka.pubsub.routing.Routing;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

class UncaughtExceptionHandlerTest {

  @Test
  void handleWithExceptionMessage() {
    // Given
    Entry<String, String> expectedMessage = new SimpleEntry<>("hello", "world");
    Entry<String, String> actualMessage = new SimpleEntry<>("hello", "bar");
    String expectedErrorMessage = "hello error";
    Request request = MockRequest.builder().build();
    Response response = MockResponse.builder().build();
    int expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR_500;
    String expectedContentType = Routing.SUPPORTED_CONTENT_TYPE;
    ExceptionHandler<Exception> handler = UncaughtExceptionHandler.builder()
        .consumer((e, req, res) -> actualMessage.setValue("world"))
        .encoder(Object::toString)
        .contentType(expectedContentType)
        .status(expectedStatus)
        .build();

    // When
    handler.handle(new Exception(expectedErrorMessage), request, response);

    // Then
    assertEquals(expectedMessage, actualMessage);
    assertEquals(expectedContentType, response.type());
    assertEquals(expectedStatus, response.status());
    assertEquals(Map.of("error", expectedErrorMessage).toString(), response.body());
  }

  @Test
  void handleWithoutExceptionMessage() {
    // Given
    Entry<String, String> expectedMessage = new SimpleEntry<>("hello", "world");
    Entry<String, String> actualMessage = new SimpleEntry<>("hello", "bar");
    Request request = MockRequest.builder().build();
    Response response = MockResponse.builder().build();
    int expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR_500;
    String expectedContentType = Routing.SUPPORTED_CONTENT_TYPE;
    ExceptionHandler<Exception> handler = UncaughtExceptionHandler.builder()
        .consumer((e, req, res) -> actualMessage.setValue("world"))
        .encoder(Object::toString)
        .contentType(expectedContentType)
        .status(expectedStatus)
        .build();

    // When
    handler.handle(new Exception(), request, response);

    // Then
    assertEquals(expectedMessage, actualMessage);
    assertEquals(expectedContentType, response.type());
    assertEquals(expectedStatus, response.status());
    assertEquals(Map.of("error", Exception.class.getSimpleName()).toString(), response.body());
  }
}