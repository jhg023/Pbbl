/*
 * MIT License
 *
 * Copyright (c) 2019 Jacob Glickman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pbbl;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * A pool that can contain buffers, eg. {@link ByteBuffer}s.
 * <br><br>
 * Buffers dispatched from this pool should be returned, resulting in significant performance improvements from
 * not having to constantly allocate new buffer instances.
 *
 * @param <T> the type of the buffers this pool stores, eg. {@link ByteBuffer}
 * @author Jacob G.
 * @since February 23, 2019
 */
public abstract class BufferPool<T> {
    
    /**
     * The data structure that holds all pooled buffers.
     */
    private final NavigableMap<Integer, Deque<T>> buffers = new TreeMap<>();
    
    /**
     * An abstract method that, given a capacity, creates a new {@code HeapByteBuffer} or {@code DirectByteBuffer}.
     *
     * @param n the capacity of the buffer to create.
     * @return a newly-created buffer.
     */
    protected abstract T create(int n);
    
    /**
     * Attempts to take a new buffer from the pool.
     * <br><br>
     * If no buffer can be found (with a capacity of at-least {@code n}) within the pool, then a new one is
     * created.
     *
     * @param n the capacity of the buffer requested.
     * @return a buffer with a capacity greater than or equal to {@code n}, with its limit set to {@code
     * n} and position set to {@code 0}.
     */
    public T take(int n) {
        synchronized (buffers) {
            var entry = buffers.ceilingEntry(n);
            
            // If entry is null, there exists no ByteBuffer within the map with a capacity greater than or equal to
            // the value requested. For that reason, one should be created.
            if (entry == null) {
                return create(n);
            }
            
            // Even though the entry isn't null, the deque that was found may not be. If it isn't, a ByteBuffer
            // should be taken from there and returned.
            var deque = entry.getValue();
            
            if (!deque.isEmpty()) {
                return clearAndLimitBuffer(deque.poll(), n);
            }
            
            // The first entry that was found had no ByteBuffers available, so we must now look at every greater
            // entry to see if one can be found. If one still cannot be found, allocate a new one.
            return buffers.tailMap(n, false).values()
                    .stream()
                    .filter(Predicate.not(Deque::isEmpty))
                    .map(Deque::poll)
                    .findAny()
                    .map(buffer -> clearAndLimitBuffer(buffer, n))
                    .orElseGet(() -> create(n));
        }
    }
    
    /**
     * Gives the specified buffer to this {@link BufferPool}.
     * <br><br>
     * This method should only be called <strong>after</strong> calling {@link #take(int)}.
     *
     * @param buffer the buffer to return to this pool.
     */
    public void give(T buffer) {
        synchronized (buffers) {
            buffers.computeIfAbsent(bufferCapacity(buffer), $ -> new ArrayDeque<>(3)).offer(buffer);
        }
    }
    
    /**
     * Clears and limits the specified buffer (in this order).
     * 
     * @param buffer the buffer to limit.
     * @param limit the new limit of the buffer.
     * @return the same {@link T} instance (the {@code buffer} parameter)
     * @see java.nio.Buffer#clear()
     * @see java.nio.Buffer#limit(int)
     */
    protected abstract T clearAndLimitBuffer(T buffer, int limit);
    
    /**
     * Gets the capacity of the specified buffer.
     *
     * @param buffer the buffer to get the capacity of.
     * @return the capacity of the buffer.
     * @see java.nio.Buffer#capacity()
     */
    protected abstract int bufferCapacity(T buffer);
    
}
