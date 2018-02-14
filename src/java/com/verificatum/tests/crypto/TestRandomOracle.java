
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

import java.util.Arrays;

import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.crypto.RandomOracle;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link RandomOracle}.
 *
 * @author Douglas Wikstrom
 */
public final class TestRandomOracle extends TestHashfunction {

    /**
     * Constructor needed to avoid that this class is instantiated.
     *
     * @param tp Test parameters configuration of the servers.
     */
    public TestRandomOracle(final TestParameters tp) {
        super(tp,
              new RandomOracle(new HashfunctionHeuristic("SHA-256"), 700),
              new RandomOracle(new HashfunctionHeuristic("SHA-384"), 700));
    }

    /**
     * newInstance.
     *
     * @throws CryptoFormatException If a test fails.
     */
    public void newInstance()
        throws CryptoFormatException {

        ByteTreeBasic bt = hashfunction.toByteTree();

        final RandomOracle ro =
            RandomOracle.newInstance(bt.getByteTreeReader(), rs, 20);

        final byte[] input = rs.getBytes(100);
        final byte[] output = hashfunction.hash(input);
        final byte[] output2 = ro.hash(input);

        assert Arrays.equals(output, output2) : "Failed to create instance!";

        boolean invalid = false;
        try {
            bt = new ByteTree(new byte[1]);
            RandomOracle.newInstance(bt.getByteTreeReader(), rs, 20);
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }

    /**
     * Equals.
     *
     * @throws EIOException If the test failed.
     */
    @Override
    public void equality()
        throws EIOException {
        super.equality();

        final Hashfunction hashfunction = new HashfunctionHeuristic("SHA-256");
        final RandomOracle ro1 = new RandomOracle(hashfunction, 150);
        final RandomOracle ro2 = new RandomOracle(hashfunction, 151);

        assert !ro1.equals(ro2) : "Failed to fail on different lengths!";
    }
}

