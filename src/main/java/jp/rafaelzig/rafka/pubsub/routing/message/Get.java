package jp.rafaelzig.rafka.pubsub.routing.message;

import static org.eclipse.jetty.http.HttpStatus.OK_200;

import jp.rafaelzig.rafka.pubsub.data.Message;
import jp.rafaelzig.rafka.pubsub.data.Role;
import jp.rafaelzig.rafka.pubsub.exception.ExhaustedTopicException;
import jp.rafaelzig.rafka.pubsub.exception.UnsubscribedTopicException;
import jp.rafaelzig.rafka.pubsub.repository.MessageRepository;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import spark.Request;
import spark.Response;
import spark.Route;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Get implements Route {

  @NonNull String directory;
  @NonNull String topicParam;

  @Override
  public Object handle(Request request, Response response)
      throws IOException, UnsubscribedTopicException, ExhaustedTopicException {
    Message message = MessageRepository.builder()
        .dataDir(directory)
        .topicName(request.params(topicParam))
        .principal(request.ip())
        .role(Role.SUBSCRIBER)
        .build()
        .fetchCurrentPublication();
    response.status(OK_200);
    return message;
  }
}
