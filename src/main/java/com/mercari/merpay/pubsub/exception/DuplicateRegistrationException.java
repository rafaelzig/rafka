package com.mercari.merpay.pubsub.exception;

import java.io.Serial;

public class DuplicateRegistrationException extends Exception {

  @Serial
  private static final long serialVersionUID = 8507371238299931257L;
  private static final String MESSAGE_FORMAT = "Topic '%s' is already registered";

  public DuplicateRegistrationException(String message, Throwable cause) {
    super(String.format(MESSAGE_FORMAT, message), cause);
  }
}
