
/*
 * Copyright 2008-2018 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * VCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * VCR is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with VCR. If not, see <http://www.gnu.org/licenses/>.
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

