
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

import com.verificatum.eio.ExtIO;

/**
 * Digest for a random oracle.
 *
 * @author Douglas Wikstrom
 */
public final class HashdigestRandomOracle implements Hashdigest {

    /**
     * Underlying hashfunction.
     */
    Hashfunction hashfunction;

    /**
     * Message digest wrapped by this instance.
     */
    Hashdigest hd;

    /**
     * Output bit length.
     */
    int outputLength;

    /**
     * Creates an instance using the given hashfunction and with the
     * given output bit length.
     *
     * @param hashfunction Underlying hashfunction.
     * @param outputLength Output bit length.
     */
    public HashdigestRandomOracle(final Hashfunction hashfunction,
                                  final int outputLength) {

        this.hashfunction = hashfunction;
        this.outputLength = outputLength;

        final byte[] prefix = new byte[4];
        ExtIO.writeInt(prefix, 0, outputLength);

        this.hd = hashfunction.getDigest();
        hd.update(prefix);
    }

    // Documented in Hashdigest.java

    @Override
    public void update(final byte[]... data) {
        hd.update(data);
    }

    @Override
    public void update(final byte[] data,
                       final int offset,
                       final int length) {
        hd.update(data, offset, length);
    }

    @Override
    public byte[] digest() {

        final PRGHeuristic prg = new PRGHeuristic(hashfunction);

        final byte[] seed = hd.digest();

        prg.setSeed(seed);

        final int len = (outputLength + 7) / 8;

        final byte[] res = prg.getBytes(len);

        if (outputLength % 8 != 0) {
            res[0] = (byte) (res[0] & (0xFF >>> (8 - outputLength % 8)));
        }

        return res;
    }
}
