
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
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.crypto.CryptoError;
import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.PRGElGamal;
import com.verificatum.eio.ByteTree;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PRGElGamal}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPRGElGamal extends TestPRG {

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     * @throws CryptoFormatException If construction of the test
     * failed.
     */
    public TestPRGElGamal(final TestParameters tp)
        throws ArithmFormatException, CryptoFormatException {
        super(tp);
    }

    /**
     * Constructors.
     *
     * @throws ArithmFormatException If a test fails.
     * @throws CryptoFormatException If a test fails.
     */
    public void constructors()
        throws ArithmFormatException, CryptoFormatException {

        final LargeInteger[] modulus = new LargeInteger[3];
        modulus[0] = SafePrimeTable.safePrime(512);
        modulus[1] = modulus[0].add(LargeInteger.ONE);
        modulus[2] = modulus[1].neg();

        for (int i = 0; i < modulus.length; i++) {
            boolean invalid = false;
            try {
                new PRGElGamal(modulus[i], i + 1, 0);
            } catch (final CryptoError ce) {
                invalid = true;
            }
            assert invalid : "Failed to fail on bad parameters! (" + i + ")";
        }
    }

    @Override
    protected PRG[] prgs() throws ArithmFormatException, CryptoFormatException {
        final PRG[] prgs = new PRG[2];

        int bitLength = 512;
        for (int j = 0; j < prgs.length; j++) {
            final LargeInteger safePrime = SafePrimeTable.safePrime(bitLength);
            prgs[j] = new PRGElGamal(safePrime, 50);
            bitLength <<= 1;
        }
        return prgs;
    }

    /**
     * newInstance.
     */
    public void newInstance() {

        boolean invalid = false;
        try {
            final ByteTree bt = new ByteTree(new byte[1]);
            PRGElGamal.newInstance(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }
}
