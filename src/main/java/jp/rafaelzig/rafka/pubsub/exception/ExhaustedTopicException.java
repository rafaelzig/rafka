package jp.rafaelzig.rafka.pubsub.exception;

import java.io.Serial;

public class ExhaustedTopicException extends Exception {

  @Serial
  private static final long serialVersionUID = -875089231171509892L;
  private static final String MESSAGE_FORMAT = "Topic '%s' has no more messages";

  public ExhaustedTopicException(String message, Throwable e) {
    super(String.format(MESSAGE_FORMAT, message), e);
  }
}
