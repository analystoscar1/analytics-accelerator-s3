package com.amazon.connector.s3.io.physical.blockmanager;

import com.amazon.connector.s3.common.Preconditions;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A circular buffer of fixed capacity. Closes its elements before removing them. Not thread-safe.
 */
public class AutoClosingCircularBuffer<T extends Closeable> implements Closeable {

  private final List<T> buffer;
  private final int capacity;
  private int oldestIndex;

  /**
   * Creates an instance of AutoClosingCircularBuffer.
   *
   * @param maxCapacity The maximum capacity of the buffer.
   */
  public AutoClosingCircularBuffer(int maxCapacity) {
    Preconditions.checkState(0 < maxCapacity, "maxCapacity should be positive");

    this.oldestIndex = 0;
    this.capacity = maxCapacity;
    this.buffer = Collections.synchronizedList(new ArrayList<>(maxCapacity));
  }

  /**
   * Adds an element to the buffer, potentially replacing another element if the maximum capacity
   * has been reached. Calls 'close' on elements before evicting them.
   *
   * @param element The new element to add to the buffer.
   */
  public void add(T element) throws IOException {
    if (buffer.size() < capacity) {
      buffer.add(element);
    } else {
      buffer.get(oldestIndex).close();
      buffer.set(oldestIndex, element);
      oldestIndex = (oldestIndex + 1) % capacity;
    }
  }

  /**
   * Returns a conventional Java stream of the underlying objects
   *
   * @return a stream of the buffer content
   */
  public Stream<T> stream() {
    synchronized (buffer) {
      return buffer.stream();
    }
  }

  /** Closes the buffer, freeing up all underlying resources. */
  @Override
  public void close() throws IOException {
    for (T t : buffer) {
      t.close();
    }
  }
}
