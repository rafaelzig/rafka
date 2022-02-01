package jp.rafaelzig.rafka.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jp.rafaelzig.rafka.pubsub.mock.MockRequest;
import jp.rafaelzig.rafka.pubsub.mock.MockResponse;
import jp.rafaelzig.rafka.pubsub.routing.Routing;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import spark.HaltException;
import spark.Request;

class RequestParamValidatorTest {

  private static final MockResponse RESPONSE = MockResponse.builder().build();

  @Test
  void handleNoFilename() throws Throwable {
    // Given
    Request request = MockRequest.builder().build();

    // When
    Executable exec = () -> new RequestParamValidator(Routing.TOPIC_PARAM, Routing.FILE_NAME_PREDICATE)
        .handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.BAD_REQUEST_400);
    }
  }

  @Test
  void handleEmptyFilename() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .params(Map.of(Routing.TOPIC_PARAM, ""))
        .build();

    // When
    Executable exec = () -> new RequestParamValidator(Routing.TOPIC_PARAM, Routing.FILE_NAME_PREDICATE)
        .handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.BAD_REQUEST_400);
    }
  }

  @Test
  void handleSmallFilename() {
    // Given
    Request request = MockRequest.builder()
        .params(Map.of(Routing.TOPIC_PARAM, "f"))
        .build();

    // When
    Executable exec = () -> new RequestParamValidator(Routing.TOPIC_PARAM, Routing.FILE_NAME_PREDICATE)
        .handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleBigFilename() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .params(Map.of(Routing.TOPIC_PARAM,
            IntStream.range(0, Byte.MAX_VALUE + 1)
                .boxed()
                .map(i -> String.valueOf(i % 10))
                .collect(Collectors.joining())))
        .build();

    // When
    Executable exec = () -> new RequestParamValidator(Routing.TOPIC_PARAM, Routing.FILE_NAME_PREDICATE)
        .handle(request, RESPONSE);

    // Then
    assertThrows(HaltException.class, exec);

    try {
      // When
      exec.execute();
    } catch (HaltException e) {
      // Then
      assertEquals(e.statusCode(), HttpStatus.BAD_REQUEST_400);
    }
  }

  @Test
  void handleGoodFilename() {
    // Given
    Request request = MockRequest.builder()
        .params(Map.of(Routing.TOPIC_PARAM, "foo"))
        .build();

    // When
    Executable exec = () -> new RequestParamValidator(Routing.TOPIC_PARAM, Routing.FILE_NAME_PREDICATE)
        .handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }
}