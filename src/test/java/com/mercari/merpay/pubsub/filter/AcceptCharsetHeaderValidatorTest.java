package com.mercari.merpay.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mercari.merpay.pubsub.mock.MockRequest;
import com.mercari.merpay.pubsub.mock.MockResponse;
import com.mercari.merpay.pubsub.routing.Routing;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import spark.HaltException;
import spark.Request;

class AcceptCharsetHeaderValidatorTest {
  private static final MockResponse RESPONSE = MockResponse.builder().build();

  @Test
  void handleNoCharset() {
    // Given
    Request request = MockRequest.builder().build();

    // When
    Executable exec = () -> new AcceptCharsetHeaderValidator(Routing.SUPPORTED_CHARSET).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleEmptyCharset() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .headers(Map.of(HttpHeader.ACCEPT_CHARSET.toString(), ""))
        .build();

    // When
    Executable exec = () -> new AcceptCharsetHeaderValidator(Routing.SUPPORTED_CHARSET).handle(request, RESPONSE);

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
  void handleSameCharset() {
    // Given
    Request request = MockRequest.builder()
        .headers(Map.of(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET))
        .build();

    // When
    Executable exec = () -> new AcceptCharsetHeaderValidator(Routing.SUPPORTED_CHARSET).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleDifferentCharset() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .headers(Map.of(HttpHeader.ACCEPT_CHARSET.toString(), Type.TEXT_PLAIN.toString()))
        .build();
    MockResponse response = MockResponse.builder().build();

    // When
    Executable exec = () -> new AcceptCharsetHeaderValidator(Routing.SUPPORTED_CHARSET).handle(request, response);

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