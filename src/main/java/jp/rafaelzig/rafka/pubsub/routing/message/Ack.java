package jp.rafaelzig.rafka.pubsub.routing.message;

import jp.rafaelzig.rafka.pubsub.data.Role;
import jp.rafaelzig.rafka.pubsub.data.Topic;
import jp.rafaelzig.rafka.pubsub.exception.ExhaustedTopicException;
import jp.rafaelzig.rafka.pubsub.exception.UnsubscribedTopicException;
import jp.rafaelzig.rafka.pubsub.repository.MessageRepository;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ack implements Route {

  @NonNull String directory;
  @NonNull String topicParam;

  @Override
  public Object handle(Request request, Response response)
      throws IOException, UnsubscribedTopicException, ExhaustedTopicException {
    Topic topic = MessageRepository.builder()
        .dataDir(directory)
        .topicName(request.params(topicParam))
        .principal(request.ip())
        .role(Role.SUBSCRIBER)
        .build()
        .advanceToNextPublication();
    response.status(HttpStatus.CREATED_201);
    return topic;
  }
}
