
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
import com.verificatum.arithm.ECqPGroup;
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link ECqPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class TestECqPGroup extends TestPGroup {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestECqPGroup(final TestParameters tp)
        throws ArithmFormatException {
        super(ECqPGroupParams.getECqPGroup("P-256"),
              ECqPGroupParams.getECqPGroup("P-384"),
              new ModPGroup(512),
              tp);
    }

    @Override
    protected PGroup[] encodingPGroups()
        throws ArithmFormatException {

        final PGroup[] pGroups = new PGroup[4];
        pGroups[0] = ECqPGroupParams.getECqPGroup("P-192");
        pGroups[1] = ECqPGroupParams.getECqPGroup("P-256");
        pGroups[2] = ECqPGroupParams.getECqPGroup("P-384");
        pGroups[3] = ECqPGroupParams.getECqPGroup("P-521");

        return pGroups;
    }

    @Override
    protected PGroup newInstance(final ByteTreeReader btr,
                                 final RandomSource rs)
        throws ArithmFormatException {
        return ECqPGroup.newInstance(btr, rs, 20);
    }
}
