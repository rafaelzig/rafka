package jp.rafaelzig.rafka.pubsub.repository;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import jp.rafaelzig.rafka.pubsub.data.Role;
import jp.rafaelzig.rafka.pubsub.data.Topic;
import jp.rafaelzig.rafka.pubsub.exception.DuplicateRegistrationException;
import jp.rafaelzig.rafka.pubsub.exception.DuplicateSubscriptionException;
import jp.rafaelzig.rafka.pubsub.exception.UnregisteredTopicException;
import jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * {@link Topic} instances should be created via this class so that state can be persisted to disk.
 */
@Value
public class TopicRepository {

  @NonNull Path topicDirPath;
  @NonNull Path principalFilePath;
  @NonNull Path logFilePath;
  @NonNull String topicName;

  @Builder
  private TopicRepository(@NonNull String dataDir,
      @NonNull String principal,
      @NonNull Role role,
      @NonNull String topicName) {
    this.topicName = topicName;
    topicDirPath = Paths.get(dataDir, topicName);
    principalFilePath = topicDirPath.resolve(String.format(role.fileNameFormat, principal));
    logFilePath = topicDirPath.resolve(MessageRepository.LOG_FILE_NAME);
  }

  public Topic createRegistration() throws DuplicateRegistrationException, IOException {
    try {
      long position = persistRegistration();
      return new Topic(topicName, position);
    } catch (FileAlreadyExistsException e) {
      throw new DuplicateRegistrationException(topicName, e);
    }
  }

  private long persistRegistration() throws FileAlreadyExistsException, IOException {
    Files.createDirectory(topicDirPath);
    Files.createFile(principalFilePath);
    Files.createFile(logFilePath);
    return 0;
  }

  public Topic createSubscription() throws DuplicateSubscriptionException, UnregisteredTopicException, IOException {
    try {
      long position = persistSubscription();
      return new Topic(topicName, position);
    } catch (FileAlreadyExistsException e) {
      throw new DuplicateSubscriptionException(topicName, e);
    } catch (FileNotFoundException | NoSuchFileException e) {
      throw new UnregisteredTopicException(topicName, e);
    }
  }

  private long persistSubscription() throws FileNotFoundException, IOException {
    long position = findCurrentPublicationPosition();
    try (FileChannel channel = FileChannel.open(principalFilePath, WRITE, CREATE_NEW)) {
      FileChannelHandler.writeLong(channel, position);
    }
    return position;
  }

  private long findCurrentPublicationPosition() throws FileNotFoundException, IOException {
    try (RandomAccessFile reader = new RandomAccessFile(logFilePath.toFile(), "r");
        FileChannel channel = reader.getChannel()) {
      long size = channel.size();
      long position = size - Integer.BYTES;
      if (position <= 0) {
        return 0;
      }
      return size - FileChannelHandler.readInt(channel, position);
    } catch (EOFException e) {
      // This should never occur as we are reading exactly Long.BYTES at position from (channel.size() - Long.BYTES)
      throw new IllegalStateException(e);
    }
  }
}
