
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
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.ModPGroupElement;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link ModPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class TestModPGroup extends TestPGroup {

    /**
     * Group used in tests.
     */
    protected ModPGroup modPGroup;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestModPGroup(final TestParameters tp)
        throws ArithmFormatException {
        super(new ModPGroup(512),
              new ModPGroup(740),
              ECqPGroupParams.getECqPGroup("P-256"),
              tp);
        this.modPGroup = (ModPGroup) pGroup;
    }

    @Override
    protected PGroup[] encodingPGroups()
        throws ArithmFormatException {

        final int bitLength = 512;

        final PGroup[] pGroups = new PGroup[3];

        // Group from bitlength.
        pGroups[0] = new ModPGroup(bitLength);

        // Group from random byte array.
        final int byteLength = 2 * ((bitLength + 7) / 8) + 10;
        final byte[] randomBytes = rs.getBytes(byteLength);
        pGroups[1] = new ModPGroup(bitLength, randomBytes, rs, 20);

        // Random small group with random encoding.
        final int obitLength = bitLength / 2;
        pGroups[2] = new ModPGroup(bitLength, obitLength,
                                   ModPGroup.RO_ENCODING, rs, 20);

        return pGroups;
    }

    @Override
    protected PGroup newInstance(final ByteTreeReader btr,
                                 final RandomSource rs)
        throws ArithmFormatException {
        return ModPGroup.newInstance(btr, rs, 20);
    }

    /**
     * To integer and contains integer.
     */
    public void containsAndToInteger() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final ModPGroupElement x =
                (ModPGroupElement) pGroup.randomElement(rs, 20);
            LargeInteger a = x.toLargeInteger();
            assert modPGroup.contains(a)
                : "Integer representative not in group!";

            assert !modPGroup.contains(LargeInteger.ZERO)
                : "Failed to verify lower bound!";

            final LargeInteger ub =
                modPGroup.getModulus().add(LargeInteger.ONE);
            assert !modPGroup.contains(ub) : "Failed to verify upper bound!";

            a = LargeInteger.ONE;
            boolean res = true;
            for (int i = 0; res && i < 100; i++) {
                a = a.add(LargeInteger.ONE);
                res = modPGroup.contains(a);
            }
            assert !res : "Failed to identify non-representative integers!";
        }
    }
}
