
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

import java.io.File;

import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;
import com.verificatum.test.TestClass;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link RandomDevice}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public final class TestRandomDevice extends TestClass {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestRandomDevice(final TestParameters tp) {
        super(tp);
    }

    /**
     * Equals.
     */
    public void equality() {

        final RandomSource urandom =
            new RandomDevice(new File("/dev/urandom"));
        final RandomSource urandomCopy =
            new RandomDevice(new File("/dev/urandom"));
        final RandomSource random =
            new RandomDevice(new File("/dev/random"));

        assert urandom.equals(urandom) : "Equality by reference failed!";
        assert urandomCopy.equals(urandom) : "Equality by value failed!";
        assert !urandom.equals(random) : "Inequality failed!";
    }

    /**
     * Verify that generation works.
     *
     * @throws Exception If a test fails.
     */
    public void generate() throws Exception {

        final RandomSource rs = new RandomDevice();

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            rs.getBytes(size);

            size++;
        }
    }

    /**
     * Verify conversion to byte tree.
     *
     * @throws Exception If a test fails.
     */
    public void marshal() throws Exception {

        final RandomSource rs1 = new RandomDevice();

        final ByteTreeBasic bt = Marshalizer.marshal(rs1);
        final RandomSource rs2 =
            Marshalizer.unmarshal_RandomSource(bt.getByteTreeReader());

        rs1.getBytes(100);
        rs2.getBytes(100);
    }

    /**
     * Exercise human description.
     */
    public void excHumanDescription() {
        new RandomDevice().humanDescription(true);
    }
}
