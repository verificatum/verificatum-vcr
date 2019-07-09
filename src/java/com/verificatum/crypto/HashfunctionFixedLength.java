
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

package com.verificatum.crypto;

/**
 * Abstract base class for a collision-free hash function with fixed
 * input and output sizes. If the input length is not a multiple of 8,
 * then the appropriate number of bits in the first input byte are
 * ignored.
 *
 * @author Douglas Wikstrom
 */
public interface HashfunctionFixedLength extends HashfunctionBasic {

    /**
     * Evaluates the function on the given input. If the output length
     * is not a multiple of 8, the suitable number of bits in the
     * first output byte are set to zero.
     *
     * @param data Input data.
     * @return Output of hash function.
     */
    byte[] hash(byte[] data);

    /**
     * Returns the input bit length.
     *
     * @return Input length in bits.
     */
    int getInputLength();
}
