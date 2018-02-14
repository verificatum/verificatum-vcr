
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
