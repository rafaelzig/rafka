package com.mercari.merpay.pubsub.exception;

import java.io.Serial;

public class UnauthorizedPublisherException extends Exception {

  @Serial
  private static final long serialVersionUID = 3696727025266155399L;
  private static final String MESSAGE_FORMAT = "Topic '%s' was registered by another publisher";

  public UnauthorizedPublisherException(String message) {
    super(String.format(MESSAGE_FORMAT, message));
  }
}
