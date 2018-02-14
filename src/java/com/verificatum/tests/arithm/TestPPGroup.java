
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
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PPGroup;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class TestPPGroup extends TestPGroup {

    /**
     * Constructs an asymmetric product group of multiplicative
     * groups.
     *
     * @param bitLength Bit length of modulus of basic multiplicative
     * group.
     * @return Group used for testing.
     * @throws ArithmFormatException If construction of the group
     * failed.
     */
    public static PGroup genPGroup(final int bitLength)
        throws ArithmFormatException {
        final PGroup pGroup = new ModPGroup(bitLength);
        return new PPGroup(new PPGroup(pGroup, pGroup), pGroup);
    }

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPPGroup(final TestParameters tp)
        throws ArithmFormatException {
        super(genPGroup(512),
              genPGroup(640),
              ECqPGroupParams.getECqPGroup("P-256"),
              tp);
    }

    @Override
    protected PGroup[] encodingPGroups()
        throws ArithmFormatException {

        final int bitLength = 512;
        final PGroup pGroup = new ModPGroup(bitLength);

        final PGroup[] pGroups = new PGroup[3];

        pGroups[0] = new PPGroup(new PPGroup(pGroup, pGroup), pGroup);
        pGroups[1] = new PPGroup(new PPGroup(pGroup, pGroup), pGroups[0]);
        pGroups[2] = new PPGroup(pGroups[0], pGroups[1]);

        return pGroups;
    }

    @Override
    protected PGroup newInstance(final ByteTreeReader btr,
                                 final RandomSource rs)
        throws ArithmFormatException {
        return PPGroup.newInstance(btr, rs, 20);
    }
}
