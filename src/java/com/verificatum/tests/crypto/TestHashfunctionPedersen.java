
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
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.HashfunctionFixedLength;
import com.verificatum.crypto.HashfunctionPedersen;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.test.TestClass;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link HashfunctionPedersen}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public final class TestHashfunctionPedersen extends TestClass {

    /**
     * Hash function used for testing.
     */
    final HashfunctionFixedLength hf;

    /**
     * Constructs test.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws ArithmFormatException If the test fails.
     */
    public TestHashfunctionPedersen(final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        final PGroup pGroup = new ModPGroup(512);
        hf = new HashfunctionPedersen(pGroup, 2, rs, 50);
    }

    /**
     * Marshalling.
     *
     * @throws EIOException If a test fails.
     */
    public void marshal()
        throws EIOException {

        final ByteTreeBasic bt = Marshalizer.marshal(hf);
        final ByteTreeReader btr = bt.getByteTreeReader();
        final HashfunctionFixedLength hf2 =
            Marshalizer.unmarshalAux_HashfunctionFixedLength(btr, rs, 50);

        final byte[] input = rs.getBytes(hf.getInputLength() / 8);

        final byte[] output1 = hf.hash(input);
        final byte[] output2 = hf2.hash(input);

        assert Arrays.equals(output1, output2)
            : "Failed to marshal hash function!";
    }

    /**
     * Exercise toString.
     */
    public void excToString() {
        hf.toString();
    }

    /**
     * Exercise hashCode.
     */
    public void excHashcode() {
        hf.hashCode();
    }

    /**
     * Exercise human description.
     */
    public void excHumanDescription() {
        hf.humanDescription(true);
    }

    /**
     * Equals.
     *
     * @throws EIOException If the test fails.
     * @throws ArithmFormatException If the test fails.
     */
    public void equality()
        throws ArithmFormatException, EIOException {

        final ByteTreeBasic bt = Marshalizer.marshal(hf);
        final ByteTreeReader btr = bt.getByteTreeReader();
        final HashfunctionFixedLength hf2 =
            Marshalizer.unmarshalAux_HashfunctionFixedLength(btr, rs, 50);

        assert hf.equals(hf) : "Equality based on references failed!";
        assert hf.equals(hf2) : "Equality based values references failed!";

        final PGroup pGroup = new ModPGroup(512);
        final HashfunctionFixedLength hf3 =
            new HashfunctionPedersen(pGroup, 2, rs, 50);

        assert !hf3.equals(hf) : "Inequality failed!";
        assert !hf.equals(new Object())
            : "Inequality with instance of different class failed!";
    }
}
