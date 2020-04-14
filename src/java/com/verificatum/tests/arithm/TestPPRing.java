
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

import java.util.Arrays;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PPRing;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PPRing}.
 *
 * @author Douglas Wikstrom
 */
public class TestPPRing extends TestPRing {

    /**
     * Constructs an asymmetric product ring of multiplicative
     * rings.
     *
     * @param bitLength Bit length of modulus of basic multiplicative
     * ring.
     * @return Ring for testing.
     * @throws ArithmFormatException If construction of the ring
     * failed.
     */
    public static PRing genPRing(final int bitLength)
        throws ArithmFormatException {
        final PField pField = new PField(SafePrimeTable.safePrime(bitLength));
        return new PPRing(new PPRing(pField, pField), pField);
    }

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPPRing(final TestParameters tp)
        throws ArithmFormatException {
        super(TestPPRing.genPRing(512),
              TestPPRing.genPRing(640),
              new PField(SafePrimeTable.safePrime(512)),
              tp);
    }

    /**
     * Equals.
     */
    public void equality() {
        super.equality();
        final PPRing pPRingA = new PPRing(pRing, pRing);
        final PPRing pPRingB = new PPRing(pRing, pRing, pRing);

        assert !pPRingA.equals(pPRingB)
            : "Inequality due to different width failed!";
    }

    /**
     * Constructors.
     */
    public void constructors() {

        final PPRing pPRingA = new PPRing(pRing, 3);
        final PPRing pPRingB = new PPRing(pRing, pRing, pRing);
        assert pPRingA.equals(pPRingB)
            : "Failed to get equal rings using different constructors!";
    }

    /**
     * Factoring.
     */
    public void factor() {

        final PPRing pPRingA = new PPRing(pRing, pRing);
        final PRing[] factors = pPRingA.getFactors();
        final PPRing pPRingB = new PPRing(factors);
        assert pPRingA.equals(pPRingB)
            : "Failed to recover product ring from factors!";
    }

    /**
     * Projection.
     */
    public void project() {
        boolean[] mask;

        final PPRing pPRingA = new PPRing(pRing.getPField(), pRing, pRing);
        final PRing[] pRings = pPRingA.getFactors();
        for (int i = 0; i < pRings.length; i++) {
            assert pPRingA.project(i).equals(pRings[i])
                : "Simple projection failed!";
        }

        for (int i = 1; i < 8; i++) {
            mask = new boolean[3];
            for (int j = 0; j < 3; j++) {
                mask[j] = (i & (0x1 << j)) != 1;
            }
            pPRingA.project(mask);
        }

        mask = new boolean[3];
        for (int i = 0; i < 3; i++) {
            Arrays.fill(mask, false);
            mask[i] = true;
            assert pPRingA.project(mask).equals(pRings[i])
                : "Projection to single position failed!";
        }

        // Fail on empty projection.
        boolean invalid = false;
        try {
            mask = new boolean[3];
            Arrays.fill(mask, false);
            pPRingA.project(mask);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on empty projection!";

        // Fail projection mask of wrong size.
        invalid = false;
        try {
            mask = new boolean[2];
            pPRingA.project(mask);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on projection mask of wrong length!";
    }
}
