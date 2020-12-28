package com.mercari.merpay.pubsub.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mercari.merpay.pubsub.mock.MockRequest;
import com.mercari.merpay.pubsub.mock.MockResponse;
import java.util.function.Function;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import spark.HaltException;
import spark.Request;

class RequestBodyValidatorTest {

  private static final Function<String, Object> DECODER = json -> new Gson().fromJson(json, JsonObject.class);
  private static final MockResponse RESPONSE = MockResponse.builder().build();

  @Test
  void handleJson() {
    // Given
    JsonObject content = new JsonObject();
    content.addProperty("hello", "world");
    Request request = MockRequest.builder()
        .body(content.toString())
        .build();

    // When
    Executable exec = () -> new RequestBodyValidator(DECODER).handle(request, RESPONSE);

    // Then
    assertDoesNotThrow(exec);
  }

  @Test
  void handleNotJson() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .body("{hello world}")
        .build();

    // When
    Executable exec = () -> new RequestBodyValidator(DECODER).handle(request, RESPONSE);

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
  void handleNoContent() throws Throwable {
    // Given
    Request request = MockRequest.builder().build();

    // When
    Executable exec = () -> new RequestBodyValidator(DECODER).handle(request, RESPONSE);

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
  void handleEmptyContent() throws Throwable {
    // Given
    Request request = MockRequest.builder()
        .body("")
        .build();

    // When
    Executable exec = () -> new RequestBodyValidator(DECODER).handle(request, RESPONSE);

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
}