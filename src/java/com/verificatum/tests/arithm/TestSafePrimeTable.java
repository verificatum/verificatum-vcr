
/*
 * Copyright 2008-2018 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * VCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * VCR is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with VCR. If not, see <http://www.gnu.org/licenses/>.
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
