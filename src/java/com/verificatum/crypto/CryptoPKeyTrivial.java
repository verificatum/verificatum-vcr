
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

import java.util.Arrays;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.ui.Util;


/**
 * A trivial encryption key that maps a message to itself. This is
 * useful as a fallback to simplify the logics of protocols.
 *
 * @author Douglas Wikstrom
 */
public final class CryptoPKeyTrivial implements CryptoPKey {

    /**
     * Constructs an instance corresponding to the input
     * representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Public key represented by the input.
     */
    public static CryptoPKeyTrivial newInstance(final ByteTreeReader btr,
                                                final RandomSource rs,
                                                final int certainty) {
        return new CryptoPKeyTrivial();
    }

    // Documented in CryptoPKey.java

    @Override
    public byte[] encrypt(final byte[] label,
                          final byte[] message,
                          final RandomSource randomSource,
                          final int statDist) {
        return Arrays.copyOfRange(message, 0, message.length);
    }

    // Documented in ByteTreeConvertible.java

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTree();
    }

    // Documented in Marshalizable.java

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose);
    }
}
