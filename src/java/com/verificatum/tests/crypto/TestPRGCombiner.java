
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

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.PRGCombiner;
import com.verificatum.crypto.PRGElGamal;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomDevice;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PRGCombiner}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public final class TestPRGCombiner extends TestPRG {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     * @throws CryptoFormatException If construction of the test
     * failed.
     */
    public TestPRGCombiner(final TestParameters tp)
        throws ArithmFormatException, CryptoFormatException {
        super(tp);
    }

    @Override
    protected PRG[] prgs()
        throws ArithmFormatException, CryptoFormatException {
        final PRG[] prgs = new PRG[1];
        prgs[0] = new PRGCombiner(new RandomDevice(), new PRGHeuristic());
        return prgs;
    }

    @Override
    public void marshal() throws Exception {

        final LargeInteger safePrime = SafePrimeTable.safePrime(512);

        final PRG elg = new PRGElGamal(safePrime, 2, 0);
        final PRG prg = new PRGCombiner(elg, new PRGHeuristic());

        final ByteTreeBasic byteTree = Marshalizer.marshal(prg);
        final PRG prg2 =
            Marshalizer.unmarshalAux_PRG(byteTree.getByteTreeReader(),
                                             rs, 10);

        final byte[] seedBytes = rs.getBytes(prg.minNoSeedBytes());

        final int size = 10;
        prg.setSeed(seedBytes);
        final byte[] r1 = prg.getBytes(size);

        prg2.setSeed(seedBytes);
        final byte[] r2 = prg2.getBytes(size);

        assert Arrays.equals(r1, r2) : "Marshalling failed!";
    }

    /**
     * newInstance.
     */
    public void newInstance() {

        boolean invalid = false;
        try {
            final ByteTree bt = new ByteTree(new byte[1]);
            PRGCombiner.newInstance(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }
}
