package jp.rafaelzig.rafka.pubsub.exception;

import java.io.Serial;

public class DuplicateSubscriptionException extends Exception {

  @Serial
  private static final long serialVersionUID = -6506733722428238443L;
  private static final String MESSAGE_FORMAT = "Topic '%s' is already subscribed";

  public DuplicateSubscriptionException(String message, Throwable cause) {
    super(String.format(MESSAGE_FORMAT, message), cause);
  }
}
