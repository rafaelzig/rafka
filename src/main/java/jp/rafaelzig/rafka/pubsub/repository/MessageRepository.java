package jp.rafaelzig.rafka.pubsub.repository;

import jp.rafaelzig.rafka.pubsub.data.Message;
import jp.rafaelzig.rafka.pubsub.data.Role;
import jp.rafaelzig.rafka.pubsub.data.Topic;
import jp.rafaelzig.rafka.pubsub.exception.ExhaustedTopicException;
import jp.rafaelzig.rafka.pubsub.exception.UnauthorizedPublisherException;
import jp.rafaelzig.rafka.pubsub.exception.UnregisteredTopicException;
import jp.rafaelzig.rafka.pubsub.exception.UnsubscribedTopicException;
import jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * {@link Message} instances should be created via this class so that state can be persisted to disk.
 */
@Value
public class MessageRepository {

  public static final String LOG_FILE_NAME = "message.log";

  @NonNull Path principalFilePath;
  @NonNull Path logFilePath;
  @NonNull String topicName;

  @Builder
  private MessageRepository(@NonNull String dataDir,
      @NonNull String principal,
      @NonNull Role role,
      @NonNull String topicName) {
    this.topicName = topicName;
    Path topicDirPath = Paths.get(dataDir, topicName);
    principalFilePath = topicDirPath.resolve(String.format(role.fileNameFormat, principal));
    logFilePath = topicDirPath.resolve(LOG_FILE_NAME);
  }

  public Message createPublication(String content)
      throws UnregisteredTopicException, UnauthorizedPublisherException, IOException {
    if (Files.notExists(logFilePath)) {
      throw new UnregisteredTopicException(topicName);
    }
    if (Files.notExists(principalFilePath)) {
      throw new UnauthorizedPublisherException(topicName);
    }
    try {
      return persistPublication(content);
    } catch (FileNotFoundException e) {
      throw new UnregisteredTopicException(topicName, e);
    }
  }

  private Message persistPublication(String content) throws FileNotFoundException, IOException {
    try (FileOutputStream out = new FileOutputStream(logFilePath.toFile(), true);
        FileChannel channel = out.getChannel()) {
      Message message = new Message(topicName, content);
      FileChannelHandler.writeBytes(channel, message.getBytes(StandardCharsets.UTF_8));
      return message;
    }
  }

  public Message fetchCurrentPublication() throws ExhaustedTopicException, UnsubscribedTopicException, IOException {
    try {
      long position = findSubscriberCurrentPublicationPosition();
      return readPublication(position);
    } catch (EOFException e) {
      throw new ExhaustedTopicException(topicName, e);
    } catch (FileNotFoundException | NoSuchFileException e) {
      throw new UnsubscribedTopicException(topicName, e);
    }
  }

  private long findSubscriberCurrentPublicationPosition() throws FileNotFoundException, IOException {
    try (RandomAccessFile reader = new RandomAccessFile(principalFilePath.toFile(), "r");
        FileChannel channel = reader.getChannel()) {
      long position = channel.size() - Long.BYTES;
      if (position < 0) {
        throw new IllegalStateException(
            String.format("Subscriber file %s is missing next position of topic %s", principalFilePath, topicName));
      }
      return FileChannelHandler.readLong(channel, position);
    } catch (EOFException e) {
      // This should never happen as we are reading exactly Long.BYTES at position from (channel.size() - Long.BYTES)
      throw new IllegalStateException(e);
    }
  }

  private Message readPublication(long position) throws EOFException, FileNotFoundException, IOException {
    try (RandomAccessFile reader = new RandomAccessFile(logFilePath.toFile(), "r");
        FileChannel channel = reader.getChannel()) {
      return Message.fromChannel(channel, position);
    }
  }

  public Topic advanceToNextPublication() throws UnsubscribedTopicException, ExhaustedTopicException, IOException {
    try {
      long currentPosition = findSubscriberCurrentPublicationPosition();
      long nextPosition = findNextPublicationPosition(currentPosition);
      persistSubscriberPublicationPosition(nextPosition);
      return new Topic(topicName, nextPosition);
    } catch (EOFException e) {
      throw new ExhaustedTopicException(topicName, e);
    } catch (FileNotFoundException | NoSuchFileException e) {
      throw new UnsubscribedTopicException(topicName, e);
    }
  }

  private void persistSubscriberPublicationPosition(long position) throws FileNotFoundException, IOException {
    try (FileOutputStream out = new FileOutputStream(principalFilePath.toFile(), true);
        FileChannel channel = out.getChannel()) {
      FileChannelHandler.writeLong(channel, position);
    }
  }

  private long findNextPublicationPosition(long position) throws FileNotFoundException, EOFException, IOException {
    try (RandomAccessFile reader = new RandomAccessFile(logFilePath.toFile(), "r");
        FileChannel channel = reader.getChannel()) {
      int length = FileChannelHandler.readInt(channel, position);
      return position + length;
    }
  }
}
