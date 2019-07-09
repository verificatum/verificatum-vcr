
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
 * Mathematical utility functions for primitive types.
 *
 * @author Douglas Wikstrom
 */
public final class MathExt {

    /**
     * Bit-mask.
     */
    public static final int MASK = 0x80000000;

    /**
     * Maximal bit length of the input.
     */
    public static final int MAX_BITLENGTH = 32;

    /**
     * This prevents instantiation.
     */
    private MathExt() { }

    /**
     * Returns the least upper bound of the binary logarithm of the
     * input, which is assumed to be positive. The output is undefined
     * otherwise.
     *
     * @param x Integer of which the logarithmic upper bound is
     * requested.
     * @return Least upper bound on the binary logarithm of the input.
     */
    public static int log2c(final int x) {
        int mask = MASK;
        int res = MAX_BITLENGTH;
        while ((x & mask) == 0 && res >= 1) {
            mask >>>= 1;
            res--;
        }
        return res;
    }
}
