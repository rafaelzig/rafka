package jp.rafaelzig.rafka.pubsub.routing.message;

import jp.rafaelzig.rafka.pubsub.data.Message;
import jp.rafaelzig.rafka.pubsub.data.Role;
import jp.rafaelzig.rafka.pubsub.exception.UnauthorizedPublisherException;
import jp.rafaelzig.rafka.pubsub.exception.UnregisteredTopicException;
import jp.rafaelzig.rafka.pubsub.repository.MessageRepository;
import java.io.IOException;
import java.util.function.Function;
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
public class Publish implements Route {

  @NonNull String directory;
  @NonNull String topicParam;
  @NonNull Function<String, Object> decoder;

  @Override
  public Object handle(Request request, Response response)
      throws IOException, UnauthorizedPublisherException, UnregisteredTopicException {
    String content = decoder.apply(request.body()).toString();
    Message message = MessageRepository.builder()
        .dataDir(directory)
        .topicName(request.params(topicParam))
        .principal(request.ip())
        .role(Role.PUBLISHER)
        .build()
        .createPublication(content);
    response.status(HttpStatus.CREATED_201);
    return message;
  }
}
