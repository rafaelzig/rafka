package jp.rafaelzig.rafka.pubsub.mock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class MockFileChannel extends FileChannel {
  @Default
  private ByteBuffer buffer = ByteBuffer.allocate(0);

  @Override
  public int write(ByteBuffer src) throws IOException {
    int bytesWritten = write(src, position());
    position(position() + bytesWritten);
    return bytesWritten;
  }

  @Override
  public int write(ByteBuffer src, long position) throws IOException {
    long before = position();
    position(position);
    int bytes = src.remaining();
    if (bytes > buffer.remaining()) {
      int newLength = (buffer.capacity() - buffer.remaining()) + bytes;
      int offset = buffer.position();
      buffer = ByteBuffer.wrap(Arrays.copyOf(buffer.array(), newLength), offset, newLength - offset);
    }
    buffer.put(src);
    position(before);
    return bytes;
  }

  @Override
  public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
    return 0;
  }

  @Override
  public long position() throws IOException {
    return buffer.position();
  }

  @Override
  public FileChannel position(long newPosition) throws IOException {
    buffer.position(Math.toIntExact(newPosition));
    return this;
  }

  @Override
  public long size() throws IOException {
    return buffer.capacity();
  }

  @Override
  public FileChannel truncate(long size) throws IOException {
    if (size < 0) {
      throw new IllegalArgumentException("size cannot be negative");
    }
    if (position() > size) {
      position(size);
    }
    if (size() >= size) {
      return this;
    }

    int newLength = Math.toIntExact(size);
    int offset = Math.min(buffer.position(), newLength);
    buffer = ByteBuffer.wrap(Arrays.copyOf(buffer.array(), newLength), offset, newLength - offset);
    return this;
  }

  @Override
  public void force(boolean metaData) throws IOException {
  }

  @Override
  public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
    return 0;
  }

  @Override
  public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
    return 0;
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    int bytesRead = read(dst, position());
    position(position() + bytesRead);
    return bytesRead;
  }

  @Override
  public int read(ByteBuffer dst, long position) {
    if (position < 0) {
      throw new IllegalArgumentException("negative position");
    }
    if (position >= buffer.capacity()) {
      return -1;
    }
    int bytesRead = 0;
    for (int i = 0; i < dst.limit(); i++) {
      int index = Math.toIntExact(i + position);
      if (index >= buffer.capacity()) {
        break;
      }
      dst.put(buffer.get(index));
      ++bytesRead;
    }
    return bytesRead;
  }

  @Override
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
    return 0;
  }

  @Override
  public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
    return null;
  }

  @Override
  public FileLock lock(long position, long size, boolean shared) throws IOException {
    return null;
  }

  @Override
  public FileLock tryLock(long position, long size, boolean shared) throws IOException {
    return null;
  }

  @Override
  protected void implCloseChannel() throws IOException {

  }
}
