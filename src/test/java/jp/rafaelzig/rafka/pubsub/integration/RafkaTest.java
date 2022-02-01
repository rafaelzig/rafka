package jp.rafaelzig.rafka.pubsub.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jp.rafaelzig.rafka.pubsub.Rafka;
import jp.rafaelzig.rafka.pubsub.data.Message;
import jp.rafaelzig.rafka.pubsub.data.Topic;
import jp.rafaelzig.rafka.pubsub.routing.Routing;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import spark.Spark;

@TestMethodOrder(OrderAnnotation.class)
class RafkaTest {

  private static final Path STATIC_PATH = Path.of("src", "test", "resources", "static", "health");
  private static final Gson GSON = new Gson();
  private static final Topic TOPIC = new Topic("foo", 0);
  private static final Message MESSAGE;

  static {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("Hello", "World");
    MESSAGE = new Message(TOPIC.getName(), jsonObject.toString());
  }

  private static Path dataDir;

  @BeforeAll
  public static void setUp() throws IOException {
    Rafka rafka = new Rafka();
    rafka.listenAndServe();
    Spark.awaitInitialization();
    dataDir = Path.of(rafka.getProperties().getProperty("data.directory"));
    deleteTestDir();
    Files.createDirectory(dataDir);
  }

  @AfterAll
  public static void tearDown() throws IOException {
    Spark.stop();
    deleteTestDir();
  }

  private static void deleteTestDir() throws IOException {
    try {
      Files.deleteIfExists(dataDir);
    } catch (DirectoryNotEmptyException e) {
      Files.walk(dataDir)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  private static void write(OutputStream stream, String content) throws IOException {
    try (OutputStream out = stream;
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
      writer.write(content);
    }
  }

  private static String readLines(InputStream stream) throws IOException {
    StringBuilder builder = new StringBuilder();
    try (InputStream in = stream;
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader)) {
      appendLines(builder, bufferedReader);
    }
    return builder.toString();
  }

  private static String readFile(Path path) throws IOException {
    StringBuilder builder = new StringBuilder();
    try (BufferedReader bufferedInputStream = Files.newBufferedReader(path)) {
      appendLines(builder, bufferedInputStream);
    }
    return builder.toString();
  }

  private static void appendLines(StringBuilder builder, BufferedReader bufferedReader) throws IOException {
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      builder.append(line);
    }
  }

  @Test
  @Order(1)
  void healthCheck() throws IOException {
    String mediaType = "application/octet-stream";
    URL url = new URL("http://localhost/health");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.GET.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), mediaType);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.OK_200;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = GSON.fromJson(readFile(STATIC_PATH), JsonObject.class);
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(con.getContentType(), mediaType);
  }

  @Test
  @Order(2)
  void subscribeToNonExistentTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/topic/subscribe/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.NOT_FOUND_404;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("error", String.format("Topic '%s' is not registered", TOPIC.getName()));
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getErrorStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(3)
  void publishMessageToNonExistentTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/message/publish/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setDoOutput(true);
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.CONTENT_TYPE.toString(), Routing.SUPPORTED_CONTENT_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    write(con.getOutputStream(), MESSAGE.getContent());
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.NOT_FOUND_404;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("error", String.format("Topic '%s' is not registered", TOPIC.getName()));
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getErrorStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(4)
  void registerNewTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/topic/register/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.CREATED_201;
    assertEquals(expectedResponseCode, actualResponseCode);
    String expectedResponseBody = GSON.toJson(TOPIC);
    String actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class).toString();
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(5)
  void registerDuplicateTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/topic/register/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.CONFLICT_409;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("error", String.format("Topic '%s' is already registered", TOPIC.getName()));
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getErrorStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(6)
  void getNonSubscribedTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/message/get/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.GET.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.FORBIDDEN_403;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("error", String.format("Topic '%s' is not subscribed", TOPIC.getName()));
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getErrorStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(7)
  void ackNonSubscribedTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/message/ack/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.FORBIDDEN_403;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("error", String.format("Topic '%s' is not subscribed", TOPIC.getName()));
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getErrorStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(8)
  void newSubscription() throws IOException {
    URL url = new URL(String.format("http://localhost/topic/subscribe/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.CREATED_201;
    assertEquals(expectedResponseCode, actualResponseCode);
    String expectedResponseBody = GSON.toJson(TOPIC);
    String actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class).toString();
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(9)
  void duplicateSubscription() throws IOException {
    URL url = new URL(String.format("http://localhost/topic/subscribe/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.CONFLICT_409;
    assertEquals(expectedResponseCode, actualResponseCode);
    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("error", String.format("Topic '%s' is already subscribed", TOPIC.getName()));
    JsonObject actualResponseBody = GSON.fromJson(readLines(con.getErrorStream()), JsonObject.class);
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(10)
  void getEmptySubscribedTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/message/get/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.GET.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.NO_CONTENT_204;
    assertEquals(expectedResponseCode, actualResponseCode);
  }

  @Test
  @Order(11)
  void ackEmptySubscribedTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/message/ack/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.NO_CONTENT_204;
    assertEquals(expectedResponseCode, actualResponseCode);
  }

  @Test
  @Order(12)
  void publishMessageToExistingTopic() throws IOException {
    URL url = new URL(String.format("http://localhost/message/publish/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setDoOutput(true);
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.CONTENT_TYPE.toString(), Routing.SUPPORTED_CONTENT_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    write(con.getOutputStream(), MESSAGE.getContent());
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.CREATED_201;
    assertEquals(expectedResponseCode, actualResponseCode);
    String expectedResponseBody = GSON.toJson(MESSAGE);
    String actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class).toString();
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(13)
  void getNewMessage() throws IOException {
    URL url = new URL(String.format("http://localhost/message/get/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.GET.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.OK_200;
    assertEquals(expectedResponseCode, actualResponseCode);
    String expectedResponseBody = GSON.toJson(MESSAGE);
    String actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class).toString();
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(14)
  void getSameMessage() throws IOException {
    URL url = new URL(String.format("http://localhost/message/get/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.GET.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.OK_200;
    assertEquals(expectedResponseCode, actualResponseCode);
    String expectedResponseBody = GSON.toJson(MESSAGE);
    String actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class).toString();
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(15)
  void ackNewMessage() throws IOException {
    URL url = new URL(String.format("http://localhost/message/ack/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.CREATED_201;
    assertEquals(expectedResponseCode, actualResponseCode);
    long position = MESSAGE.getBytes().length;
    Topic next = new Topic(TOPIC.getName(), position);
    String expectedResponseBody = GSON.toJson(next);
    String actualResponseBody = GSON.fromJson(readLines(con.getInputStream()), JsonObject.class).toString();
    assertEquals(expectedResponseBody, actualResponseBody);
    assertEquals(Routing.SUPPORTED_CONTENT_TYPE, con.getContentType());
  }

  @Test
  @Order(16)
  void getOldMessage() throws IOException {
    URL url = new URL(String.format("http://localhost/message/get/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.GET.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.NO_CONTENT_204;
    assertEquals(expectedResponseCode, actualResponseCode);
  }

  @Test
  @Order(17)
  void ackOldMessage() throws IOException {
    URL url = new URL(String.format("http://localhost/message/ack/%s", TOPIC.getName()));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(HttpMethod.POST.toString());
    con.setRequestProperty(HttpHeader.ACCEPT.toString(), Routing.SUPPORTED_MEDIA_TYPE);
    con.setRequestProperty(HttpHeader.ACCEPT_CHARSET.toString(), Routing.SUPPORTED_CHARSET);
    con.connect();
    int actualResponseCode = con.getResponseCode();
    int expectedResponseCode = HttpStatus.NO_CONTENT_204;
    assertEquals(expectedResponseCode, actualResponseCode);
  }
}