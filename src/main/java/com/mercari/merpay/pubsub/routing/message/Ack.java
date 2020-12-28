package com.mercari.merpay.pubsub.routing.message;

import com.mercari.merpay.pubsub.data.Role;
import com.mercari.merpay.pubsub.data.Topic;
import com.mercari.merpay.pubsub.exception.ExhaustedTopicException;
import com.mercari.merpay.pubsub.exception.UnsubscribedTopicException;
import com.mercari.merpay.pubsub.repository.MessageRepository;
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
