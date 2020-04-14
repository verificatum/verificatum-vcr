
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
 * A reader of {@link ByteTree} instances.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeReaderBT extends ByteTreeReader {

    /**
     * Underlying byte tree.
     */
    ByteTree bt;

    /**
     * Creates a reader of the given byte tree with the given parent.
     *
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderBT(final ByteTree bt) {
        this(null, bt);
    }

    /**
     * Creates a reader of the given byte tree with the given parent.
     *
     * @param parent Instance that spawned this one.
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderBT(final ByteTreeReader parent, final ByteTree bt) {
        super(parent, getRemaining(bt));
        this.bt = bt;
    }

    /**
     * Returns the number of children/bytes in the given byte tree.
     *
     * @param bt Underlying byte tree.
     * @return Number of children/bytes
     */
    private static int getRemaining(final ByteTree bt) {
        if (bt.value == null) {
            return bt.children.length;
        } else {
            return bt.value.length;
        }
    }

    // Documented in ByteTreeReader.java

    @Override
    public boolean isLeaf() {
        return bt.value != null;
    }

    @Override
    protected ByteTreeReader getNextChildInner() {
        final ByteTree child = bt.children[bt.children.length - remaining];
        return new ByteTreeReaderBT(this, child);
    }

    @Override
    protected void readInner(final byte[] destination,
                             final int offset,
                             final int length) {
        System.arraycopy(bt.value, bt.value.length - remaining, destination,
                         offset, length);
    }

    @Override
    public void close() {

        // Potentially this helps the garbage collect.
        this.bt = null;
    }
}
