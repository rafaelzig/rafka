package jp.rafaelzig.rafka.pubsub.nio;

import static jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler.readBytes;
import static jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler.readInt;
import static jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler.readLong;
import static jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler.writeBytes;
import static jp.rafaelzig.rafka.pubsub.nio.FileChannelHandler.writeLong;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jp.rafaelzig.rafka.pubsub.mock.MockFileChannel;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class FileChannelHandlerTest {

  @Test
  void readIllegalPositionInteger() {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readInt(channel, -1);

    // Then
    assertThrows(IllegalArgumentException.class, exec);
  }

  @Test
  void readNoInteger() throws IOException {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readInt(channel, 0);

    // Then
    assertThrows(EOFException.class, exec);
  }

  @Test
  void readOneInteger() throws IOException {
    // Given
    int expected = Integer.MAX_VALUE;
    ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
    byteBuffer.putInt(expected);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    int actual = readInt(channel, 0);

    // Then
    assertEquals(expected, actual);
  }

  @Test
  void readTwoIntegers() throws IOException {
    // Given
    int expectedSecond = Integer.MIN_VALUE;
    int expectedFirst = Integer.MAX_VALUE;
    ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES << 1);
    byteBuffer.putInt(expectedFirst);
    byteBuffer.putInt(expectedSecond);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    int actualFirst = readInt(channel, 0);
    int actualSecond = readInt(channel, Integer.BYTES);

    // Then
    assertEquals(expectedFirst, actualFirst);
    assertEquals(expectedSecond, actualSecond);
  }

  @Test
  void readIllegalPositionLong() {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readLong(channel, -1);

    // Then
    assertThrows(IllegalArgumentException.class, exec);
  }

  @Test
  void readNoLong() throws IOException {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readLong(channel, 0);

    // Then
    assertThrows(EOFException.class, exec);
  }

  @Test
  void readOneLong() throws IOException {
    // Given
    long expected = Long.MAX_VALUE;
    ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
    byteBuffer.putLong(expected);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    long actual = readLong(channel, 0);

    // Then
    assertEquals(expected, actual);
  }

  @Test
  void readTwoLongs() throws IOException {
    // Given
    long expectedSecond = Integer.MIN_VALUE;
    long expectedFirst = Long.MAX_VALUE;
    ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES << 1);
    byteBuffer.putLong(expectedFirst);
    byteBuffer.putLong(expectedSecond);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    long actualFirst = readLong(channel, 0);
    long actualSecond = readLong(channel, Long.BYTES);

    // Then
    assertEquals(expectedFirst, actualFirst);
    assertEquals(expectedSecond, actualSecond);
  }

  @Test
  void readIllegalLengthBytes() {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readBytes(channel, 0, 0);

    // Then
    assertThrows(IllegalArgumentException.class, exec);
  }

  @Test
  void readIllegalPositionBytes() {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readBytes(channel, -1, 1);

    // Then
    assertThrows(IllegalArgumentException.class, exec);
  }

  @Test
  void readNoBytes() throws IOException {
    // Given
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    Executable exec = () -> readBytes(channel, 0, 1);

    // Then
    assertThrows(EOFException.class, exec);
  }

  @Test
  void readOneByte() throws IOException {
    // Given
    byte[] expected = {Byte.MIN_VALUE};
    ByteBuffer byteBuffer = ByteBuffer.wrap(expected);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    byte[] actual = readBytes(channel, 0, expected.length);

    // Then
    assertArrayEquals(actual, expected);
  }

  @Test
  void readTwoBytes() throws IOException {
    // Given
    byte[] expected = {Byte.MIN_VALUE, Byte.MAX_VALUE};
    ByteBuffer byteBuffer = ByteBuffer.wrap(expected);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    byte[] actual = readBytes(channel, 0, expected.length);

    // Then
    assertArrayEquals(actual, expected);
  }

  @Test
  void readFirstByte() throws IOException {
    // Given
    byte[] expected = {Byte.MIN_VALUE, Byte.MAX_VALUE};
    ByteBuffer byteBuffer = ByteBuffer.wrap(expected);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    byte[] actual = readBytes(channel, 0, 1);

    // Then
    assertArrayEquals(actual, new byte[] {expected[0]});
  }

  @Test
  void readLastByte() throws IOException {
    // Given
    byte[] expected = {Byte.MIN_VALUE, Byte.MAX_VALUE};
    ByteBuffer byteBuffer = ByteBuffer.wrap(expected);
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    byte[] actual = readBytes(channel, expected.length - 1, expected.length - 1);

    // Then
    assertArrayEquals(actual, new byte[] {expected[expected.length - 1]});
  }

  @Test
  void readTooManyBytes() throws IOException {
    // Given
    ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] {Byte.MIN_VALUE, 0, Byte.MAX_VALUE});
    MockFileChannel channel = MockFileChannel.builder().buffer(byteBuffer).build();

    // When
    Executable exec = () -> readBytes(channel, 1, byteBuffer.capacity());

    // Then
    assertThrows(EOFException.class, exec);
  }

  @Test
  void writeOneLong() throws IOException {
    // Given
    long expected = Long.MIN_VALUE;
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    writeLong(channel, expected);

    // Then
    long actual = readLong(channel, 0);
    assertEquals(expected, actual);
  }

  @Test
  void writeTwoLong() throws IOException {
    // Given
    long expectedFirst = Long.MIN_VALUE;
    long expectedSecond = Long.MIN_VALUE;
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    writeLong(channel, expectedFirst);
    writeLong(channel, expectedSecond);

    // Then
    long actualFirst = readLong(channel, 0);
    long actualSecond = readLong(channel, Long.BYTES);
    assertEquals(expectedFirst, actualFirst);
    assertEquals(expectedSecond, actualSecond);
  }

  @Test
  void writeOneByte() throws IOException {
    // Given
    byte[] expected = {Byte.MIN_VALUE};
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    writeBytes(channel, expected);

    // Then
    byte[] actual = readBytes(channel, 0, expected.length);
    assertArrayEquals(expected, actual);
  }

  @Test
  void writeTwoBytes() throws IOException {
    // Given
    byte[] expected = {Byte.MIN_VALUE, Byte.MAX_VALUE};
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    writeBytes(channel, expected);

    // Then
    byte[] actual = readBytes(channel, 0, expected.length);
    assertArrayEquals(expected, actual);
  }

  @Test
  void writeManyBytes() throws IOException {
    // Given
    byte[] expected = "Hello World!".getBytes(StandardCharsets.UTF_8);
    MockFileChannel channel = MockFileChannel.builder().build();

    // When
    writeBytes(channel, expected);

    // Then
    byte[] actual = readBytes(channel, 0, expected.length);
    assertArrayEquals(expected, actual);
  }
}