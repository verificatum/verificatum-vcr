
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

package com.verificatum.tests.arithm;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ExtIO;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;

/**
 * Tests {@link SafePrimeTable}.
 *
 * @author Douglas Wikstrom
 */
public final class TestSafePrimeTable extends TestClass {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestSafePrimeTable(final TestParameters tp) {
        super(tp);
    }

    /**
     * Sanity check.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void sanityCheck()
        throws ArithmFormatException {

        final RandomSource rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
        final Timer timer = new Timer(testTime);

        final int size =
            SafePrimeTable.MAX_BIT_LENGTH - SafePrimeTable.MIN_BIT_LENGTH;

        while (!timer.timeIsUp()) {

            final byte[] indexBytes = rs.getBytes(4);
            final int offset = Math.abs(ExtIO.readInt(indexBytes, 0)) % size;
            final int index = SafePrimeTable.MIN_BIT_LENGTH + offset;

            final LargeInteger candidate = SafePrimeTable.safePrime(index);

            assert candidate.bitLength() == index
                : "Candidate at index " + index + "has wrong bit length!";

            assert candidate.isSafePrime(rs, 5)
                : "Prime of bit length is " + index + " is not safe!";
        }

        boolean invalid = false;
        try {
            SafePrimeTable.safePrime(SafePrimeTable.MIN_BIT_LENGTH - 1);
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on too small bit length!";

        invalid = false;
        try {
            SafePrimeTable.safePrime(SafePrimeTable.MAX_BIT_LENGTH + 1);
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on too small bit length!";
    }
}
