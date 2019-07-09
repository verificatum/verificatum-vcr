
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

package com.verificatum.arithm;

/**
 * Interface for an iterator over a {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerIteratorIM implements LargeIntegerIterator {

    /**
     * Current index.
     */
    private int current;

    /**
     * Underlying array.
     */
    final LargeIntegerArrayIM array;

    /**
     * Creates an iterator reading from the given array.
     *
     * @param array Underlying array.
     */
    public LargeIntegerIteratorIM(final LargeIntegerArrayIM array) {
        this.array = array;
        this.current = 0;
    }

    // Documented in LargeIntegerIterator.java

    @Override
    public LargeInteger next() {
        if (current < array.li.length) {
            return array.li[current++];
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return current < array.li.length;
    }

    /**
     * This method is needed to give a uniform interface for both
     * memory mapped and file mapped iterators. The latter must be
     * closed manually.
     */
    @Override
    public void close() {
    }
}
