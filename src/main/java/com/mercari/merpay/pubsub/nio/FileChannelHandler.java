package com.mercari.merpay.pubsub.nio;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Properties;

/**
 * Wrapper around {@link FileChannel} methods to facilitate read/write operations.
 */
public class FileChannelHandler {

  public static int readInt(FileChannel channel, long position) throws EOFException, IOException {
    checkPosition(position);
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
    int bytesRead = channel.read(buffer, position);
    if (bytesRead <= 0) {
      throw new EOFException("EOF reached while reading int");
    }
    buffer.flip();
    return buffer.getInt();
  }

  public static long readLong(FileChannel channel, long position) throws EOFException, IOException {
    checkPosition(position);
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    int bytesRead = channel.read(buffer, position);
    if (bytesRead <= 0) {
      throw new EOFException("EOF reached while reading long");
    }
    buffer.flip();
    return buffer.getLong();
  }

  public static byte readByte(FileChannel channel, long position) throws EOFException, IOException {
    checkPosition(position);
    ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
    int bytesRead = channel.read(buffer, position);
    if (bytesRead <= 0) {
      throw new EOFException("EOF reached while reading byte");
    }
    buffer.flip();
    return buffer.get();
  }

  public static byte[] readBytes(FileChannel channel, long position, int length) throws EOFException, IOException {
    checkPosition(position);
    checkLength(length);
    ByteBuffer buffer = ByteBuffer.allocate(length);
    int bytesRead = channel.read(buffer, position);
    buffer.flip();
    if (bytesRead <= 0) {
      throw new EOFException("EOF reached while reading bytes");
    }
    byte[] bytes = buffer.array();
    if (bytesRead < length) {
      throw new EOFException("EOF reached while reading bytes");
    }
    return bytes;
  }

  public static void writeLong(FileChannel channel, long l) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES)
        .putLong(l)
        .flip();
    channel.write(buffer);
    channel.force(false);
  }

  public static void writeBytes(FileChannel channel, byte[] bytes) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    channel.write(buffer);
    channel.force(false);
  }

  private static void checkLength(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("illegal length");
    }
  }

  private static void checkPosition(long position) {
    if (position < 0) {
      throw new IllegalArgumentException("negative position");
    }
  }
}
