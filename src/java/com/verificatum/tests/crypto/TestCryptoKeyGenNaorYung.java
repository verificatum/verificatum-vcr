
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
