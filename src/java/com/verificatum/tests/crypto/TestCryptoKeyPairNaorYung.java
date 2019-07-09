
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

package com.verificatum.tests.crypto;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.crypto.CryptoKeyGenNaorYung;
import com.verificatum.crypto.CryptoKeyPair;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ExtIO;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link CryptoKeyPair} for Naor-Yung keys.
 *
 * @author Douglas Wikstrom
 */
public final class TestCryptoKeyPairNaorYung extends TestCryptoKeyPair {

    /**
     * Generates key pair used for testing.
     *
     * @param tp Test parameters.
     * @return Key pair used for testing.
     * @throws ArithmFormatException If a test fails.
     */
    public static CryptoKeyPair keyPair(final TestParameters tp)
        throws ArithmFormatException {
        final CryptoKeyGenNaorYung keyGen =
            TestCryptoKeyGenNaorYung.keyGen(tp);
        final RandomSource rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
        return keyGen.gen(rs, 10);
    }

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If a test fails.
     */
    public TestCryptoKeyPairNaorYung(final TestParameters tp)
        throws ArithmFormatException {
        super(tp, keyPair(tp));
    }
}
