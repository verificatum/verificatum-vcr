
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
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.CryptoKeyGenNaorYung;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.test.TestParameters;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link CryptoKeyGenNaorYung}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public final class TestCryptoKeyGenNaorYung extends TestCryptoKeyGen {

    /**
     * Generates a key generator.
     *
     * @param tp Test parameters.
     * @return Key generator.
     * @throws ArithmFormatException If construction of generator fails.
     */
    public static CryptoKeyGenNaorYung keyGen(final TestParameters tp)
        throws ArithmFormatException {
        final int secpro = 256;
        final PGroup pGroup = new ModPGroup(512);
        final Hashfunction roh = new HashfunctionHeuristic("SHA-256");
        return new CryptoKeyGenNaorYung(pGroup, roh, secpro);
    }

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test fails.
     */
    public TestCryptoKeyGenNaorYung(final TestParameters tp)
        throws ArithmFormatException {
        super(tp, keyGen(tp));
    }

    /**
     * Exercise getPGroup.
     */
    public void excGetPGroup() {
        ((CryptoKeyGenNaorYung) keyGen).getPGroup();
    }

    /**
     * Exercise getRandomOracleHashfunction.
     */
    public void excGetRandomOracleHashfunction() {
        ((CryptoKeyGenNaorYung) keyGen).getRandomOracleHashfunction();
    }
}
