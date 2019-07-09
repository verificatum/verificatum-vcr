
/* Copyright 2008-2019 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.verificatum.eio;

import java.io.Closeable;

/**
 * Wrapper class that provides a queue-like interface to a byte tree
 * reader.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeQueue implements Closeable {

    /**
     * Next byte tree to be read.
     */
    private ByteTree head;

    /**
     * Internal reader of byte trees.
     */
    private final ByteTreeReader reader;

    /**
     * Creates a queue from the given byte tree reader.
     *
     * @param reader Underlying byte tree reader.
     * @throws EIOException If reading from the underlying byte tree
     * reader fails.
     */
    public ByteTreeQueue(final ByteTreeReader reader) throws EIOException {
        this.reader = reader;
        if (reader.getRemaining() > 0) {
            this.head = reader.getNextChild().readByteTree();
        } else {
            this.head = null;
        }
    }

    /**
     * Creates a queue from a byte tree reader of the given byte tree.
     *
     * @param byteTree Byte tree from which the underlying byte tree
     * reader reads.
     * @throws EIOException If getting the byte tree reader from the
     * input fails.
     */
    public ByteTreeQueue(final ByteTree byteTree) throws EIOException {
        this(byteTree.getByteTreeReader());
    }

    /**
     * Returns number of elements left in the queue.
     *
     * @return Number of elements left in the queue.
     */
    public int getRemaining() {
        if (head == null) {
            return 0;
        } else {
            return reader.getRemaining() + 1;
        }
    }

    /**
     * Returns the first byte tree in the queue without removing it.
     *
     * @return First byte tree in the queue.
     */
    public ByteTree peekByteTree() {
        return head;
    }

    /**
     * Returns the first byte tree in the queue and removes it.
     *
     * @return First byte tree in the queue.
     * @throws EIOException If reading from the underlying byte tree
     * reader fails.
     */
    public ByteTree popByteTree() throws EIOException {
        final ByteTree result = head;
        if (reader.getRemaining() > 0) {
            head = reader.getNextChild().readByteTree();
        } else {
            head = null;
        }
        return result;
    }

    /**
     * Closes the underlying reader.
     */
    public void close() {
        reader.close();
    }
}

