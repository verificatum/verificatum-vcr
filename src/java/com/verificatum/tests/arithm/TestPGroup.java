
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

package com.verificatum.tests.arithm;

import java.util.Arrays;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PGroupElementArray;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests {@link PGroup}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public abstract class TestPGroup extends TestClass {

    /**
     * Group used for testing.
     */
    protected PGroup pGroup;

    /**
     * Secondary group used for failure testing.
     */
    protected PGroup pGroup2;

    /**
     * Group of different class used for failure testing.
     */
    protected PGroup otherPGroup;

    /**
     * Group of different class used for failure testing.
     */
    protected PGroup[] encodePGroups;

    /**
     * Groups for constructors and encoding.
     */
    protected PGroup[] pGroups;

    /**
     * Constructs test.
     *
     * @param pGroup Group used for testing.
     * @param pGroup2 Secondary group used for failure testing.
     * @param otherPGroup Group from other class used for failure
     * testing.
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    protected TestPGroup(final PGroup pGroup,
                         final PGroup pGroup2,
                         final PGroup otherPGroup,
                         final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.pGroup = pGroup;
        this.pGroup2 = pGroup2;
        this.otherPGroup = otherPGroup;
    }

    /**
     * Byte tree.
     *
     * @return Various groups for testing encoding.
     * @throws ArithmFormatException If a test failed.
     */
    protected abstract PGroup[] encodingPGroups()
        throws ArithmFormatException;

    /**
     * Recover instance from byte tree.
     *
     * @param btr Source of byte tree.
     * @param rs Source of randomness.
     * @return Recovered group.
     * @throws ArithmFormatException If construction failed.
     */
    protected abstract PGroup newInstance(final ByteTreeReader btr,
                                          final RandomSource rs)
        throws ArithmFormatException;

    /**
     * Constructors.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void applyConstructors()
        throws ArithmFormatException {
        if (pGroups == null) {
            this.pGroups = encodingPGroups();
        }
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If failing test.
     */
    public void byteTree()
        throws ArithmFormatException {
        if (pGroups == null) {
            this.pGroups = encodingPGroups();
        }

        for (int i = 0; i < pGroups.length; i++) {
            final ByteTreeBasic bt = pGroups[0].toByteTree();
            final PGroup gpc = newInstance(bt.getByteTreeReader(), rs);
            assert gpc.equals(pGroups[0]) : "Construct and recover failed!";
        }
    }

    /**
     * Encoding.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void encode()
        throws ArithmFormatException {
        if (pGroups == null) {
            this.pGroups = encodingPGroups();
        }

        for (int i = 0; i < pGroups.length; i++) {
            final int encodeLength = pGroups[i].getEncodeLength();

            final byte[] a = rs.getBytes(encodeLength);
            final PGroupElement x = pGroups[i].encode(a, 0, encodeLength);
            final byte[] b = x.decode();

            assert Arrays.equals(a, b) : "Failed to encode or decode in group "
                + i + "!";
        }
    }

    /**
     * Div.
     */
    public void div() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PGroupElement[] n = pGroup.randomElements(size, rs, 10);
            final PGroupElement[] d = pGroup.randomElements(size, rs, 10);

            final PGroupElement[] r = pGroup.div(n, d);

            for (int i = 0; i < n.length; i++) {
                assert r[i].equals(n[i].div(d[i])) : "Failed to divide";
            }

            size++;
        }
    }

    /**
     * Power product of arrays.
     */
    public void expProd() {

        final Timer timer = new Timer(testTime);

        int width = 1;
        int size = 1;
        final int bitLength =
            pGroup.getPRing().getPField().getOrder().bitLength();

        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(width, bitLength, rs);
            final LargeInteger[] y = new LargeInteger[x.length];
            for (int i = 0; i < width; i++) {
                y[i] = x[i].neg();
            }

            final PGroupElementArray[] b = new PGroupElementArray[width];
            final PGroupElement[][] bb = new PGroupElement[width][];
            for (int i = 0; i < width; i++) {
                b[i] = pGroup.randomElementArray(size, rs, 10);
                bb[i] = b[i].elements();
            }

            final PGroupElementArray r = pGroup.expProd(b, x, bitLength);
            final PGroupElementArray t = pGroup.expProd(b, y, bitLength);

            final PGroupElement[] ss = new PGroupElement[size];
            for (int i = 0; i < size; i++) {
                ss[i] = pGroup.getONE();
                for (int j = 0; j < width; j++) {
                    ss[i] = ss[i].mul(bb[j][i].exp(x[j]));
                }
            }
            final PGroupElementArray s = pGroup.toElementArray(ss);

            assert r.equals(s) : "Failed to compute power product of arrays!";

            assert r.inv().equals(t)
                : "Failed to computer power product of arrays with negative "
                + "exponents!";

            size++;
            if (size % 2 == 0) {
                width++;
            }

            for (int i = 0; i < b.length; i++) {
                b[i].free();
            }
            r.free();
            t.free();
            s.free();
        }
    }

    /**
     * Equals for arrays.
     */
    public void equalsArrays() {

        final int size = 10;

        final PGroupElement[] x = pGroup.randomElements(size, rs, 10);
        final PGroupElement[] y = pGroup.randomElements(size, rs, 10);
        final PGroupElement[] z = pGroup.randomElements(size + 1, rs, 10);

        assert pGroup.equals(x, x) : "Equality failed!";
        assert !pGroup.equals(x, y) : "Inequality failed!";
        assert !pGroup.equals(x, z) : "Inequality of sizes failed!";
    }

    /**
     * Exercise threshold getters and setters.
     */
    public void excThresholds() {
        pGroup.setExpThreadThreshold(pGroup.getExpThreadThreshold());
        pGroup.setMulThreadThreshold(pGroup.getMulThreadThreshold());
    }

    /**
     * Exercise toString.
     */
    public void excToString() {
        pGroup.toString();
    }

    /**
     * Exercise getPrimeOrderGroup.
     */
    public void excGetPrimeOrderGroup() {
        pGroup.getPrimeOrderPGroup();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCode() {
        pGroup.hashCode();
    }

    /**
     * Exercises humanDescription.
     */
    public void excHumanDescription() {
        pGroup.humanDescription(true);
    }
}
