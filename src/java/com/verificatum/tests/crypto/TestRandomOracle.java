
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

