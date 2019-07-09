
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

/**
 * A reader of {@link ByteTreeContainer} instances.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeReaderC extends ByteTreeReader {

    /**
     * Byte tree from which this instance reads.
     */
    ByteTreeContainer btc;

    /**
     * Position of reader within the underlying byte tree.
     */
    int index;

    /**
     * Creates an instance that reads from the given byte tree and
     * with the given parent.
     *
     * @param parent Instance that spawned this one.
     * @param btc Byte tree from which this instance reads.
     */
    protected ByteTreeReaderC(final ByteTreeReader parent,
                              final ByteTreeContainer btc) {
        super(parent, btc.children.length);
        this.btc = btc;
        this.index = 0;
    }

    // Documented in ByteTreeReader.java

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    protected ByteTreeReader getNextChildInner() {
        final ByteTreeReader btr = btc.children[index++].getByteTreeReader();
        btr.parent = this;
        return btr;
    }

    @Override
    protected void readInner(final byte[] destination,
                             final int offset,
                             final int length) {
        throw new EIOError("A ByteTreeContainer is always a node!");
    }

    @Override
    public void close() {
        btc = null;
    }
}
