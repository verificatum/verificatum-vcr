
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

import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionFixedLength;
import com.verificatum.crypto.HashfunctionMerkleDamgaard;
import com.verificatum.crypto.HashfunctionPedersen;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link HashfunctionMerkleDamgaard}.
 *
 * @author Douglas Wikstrom
 */
public final class TestHashfunctionMerkleDamgaard extends TestHashfunction {

    /**
     * Construct hash function.
     *
     * @param bitLength Logarithm of order of underlying group.
     * @param tp Test parameters.
     * @return Hash function for testing.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    protected static Hashfunction hashfunction(final int bitLength,
                                               final TestParameters tp)
        throws ArithmFormatException {
        final RandomSource rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
        final PGroup pGroup = new ModPGroup(bitLength);
        final HashfunctionFixedLength flhf =
            new HashfunctionPedersen(pGroup, 2, rs, 50);
        return new HashfunctionMerkleDamgaard(flhf);
    }

    /**
     * Constructor needed to avoid that this class is instantiated.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestHashfunctionMerkleDamgaard(final TestParameters tp)
    throws ArithmFormatException {
        super(tp, hashfunction(512, tp), hashfunction(768, tp));
    }

    /**
     * Verify that the hash function can be marshalled.
     *
     * @throws EIOException If a test fails.
     */
    public void marshal() throws EIOException {

        final ByteTreeBasic bt = Marshalizer.marshal(hashfunction);

        final ByteTreeReader btr = bt.getByteTreeReader();

        final Hashfunction hashfunction2 =
            Marshalizer.unmarshalAux_Hashfunction(btr, rs, 50);

        int size = 1;

        final Timer timer = new Timer(tp.milliSeconds);

        while (!timer.timeIsUp()) {

            final byte[] input = rs.getBytes(size);

            final byte[] output1 = hashfunction.hash(input);
            final byte[] output2 = hashfunction2.hash(input);

            assert Arrays.equals(output1, output2)
                : "Failed to marshal hash function!";

            size++;
        }
    }
}
