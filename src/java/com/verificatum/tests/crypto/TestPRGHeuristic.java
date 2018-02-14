
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

package com.verificatum.tests.crypto;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.eio.ByteTree;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PRGHeuristic}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPRGHeuristic extends TestPRG {

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     * @throws CryptoFormatException If construction of the test
     * failed.
     */
    public TestPRGHeuristic(final TestParameters tp)
        throws ArithmFormatException, CryptoFormatException {
        super(tp);
    }

    @Override
    protected PRG[] prgs()
        throws ArithmFormatException, CryptoFormatException {
        final PRG[] prgs = new PRG[1];
        prgs[0] = new PRGHeuristic();
        return prgs;
    }

    /**
     * newInstance.
     */
    public void newInstance() {

        boolean invalid = false;
        try {
            final ByteTree bt = new ByteTree(new byte[1]);
            PRGHeuristic.newInstance(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }
}
