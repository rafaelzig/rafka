package com.mercari.merpay.pubsub.data;

import com.mercari.merpay.pubsub.nio.FileChannelHandler;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.Value;

/**
 * Represents a topic message.
 */
@Value
public class Message {

  @NonNull String topic;
  @NonNull String content;

  public static Message fromChannel(FileChannel channel, long position) throws EOFException, IOException {
    long cursor = position;
    int startTotalLength = FileChannelHandler.readInt(channel, cursor);
    cursor += Integer.BYTES;
    byte topicLength = FileChannelHandler.readByte(channel, cursor);
    cursor += Byte.BYTES;
    byte[] topicBytes = FileChannelHandler.readBytes(channel, cursor, topicLength);
    cursor += topicLength;
    int contentLength = FileChannelHandler.readInt(channel, cursor);
    cursor += Integer.BYTES;
    byte[] contentBytes = FileChannelHandler.readBytes(channel, cursor, contentLength);
    cursor += contentLength;
    int endTotalLength = FileChannelHandler.readInt(channel, cursor);
    if (startTotalLength != Integer.BYTES + Byte.BYTES + topicLength + Integer.BYTES + contentLength + Integer.BYTES) {
      throw new IllegalStateException("persisted total length is different than actual total length");
    }
    if (startTotalLength != endTotalLength) {
      throw new IllegalStateException("persisted start total length is different persisted end total length");
    }

    String topic = new String(topicBytes, StandardCharsets.UTF_8);
    String content = new String(contentBytes, StandardCharsets.UTF_8);
    return new Message(topic, content);
  }

  public byte[] getBytes() {
    return getBytes(Charset.defaultCharset());
  }

  public byte[] getBytes(Charset charset) {
    byte[] topicBytes = topic.getBytes(charset);
    byte topicLength = toByteExact(topicBytes.length);
    byte[] contentBytes = content.getBytes(charset);
    int contentLength = contentBytes.length;
    int totalLength = Integer.BYTES + Byte.BYTES + topicLength + Integer.BYTES + contentLength + Integer.BYTES;

    return ByteBuffer.allocate(totalLength)
        .putInt(totalLength)
        .put(topicLength)
        .put(topicBytes)
        .putInt(contentLength)
        .put(contentBytes)
        .putInt(totalLength)
        .array();
  }

  private static byte toByteExact(int value) {
    if ((byte) value != value) {
      throw new ArithmeticException("byte overflow");
    }
    return (byte) value;
  }
}
