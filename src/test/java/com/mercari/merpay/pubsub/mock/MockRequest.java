package com.mercari.merpay.pubsub.mock;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import spark.Request;

@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class MockRequest extends Request {

  private final String body;
  private final int contentLength;
  private final String contentType;
  private final String ip;
  @Default
  private final Map<String, String> params = Collections.emptyMap();
  @Default
  private final Map<String, String> headers = Collections.emptyMap();

  @Override
  public String body() {
    return body;
  }

  @Override
  public int contentLength() {
    return contentLength;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public Map<String, String> params() {
    return Collections.unmodifiableMap(params);
  }

  @Override
  public String params(String param) {
    return params.get(param);
  }

  @Override
  public Set<String> headers() {
    return Collections.unmodifiableSet(headers.keySet());
  }

  @Override
  public String headers(String header) {
    return headers.get(header);
  }

  @Override
  public String ip() {
    return ip;
  }
}
