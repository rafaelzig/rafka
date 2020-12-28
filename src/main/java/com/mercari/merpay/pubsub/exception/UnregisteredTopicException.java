package com.mercari.merpay.pubsub.exception;

import java.io.Serial;

public class UnregisteredTopicException extends TopicNotFoundException {

  @Serial
  private static final long serialVersionUID = 3535975294048389364L;
  private static final String MESSAGE_FORMAT = "Topic '%s' is not registered";

  public UnregisteredTopicException(String message) {
    super(String.format(MESSAGE_FORMAT, message));
  }

  public UnregisteredTopicException(String message, Throwable cause) {
    super(String.format(MESSAGE_FORMAT, message), cause);
  }
}
