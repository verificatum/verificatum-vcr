
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
