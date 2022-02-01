package jp.rafaelzig.rafka.pubsub.exception;

import java.io.Serial;

public class UnsubscribedTopicException extends TopicNotFoundException {

  @Serial
  private static final long serialVersionUID = 3535975294048389364L;
  private static final String MESSAGE_FORMAT = "Topic '%s' is not subscribed";

  public UnsubscribedTopicException(String message, Throwable cause) {
    super(String.format(MESSAGE_FORMAT, message), cause);
  }
}
