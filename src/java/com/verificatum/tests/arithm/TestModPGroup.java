
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
