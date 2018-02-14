
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

package com.verificatum.tests.arithm;

import java.util.Arrays;

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PGroupElementArray;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PRingElementArray;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link PGroupElement}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestPGroupElement extends TestClass {

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
     * Initializes the group for testing.
     *
     * @param pGroup Group used for testing.
     * @param pGroup2 Secondary group used for failure testing.
     * @param otherPGroup Group from other class used for failure
     * testing.
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    protected TestPGroupElement(final PGroup pGroup,
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
     * Exercise toString.
     */
    public void excToString() {
        final PGroupElement x = pGroup.randomElement(rs, 50);
        x.toString();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCode() {
        final PGroupElement x = pGroup.randomElement(rs, 50);
        x.hashCode();
    }

    /**
     * Excercise toByteArray.
     */
    public void excToByteArray() {
        final PGroupElement a = pGroup.randomElement(rs, 10);
        a.toByteArray();
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTree()
        throws ArithmFormatException {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElement e1 = pGroup.randomElement(rs, 50);
            final ByteTreeBasic bt = e1.toByteTree();

            ByteTreeReader btr = bt.getByteTreeReader();
            PGroupElement e2 = pGroup.toElement(btr);
            assert e1.equals(e2)
                : "Failed to convert group element and byte tree!";

            btr = bt.getByteTreeReader();
            e2 = pGroup.toElement(btr);
            assert e1.equals(e2)
                : "Failed to unsafely convert group element and byte tree!";
        }

        final ByteTree bt = new ByteTree();
        final ByteTreeReader btr = bt.getByteTreeReader();
        boolean invalid = false;
        try {
            pGroup.toElement(btr);
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }

    /**
     * Encode.
     */
    public void encode() {
        for (int len = 1; len <= pGroup.getEncodeLength(); len++) {

            final byte[] a = rs.getBytes(len);
            final PGroupElement x = pGroup.encode(a, 0, a.length);
            final byte[] b = x.decode();

            assert Arrays.equals(a, b);
        }
    }

    /**
     * Verifies that arithmetic can be carried out correctly.
     *
     * @throws ArithmException If a test failed.
     */
    public void arithmetic()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElement a = pGroup.randomElement(rs, 50);

            assert a.mul(pGroup.getONE()).equals(a)
                : "Multiplication with one failed!";

            final PGroupElement b = pGroup.randomElement(rs, 50);

            assert a.inv().mul(a).equals(pGroup.getONE())
                : "Value multiplied by inverse does not give one!";

            assert a.mul(b).equals(b.mul(a))
                : "Multiplication is not commutative!";
        }

        final PGroupElement a = pGroup.randomElement(rs, 10);
        final PGroupElement b = pGroup2.randomElement(rs, 10);
        final PGroupElement c = otherPGroup.randomElement(rs, 10);

        boolean invalid = false;
        try {
            a.mul(b);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on elements from groups with "
            + "different parameters!";

        invalid = false;
        try {
            a.mul(c);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert true : "Failed to fail on elements from different groups!";
    }


    /**
     * Verifies that arithmetic can be carried out correctly.
     */
    private void exp() {

        final Timer timer = new Timer(testTime);

        final int size = 1;

        while (!timer.timeIsUp()) {

            final PGroupElement b = pGroup.randomElement(rs, 10);

            final PRingElementArray e =
                pGroup.getPRing().randomElementArray(size, rs, 10);
            final PRingElement[] es = e.elements();

            final PGroupElementArray r = b.exp(e);
            final PGroupElement[] rs = r.elements();

            final PGroupElement[] rss = b.exp(es);

            for (int i = 0; i < rs.length; i++) {
                assert rs[i].equals(b.exp(es[i])) : "Exponentiation failed!";
                assert rss[i].equals(rs[i]) : "Exponentiation failed!";
            }

            e.free();
            r.free();
        }

        final PGroupElement b = pGroup.randomElement(rs, 10);

        final PRingElementArray e =
            pGroup2.getPRing().randomElementArray(size, rs, 10);
        boolean invalid = false;
        try {
            b.exp(e);
        } catch (final ArithmError ae) {
            invalid = true;
        }

        assert invalid
            : "Failed to fail on mismatching group and ring!";

        e.free();
    }

    /**
     * Verifies that arithmetic can be carried out correctly.
     */
    public void expM() {
        TestLargeIntegerArray.memoryBased();
        exp();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Verifies that arithmetic can be carried out correctly.
     */
    public void expF() {
        TestLargeIntegerArray.fileBased(TestPGroupElementArray.TEST_BATCH_SIZE);
        exp();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Arithmetic sugar.
     */
    public void arithmeticSugar() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElement x = pGroup.randomElement(rs, 50);
            final PGroupElement y = pGroup.randomElement(rs, 50);

            assert x.div(y).equals(x.mul(y.inv())) : "Division failed!";

            final LargeInteger a = new LargeInteger(20, rs);
            final int aa = a.intValue();

            assert x.exp(aa).equals(x.exp(a))
                : "Int exponentiation int failed!";

            final PRingElement b = pGroup.getPRing().randomElement(rs, 50);

            assert x.exp(b).mul(y).equals(x.expMul(b, y)) : "Exp-mul failed!";
        }
    }

    /**
     * Arithmetic sugar.
     */
    public void compareTo() {

        final Timer timer = new Timer(testTime);

        // Standard case.
        while (!timer.timeIsUp()) {

            final PGroupElement x = pGroup.randomElement(rs, 10);
            final PGroupElement y = pGroup.randomElement(rs, 10);

            assert x.compareTo(x) == 0 : "Equality failed!";

            final int cmp = x.compareTo(y);
            assert Math.abs(cmp) == 1 : "Inequality failed!";
        }

        final PGroupElement x = pGroup.randomElement(rs, 10);

        final PGroupElement y = pGroup2.randomElement(rs, 10);
        boolean invalid = false;
        try {
            x.compareTo(y);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on elements from groups of "
            + "different parameters!";

        final PGroupElement z = otherPGroup.randomElement(rs, 10);
        invalid = false;
        try {
            x.compareTo(z);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on elements from distinct groups!";
    }

    /**
     * Equals.
     */
    public void equality() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElement x = pGroup.randomElement(rs, 30);
            final PGroupElement y = x.mul(pGroup.getONE());
            final PGroupElement z = pGroup.randomElement(rs, 30);

            assert x.equals(y) : "Failed to check equality of elements!";
            assert !x.equals(z) : "Failed to check inequality of elements!";
        }
        assert !pGroup.getONE().equals(new Object())
            : "Failed to fail on equality with non-field element!";

        final PGroupElement x = pGroup.randomElement(rs, 30);
        assert x.equals(x) : "Failed to compare references!";

        final PGroupElement y = pGroup2.randomElement(rs, 30);
        assert !x.equals(y)
            : "Failed to fail on equality of elements from distinct groups!";
    }

    /**
     * Arithmetic sugar.
     */
    public void naiveExp() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {
            final PGroupElement x = pGroup.randomElement(rs, 10);
            final PRingElement[] e =
                pGroup.getPRing().randomElements(size, rs, 10);

            final PGroupElement[] y = x.naiveExp(e);
            for (int i = 0; i < e.length; i++) {
                assert x.exp(e[i]).equals(y[i])
                    : "Naive exponentiation failed!";
            }

            size++;
        }
    }

    /**
     * Arithmetic sugar.
     */
    public void naiveExpProd() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {
            final PGroupElement[] b = pGroup.randomElements(size, rs, 10);
            final PRingElement[] e =
                pGroup.getPRing().randomElements(size, rs, 10);

            PGroupElement x = pGroup.naiveExpProd(b, e);

            PGroupElement res = pGroup.getONE();
            for (int i = 0; i < b.length; i++) {
                res = res.mul(b[i].exp(e[i]));
            }

            assert x.equals(res) : "Naive power product failed!";

            final int bitLength =
                pGroup.getPRing().getPField().getOrder().bitLength();
            final LargeInteger[] ei = LargeInteger.random(size, bitLength, rs);

            x = pGroup.expProd(b, ei, bitLength);

            res = pGroup.getONE();
            for (int i = 0; i < b.length; i++) {
                res = res.mul(b[i].exp(ei[i]));
            }

            assert x.equals(res) : "Naive power product failed!";

            size++;
        }
    }
}
