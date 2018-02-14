
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
