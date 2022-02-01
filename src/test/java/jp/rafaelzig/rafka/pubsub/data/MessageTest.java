package jp.rafaelzig.rafka.pubsub.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jp.rafaelzig.rafka.pubsub.mock.MockFileChannel;
import jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class MessageTest {

  @Test
  void fromChannelInvalidPosition() throws IOException {
    // Given
    Message expected = new Message("Hello Topic", "Hello World");
    MockFileChannel channel = MockFileChannel.builder().build();
    FileChannelHandler.writeBytes(channel, expected.getBytes(StandardCharsets.UTF_8));

    // When
    Executable exec = () -> Message.fromChannel(channel, 1);

    // Then
    assertThrows(EOFException.class, exec);
  }

  @Test
  void fromChannelStart() throws IOException {
    // Given
    Message expected = new Message("Hello Topic", "Hello World");
    MockFileChannel channel = MockFileChannel.builder().build();
    FileChannelHandler.writeBytes(channel, expected.getBytes(StandardCharsets.UTF_8));

    // When
    Message actual = Message.fromChannel(channel, 0);

    // Then
    assertEquals(expected, actual);
  }

  @Test
  void fromChannelPosition() throws IOException {
    // Given
    Message first = new Message("Hello Topic", "Hello World");
    byte[] firstBytes = first.getBytes(StandardCharsets.UTF_8);
    Message expected = new Message("Bye Topic", "Bye World");
    MockFileChannel channel = MockFileChannel.builder().build();
    FileChannelHandler.writeBytes(channel, firstBytes);
    FileChannelHandler.writeBytes(channel, expected.getBytes(StandardCharsets.UTF_8));

    // When
    Message actual = Message.fromChannel(channel, firstBytes.length);

    // Then
    assertEquals(expected, actual);
  }

  @Test
  void getBytesSameContent() {
    // Given
    Message msg = new Message("Hello Topic", "Hello World");
    byte[] expectedTopicBytes = msg.getTopic().getBytes(StandardCharsets.UTF_8);
    byte[] actualTopicBytes = new byte[expectedTopicBytes.length];
    byte[] expectedContentBytes = msg.getContent().getBytes(StandardCharsets.UTF_8);
    byte[] actualContentBytes = new byte[expectedContentBytes.length];
    int expectedLength = Integer.BYTES
        + Byte.BYTES
        + msg.getTopic().getBytes(StandardCharsets.UTF_8).length
        + Integer.BYTES
        + msg.getContent().getBytes(StandardCharsets.UTF_8).length
        + Integer.BYTES;

    // When
    byte[] actualBytes = msg.getBytes(StandardCharsets.UTF_8);
    int actualLength = ByteBuffer.wrap(actualBytes, 0, Integer.BYTES).getInt();
    byte actualTopicLength = ByteBuffer.wrap(actualBytes, Integer.BYTES, Byte.BYTES).get();
    ByteBuffer.wrap(actualBytes, Integer.BYTES + Byte.BYTES, actualTopicLength).get(actualTopicBytes);
    int actualContentLength = ByteBuffer
        .wrap(actualBytes, Integer.BYTES + Byte.BYTES + actualTopicLength, Integer.BYTES)
        .getInt();
    ByteBuffer
        .wrap(actualBytes, Integer.BYTES + Byte.BYTES + actualTopicLength + Integer.BYTES, actualContentLength)
        .get(actualContentBytes);

    // Then
    assertEquals(expectedLength, actualLength);
    assertEquals(expectedTopicBytes.length, actualTopicLength);
    assertArrayEquals(expectedTopicBytes, actualTopicBytes);
    assertEquals(expectedContentBytes.length, actualContentLength);
    assertEquals(expectedLength, actualBytes.length);
  }
}