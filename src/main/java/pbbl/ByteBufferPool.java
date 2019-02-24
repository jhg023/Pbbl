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
 * A pool that can contain both {@code HeapByteBuffer}s and {@code DirectByteBuffer}s.
 * <br><br>
 * {@link ByteBuffer}s dispatched from this pool will be reused, resulting in significant performance improvements from
 * not having to constantly allocate new {@link ByteBuffer}s.
 *
 * @author Jacob G.
 * @since February 23, 2019
 */
public abstract class ByteBufferPool {
    
    /**
     * The data structure that holds all pooled {@link ByteBuffer}s.
     */
    private final NavigableMap<Integer, Deque<ByteBuffer>> buffers = new TreeMap<>();
    
    /**
     * An abstract method that, given a capacity, creates a new {@code HeapByteBuffer} or {@code DirectByteBuffer}.
     *
     * @param n the capacity of the {@link ByteBuffer} to create.
     * @return a newly-created {@link ByteBuffer}.
     */
    protected abstract ByteBuffer create(int n);
    
    /**
     * Attempts to take a new {@link ByteBuffer} from the pool.
     * <br><br>
     * If no {@link ByteBuffer} can be found (with a capacity of at-least {@code n}) within the pool, then a new one is
     * created.
     *
     * @param n the capacity of the {@link ByteBuffer} requested.
     * @return a {@link ByteBuffer} with a capacity greater than or equal to {@code n}, with its limit set to {@code
     * n} and position set to {@code 0}.
     */
    public ByteBuffer take(int n) {
        synchronized (buffers) {
            var entry = buffers.ceilingEntry(n);
            
            // If entry is null, there exists no ByteBuffer within the map with a capacity greater than or equal to
            // the value requested. For that reason, one should be created.
            if (entry == null) {
                buffers.put(n, new ArrayDeque<>(3));
                return create(n);
            }
            
            // Even though the entry isn't null, the deque that was found may not be. If it isn't, a ByteBuffer
            // should be taken from there and returned.
            var deque = entry.getValue();
            
            if (!deque.isEmpty()) {
                return deque.poll().clear().limit(n);
            }
            
            // The first entry that was found had no ByteBuffers available, so we must now look at every greater
            // entry to see if one can be found. If one still cannot be found, allocate a new one.
            return buffers.tailMap(n, false).values()
                    .stream()
                    .filter(Predicate.not(Deque::isEmpty))
                    .map(Deque::poll)
                    .findAny()
                    .map(buffer -> buffer.clear().limit(n))
                    .orElseGet(() -> create(n));
        }
    }
    
    /**
     * Gives the specified {@link ByteBuffer} to this {@link ByteBufferPool}.
     * <br><br>
     * This method should only be called <strong>after</strong> calling {@link #take(int)}.
     *
     * @param buffer the {@link ByteBuffer} to return to this pool.
     */
    public void give(ByteBuffer buffer) {
        synchronized (buffers) {
            buffers.computeIfAbsent(buffer.capacity(), $ -> new ArrayDeque<>()).offer(buffer);
        }
    }
    
}
