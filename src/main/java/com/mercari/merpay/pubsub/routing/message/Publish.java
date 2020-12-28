package com.mercari.merpay.pubsub.routing.message;

import com.mercari.merpay.pubsub.data.Message;
import com.mercari.merpay.pubsub.data.Role;
import com.mercari.merpay.pubsub.exception.UnauthorizedPublisherException;
import com.mercari.merpay.pubsub.exception.UnregisteredTopicException;
import com.mercari.merpay.pubsub.repository.MessageRepository;
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
