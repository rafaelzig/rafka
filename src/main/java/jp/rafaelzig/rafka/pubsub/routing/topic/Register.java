package jp.rafaelzig.rafka.pubsub.routing.topic;

import jp.rafaelzig.rafka.pubsub.data.Role;
import jp.rafaelzig.rafka.pubsub.data.Topic;
import jp.rafaelzig.rafka.pubsub.exception.DuplicateRegistrationException;
import jp.rafaelzig.rafka.pubsub.repository.TopicRepository;
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
public class Register implements Route {

  @NonNull String directory;
  @NonNull String topicParam;

  @Override
  public Object handle(Request request, Response response) throws IOException, DuplicateRegistrationException {
    Topic topic = TopicRepository.builder()
        .dataDir(directory)
        .topicName(request.params(topicParam))
        .principal(request.ip())
        .role(Role.PUBLISHER)
        .build()
        .createRegistration();
    response.status(HttpStatus.CREATED_201);
    return topic;
  }
}
