package com.mercari.merpay.pubsub.routing.message;

import static org.eclipse.jetty.http.HttpStatus.OK_200;

import com.mercari.merpay.pubsub.data.Message;
import com.mercari.merpay.pubsub.data.Role;
import com.mercari.merpay.pubsub.exception.ExhaustedTopicException;
import com.mercari.merpay.pubsub.exception.UnsubscribedTopicException;
import com.mercari.merpay.pubsub.repository.MessageRepository;
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
