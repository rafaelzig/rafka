package com.mercari.merpay.pubsub.mock;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import spark.Response;

@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class MockResponse extends Response {

  private String type;
  private int status;

  @Override
  public void type(String contentType) {
    type = contentType;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public void status(int statusCode) {
    status = statusCode;
  }

  @Override
  public int status() {
    return status;
  }
}
