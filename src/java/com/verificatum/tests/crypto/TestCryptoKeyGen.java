
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
