
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
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.crypto.CryptoError;
import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.PRGElGamal;
import com.verificatum.eio.ByteTree;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PRGElGamal}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPRGElGamal extends TestPRG {

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     * @throws CryptoFormatException If construction of the test
     * failed.
     */
    public TestPRGElGamal(final TestParameters tp)
        throws ArithmFormatException, CryptoFormatException {
        super(tp);
    }

    /**
     * Constructors.
     *
     * @throws ArithmFormatException If a test fails.
     * @throws CryptoFormatException If a test fails.
     */
    public void constructors()
        throws ArithmFormatException, CryptoFormatException {

        final LargeInteger[] modulus = new LargeInteger[3];
        modulus[0] = SafePrimeTable.safePrime(512);
        modulus[1] = modulus[0].add(LargeInteger.ONE);
        modulus[2] = modulus[1].neg();

        for (int i = 0; i < modulus.length; i++) {
            boolean invalid = false;
            try {
                new PRGElGamal(modulus[i], i + 1, 0);
            } catch (final CryptoError ce) {
                invalid = true;
            }
            assert invalid : "Failed to fail on bad parameters! (" + i + ")";
        }
    }

    @Override
    protected PRG[] prgs() throws ArithmFormatException, CryptoFormatException {
        final PRG[] prgs = new PRG[2];

        int bitLength = 512;
        for (int j = 0; j < prgs.length; j++) {
            final LargeInteger safePrime = SafePrimeTable.safePrime(bitLength);
            prgs[j] = new PRGElGamal(safePrime, 50);
            bitLength <<= 1;
        }
        return prgs;
    }

    /**
     * newInstance.
     */
    public void newInstance() {

        boolean invalid = false;
        try {
            final ByteTree bt = new ByteTree(new byte[1]);
            PRGElGamal.newInstance(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }
}
