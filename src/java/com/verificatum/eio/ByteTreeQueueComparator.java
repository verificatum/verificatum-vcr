
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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator of queues of byte trees. Note that this comparator
 * compares the first elements in two queues of byte trees. Note that
 * this comparator imposes orderings that are inconsistent with
 * equals.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeQueueComparator
    implements Comparator<ByteTreeQueue>, Serializable {

    /**
     * Underlying comparator of byte trees.
     */
    private final ByteTreeComparator comparator;

    /**
     * Underlying comparator of byte trees.
     *
     * @param comparator Comparator of byte trees.
     */
    public ByteTreeQueueComparator(final ByteTreeComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(final ByteTreeQueue left, final ByteTreeQueue right) {
        return comparator.compare(left.peekByteTree(), right.peekByteTree());
    }
}
