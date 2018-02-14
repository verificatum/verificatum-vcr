
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
import com.verificatum.crypto.CryptoKeyGenNaorYung;
import com.verificatum.crypto.CryptoKeyPair;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ExtIO;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link CryptoKeyPair} for Naor-Yung keys.
 *
 * @author Douglas Wikstrom
 */
public final class TestCryptoKeyPairNaorYung extends TestCryptoKeyPair {

    /**
     * Generates key pair used for testing.
     *
     * @param tp Test parameters.
     * @return Key pair used for testing.
     * @throws ArithmFormatException If a test fails.
     */
    public static CryptoKeyPair keyPair(final TestParameters tp)
        throws ArithmFormatException {
        final CryptoKeyGenNaorYung keyGen =
            TestCryptoKeyGenNaorYung.keyGen(tp);
        final RandomSource rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
        return keyGen.gen(rs, 10);
    }

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If a test fails.
     */
    public TestCryptoKeyPairNaorYung(final TestParameters tp)
        throws ArithmFormatException {
        super(tp, keyPair(tp));
    }
}
