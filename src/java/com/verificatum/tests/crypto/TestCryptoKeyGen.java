
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
import com.verificatum.crypto.CryptoKeyGen;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.test.TestClass;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests {@link CryptoKeyGen}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestCryptoKeyGen extends TestClass {

    /**
     * Key generator used for testing.
     */
    final CryptoKeyGen keyGen;

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @param keyGen Key generator used for testing.
     * @throws ArithmFormatException If construction of the test fails.
     */
    public TestCryptoKeyGen(final TestParameters tp,
                            final CryptoKeyGen keyGen)
        throws ArithmFormatException {
        super(tp);
        this.keyGen = keyGen;
    }

    /**
     * Marshalize key generator.
     *
     * @throws EIOException If a test fails.
     */
    public void equalsAndMarshal()
        throws EIOException {

        assert keyGen.equals(keyGen)
            : "Equality by reference failed!";

        assert !keyGen.equals(new Object())
            : "Inequality with instance of wrong class failed!";

        final ByteTreeBasic keyGenBT = Marshalizer.marshal(keyGen);
        final ByteTreeReader reader = keyGenBT.getByteTreeReader();

        final CryptoKeyGen keyGen2 =
            (CryptoKeyGen)
            Marshalizer.unmarshalAux_CryptoKeyGen(reader, rs, 10);

        assert keyGen2.equals(keyGen)
            : "Failed to marshal or equality by value failed!";
    }

    /**
     * Exercise toString.
     */
    public void excToString() {
        keyGen.toString();
    }

    /**
     * Exercise humanDescription.
     */
    public void excHumanDescription() {
        keyGen.humanDescription(true);
    }

    /**
     * Exercise hashCode.
     */
    public void excHashCode() {
        keyGen.hashCode();
    }
}
