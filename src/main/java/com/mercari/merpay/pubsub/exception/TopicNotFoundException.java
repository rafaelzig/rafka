package com.mercari.merpay.pubsub.exception;

import java.io.Serial;

public class TopicNotFoundException extends Exception {

  @Serial
  private static final long serialVersionUID = -2175687870684714031L;

  public TopicNotFoundException(String message) {
    super(message);
  }
  public TopicNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
