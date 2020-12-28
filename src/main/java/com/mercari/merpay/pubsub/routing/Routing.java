package com.mercari.merpay.pubsub.routing;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.path;
import static spark.Spark.post;

import com.mercari.merpay.pubsub.exception.DuplicateRegistrationException;
import com.mercari.merpay.pubsub.exception.DuplicateSubscriptionException;
import com.mercari.merpay.pubsub.exception.ExhaustedTopicException;
import com.mercari.merpay.pubsub.exception.UnauthorizedPublisherException;
import com.mercari.merpay.pubsub.exception.UnregisteredTopicException;
import com.mercari.merpay.pubsub.exception.UnsubscribedTopicException;
import com.mercari.merpay.pubsub.filter.AcceptCharsetHeaderValidator;
import com.mercari.merpay.pubsub.filter.AcceptHeaderValidator;
import com.mercari.merpay.pubsub.filter.ContentLengthHeaderValidator;
import com.mercari.merpay.pubsub.filter.ContentTypeHeaderValidator;
import com.mercari.merpay.pubsub.filter.RequestBodyValidator;
import com.mercari.merpay.pubsub.filter.RequestParamValidator;
import com.mercari.merpay.pubsub.filter.UncaughtExceptionHandler;
import com.mercari.merpay.pubsub.routing.message.Ack;
import com.mercari.merpay.pubsub.routing.message.Get;
import com.mercari.merpay.pubsub.routing.message.Publish;
import com.mercari.merpay.pubsub.routing.topic.Register;
import com.mercari.merpay.pubsub.routing.topic.Subscribe;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes.Type;
import spark.Spark;

/**
 * Routing declarations. Filters, exception handlers, static and dynamic routes are declared here.
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Routing {

  private static final Logger LOGGER = LogManager.getLogger(Routing.class);
  public static final String SUPPORTED_MEDIA_TYPE = Type.APPLICATION_JSON.toString();
  public static final String SUPPORTED_CHARSET = StandardCharsets.UTF_8.toString();
  public static final String SUPPORTED_CONTENT_TYPE =
      String.format("%s;charset=%s", SUPPORTED_MEDIA_TYPE, SUPPORTED_CHARSET).toLowerCase(Locale.ENGLISH);
  public static final Predicate<String> FILE_NAME_PREDICATE = Pattern.compile("^[^\\s<>.?:;\"*|\\\\/]{1,127}$")
      .asMatchPredicate();
  public static final String TOPIC_PARAM = ":topic";

  @NonNull Function<Object, String> encoder;
  @NonNull Function<String, Object> decoder;
  @NonNull String staticDir;
  @NonNull String dataDir;
  int maxContentLength;

  public void register() {
    registerStaticRoutes();
    registerBeforeFilters();
    registerAfterFilters();
    registerTopicRoutes();
    registerMessageRoutes();
    registerErrorHandlingRoutes();
  }

  /**
   * Static files location must be configured before route mapping
   */
  private void registerStaticRoutes() {
    Spark.staticFileLocation(staticDir);
  }

  /**
   * Before-filters are evaluated before each request, and can read the request and read/modify the response
   */
  private void registerBeforeFilters() {
    before(
        (req, res) -> res.type(SUPPORTED_CONTENT_TYPE),
        new AcceptHeaderValidator(SUPPORTED_MEDIA_TYPE),
        new AcceptCharsetHeaderValidator(SUPPORTED_CHARSET)
    );
  }

  /**
   * After-filters are evaluated after each request, and can read the request and read/modify the response
   */
  private void registerAfterFilters() {
    // after((req, res) -> response.header("foo", "bar"));
  }

  /**
   * Matched in the order they are defined. The first route that matches the request is invoked.
   */
  private void registerTopicRoutes() {
    path("/topic", () -> {
      before(String.format("/*/%s", TOPIC_PARAM), new RequestParamValidator(TOPIC_PARAM, FILE_NAME_PREDICATE));
      path("/register", () -> post(String.format("/%s", TOPIC_PARAM),
          Register.builder()
              .topicParam(TOPIC_PARAM)
              .directory(dataDir)
              .build(),
          encoder::apply));
      path("/subscribe", () -> post(String.format("/%s", TOPIC_PARAM),
          Subscribe.builder()
              .topicParam(TOPIC_PARAM)
              .directory(dataDir)
              .build(),
          encoder::apply));
    });
  }

  /**
   * Matched in the order they are defined. The first route that matches the request is invoked.
   */
  private void registerMessageRoutes() {
    path("/message", () -> {
      before(String.format("/*/%s", TOPIC_PARAM), new RequestParamValidator(TOPIC_PARAM, FILE_NAME_PREDICATE));
      path("/publish", () -> {
        before("/*",
            new ContentTypeHeaderValidator(SUPPORTED_CONTENT_TYPE),
            new ContentLengthHeaderValidator(maxContentLength),
            new RequestBodyValidator(decoder));
        post(String.format("/%s", TOPIC_PARAM),
            Publish.builder()
                .topicParam(TOPIC_PARAM)
                .directory(dataDir)
                .decoder(decoder)
                .build(),
            encoder::apply);
      });
      path("/ack",
          () -> post(String.format("/%s", TOPIC_PARAM),
              Ack.builder()
                  .topicParam(TOPIC_PARAM)
                  .directory(dataDir)
                  .build(),
              encoder::apply));
      path("/get",
          () -> get(String.format("/%s", TOPIC_PARAM),
              Get.builder()
                  .topicParam(TOPIC_PARAM)
                  .directory(dataDir)
                  .build(),
              encoder::apply));
    });
  }

  private void registerErrorHandlingRoutes() {
    notFound((req, res) -> encoder.apply(Map.of("error", "Not Found")));
    internalServerError((req, res) -> encoder.apply(Map.of("error", "Internal Server Error")));
    exception(Exception.class, UncaughtExceptionHandler.builder()
        .consumer((e, req, res) -> LOGGER.error(req.contextPath(), e))
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        .encoder(encoder)
        .build());
    exception(DuplicateRegistrationException.class, UncaughtExceptionHandler.builder()
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.CONFLICT_409)
        .encoder(encoder)
        .build());
    exception(DuplicateSubscriptionException.class, UncaughtExceptionHandler.builder()
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.CONFLICT_409)
        .encoder(encoder)
        .build());
    exception(UnregisteredTopicException.class, UncaughtExceptionHandler.builder()
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.NOT_FOUND_404)
        .encoder(encoder)
        .build());
    exception(UnsubscribedTopicException.class, UncaughtExceptionHandler.builder()
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.FORBIDDEN_403)
        .encoder(encoder)
        .build());
    exception(UnauthorizedPublisherException.class, UncaughtExceptionHandler.builder()
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.UNAUTHORIZED_401)
        .encoder(encoder)
        .build());
    exception(ExhaustedTopicException.class, UncaughtExceptionHandler.builder()
        .contentType(SUPPORTED_CONTENT_TYPE)
        .status(HttpStatus.NO_CONTENT_204)
        .encoder(encoder)
        .build());
  }
}
