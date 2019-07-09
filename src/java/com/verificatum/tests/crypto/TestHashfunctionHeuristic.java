
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

import com.verificatum.crypto.CryptoError;
import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link HashfunctionHeuristic}.
 *
 * @author Douglas Wikstrom
 */
public final class TestHashfunctionHeuristic extends TestHashfunction {

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     */
    public TestHashfunctionHeuristic(final TestParameters tp) {
        super(tp,
              new HashfunctionHeuristic("SHA-256"),
              new HashfunctionHeuristic("SHA-384"));
    }

    /**
     * Constructors.
     *
     * @throws CryptoFormatException If a test fails.
     */
    public void constructors() throws CryptoFormatException {

        boolean invalid = false;
        try {
            new HashfunctionHeuristic("SHA-257");
        } catch (final CryptoError ce) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad algorithm name!";

        invalid = false;
        try {
            final byte[] data =
                new byte[HashfunctionHeuristic.MAX_ALGORITHM_BYTELENGTH + 1];
            final ByteTree bt = new ByteTree(data);
            HashfunctionHeuristic.newInstance(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad algorithm name!";
    }

    /**
     * Marshalling.
     *
     * @throws EIOException If a test fails.
     */
    public void marshal() throws EIOException {

        final Hashfunction hf256 = new HashfunctionHeuristic("SHA-256");
        final Hashfunction hf384 = new HashfunctionHeuristic("SHA-384");
        final Hashfunction hf512 = new HashfunctionHeuristic("SHA-512");

        final ByteTreeBasic bt256 = Marshalizer.marshal(hf256);
        final ByteTreeBasic bt384 = Marshalizer.marshal(hf384);
        final ByteTreeBasic bt512 = Marshalizer.marshal(hf512);

        final ByteTreeReader btr256 = bt256.getByteTreeReader();
        final ByteTreeReader btr384 = bt384.getByteTreeReader();
        final ByteTreeReader btr512 = bt512.getByteTreeReader();

        final Hashfunction hf256t =
            Marshalizer.unmarshalAux_Hashfunction(btr256, rs, 50);
        final Hashfunction hf384t =
            Marshalizer.unmarshalAux_Hashfunction(btr384, rs, 50);
        final Hashfunction hf512t =
            Marshalizer.unmarshalAux_Hashfunction(btr512, rs, 50);

        final int size = 1;

        final Timer timer = new Timer(tp.milliSeconds);

        while (!timer.timeIsUp()) {

            final byte[] input = rs.getBytes(size);

            assert Arrays.equals(hf256.hash(input), hf256t.hash(input))
                : "Failed to marshal SHA-256";
            assert Arrays.equals(hf384.hash(input), hf384t.hash(input))
                : "Failed to marshal SHA-384";
            assert Arrays.equals(hf512.hash(input), hf512t.hash(input))
                : "Failed to marshal SHA-512";
        }
    }
}
