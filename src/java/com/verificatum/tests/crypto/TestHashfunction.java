
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

import com.verificatum.crypto.Hashfunction;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link Hashfunction}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestHashfunction extends TestClass {

    /**
     * Primary testing hashfunction.
     */
    protected Hashfunction hashfunction;

    /**
     * Secondary testing hashfunction.
     */
    protected Hashfunction hashfunction2;

    /**
     * Construct.
     *
     * @param tp Test parameters.
     * @param hashfunction Primary testing hashfunction.
     * @param hashfunction2 Secondary testing hashfunction.
     */
    public TestHashfunction(final TestParameters tp,
                            final Hashfunction hashfunction,
                            final Hashfunction hashfunction2) {
        super(tp);
        this.hashfunction = hashfunction;
        this.hashfunction2 = hashfunction2;
    }

    /**
     * Equals.
     *
     * @throws EIOException If the test failed.
     */
    public void equality()
        throws EIOException {
        assert !hashfunction.equals(new Object())
            : "Inequality with non hashfunction!";
        assert hashfunction.equals(hashfunction)
            : "Equality by reference failed!";

        final ByteTreeBasic bt = Marshalizer.marshal(hashfunction);
        final ByteTreeReader btr = bt.getByteTreeReader();
        final Hashfunction hashfunctionCopy =
            Marshalizer.unmarshalAux_Hashfunction(btr, rs, 50);
        assert hashfunctionCopy.equals(hashfunction)
            : "Equality by value failed!";

        assert !hashfunction.equals(hashfunction2)
            : "Inequality failed!";
    }

    /**
     * Exercise toString.
     */
    public void excToString() {
        hashfunction.toString();
    }

    /**
     * Exercise getOutputLength.
     */
    public void excGetOutputLength() {
        hashfunction.getOutputLength();
    }

    /**
     * Exercise humanDescription.
     */
    public void excHumanDescription() {
        hashfunction.humanDescription(true);
    }

    /**
     * Exercise hashCode.
     */
    public void excHashcode() {
        hashfunction.hashCode();
    }

    /**
     * Verify that the hash function can be used to hash bytes.
     */
    public void hashing() {

        final int size = 1;

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final byte[] input = rs.getBytes(size);
            hashfunction.hash(input);
        }
    }
}
