/*
 * MIT License
 *
 * Copyright (c) 2020 Jacob Glickman
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
package com.github.pbbl.heap;

import com.github.pbbl.AbstractBufferPool;

import java.nio.FloatBuffer;

/**
 * Represents a pool of non-direct {@link FloatBuffer} objects.
 *
 * @author Jacob G.
 * @since May 25, 2020
 */
public final class FloatBufferPool extends AbstractBufferPool<FloatBuffer> {

    @Override
    protected FloatBuffer allocate(int capacity) {
        return FloatBuffer.allocate(capacity);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code buffer} is direct.
     */
    @Override
    public void give(FloatBuffer buffer) {
        if (buffer.isDirect()) {
            throw new IllegalArgumentException("A direct FloatBuffer cannot be given to a FloatBufferPool!");
        }

        super.give(buffer);
    }
}
