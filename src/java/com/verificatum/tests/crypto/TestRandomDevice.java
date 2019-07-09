
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
