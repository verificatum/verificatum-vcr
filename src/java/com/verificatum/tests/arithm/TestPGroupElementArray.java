
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
import com.verificatum.arithm.Permutation;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PGroupElementArray;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PRingElementArray;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link PGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public class TestPGroupElementArray extends TestClass {

    /**
     * Main group used for testing.
     */
    private final PGroup pGroup;

    /**
     * Secondary group used for failure testing.
     */
    private final PGroup pGroup2;

    /**
     * Group of different class used for failure testing.
     */
    private final PGroup otherPGroup;

    /**
     * Batch used.
     */
    public static final int TEST_BATCH_SIZE = 5;

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
    protected TestPGroupElementArray(final PGroup pGroup,
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
     * Excercise hashCode.
     */
    protected void excHashCode() {
        final PGroupElementArray x = pGroup.randomElementArray(10, rs, 50);
        x.hashCode();
        x.free();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCodeM() {
        TestLargeIntegerArray.memoryBased();
        excHashCode();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCodeF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        excHashCode();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    protected void byteTree()
        throws ArithmFormatException {
        final Timer timer = new Timer(testTime);

        final int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 10);
            ByteTreeReader btr = x.toByteTree().getByteTreeReader();
            final PGroupElementArray y = pGroup.toElementArray(0, btr);

            btr = x.toByteTree().getByteTreeReader();
            final PGroupElementArray z = pGroup.unsafeToElementArray(0, btr);

            assert x.equals(y) : "Failed to convert/recover byte tree!";
            assert x.equals(z) : "Failed to convert/recover unsafe byte tree!";

            x.free();
            y.free();
            z.free();
        }
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTreeM()
        throws ArithmFormatException {
        TestLargeIntegerArray.memoryBased();
        byteTree();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTreeF()
        throws ArithmFormatException {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        byteTree();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Concatenation.
     */
    protected void concatenate() {
        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray[] xas = new PGroupElementArray[size];
            for (int i = 0; i < xas.length; i++) {
                xas[i] = pGroup.randomElementArray(size, rs, 20);
            }
            final PGroupElementArray ya = pGroup.toElementArray(xas);

            for (int i = 0; i < xas.length; i++) {
                for (int j = 0; j < xas[i].size(); j++) {
                    assert ya.get(i * size + j).equals(xas[i].get(j));
                }
            }

            for (int i = 0; i < xas.length; i++) {
                xas[i].free();
            }
            ya.free();

            size++;
        }
        final PGroupElementArray x = pGroup.randomElementArray(1, rs, 20);
        final PGroupElementArray y = pGroup2.randomElementArray(1, rs, 20);
        boolean invalid = false;
        try {
            pGroup.toElementArray(x, y);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on concatenation for distinct groups!";
        x.free();
        y.free();
    }

    /**
     * Concatenation.
     */
    public void concatenateM() {
        TestLargeIntegerArray.memoryBased();
        concatenate();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Concatenation.
     */
    public void concatenateF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        concatenate();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Fill array with identical elements.
     */
    protected void fill() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PGroupElement e = pGroup.randomElement(rs, 50);

            final PGroupElement[] es = new PGroupElement[size];
            Arrays.fill(es, e);
            final PGroupElementArray a1 = pGroup.toElementArray(es);

            final PGroupElementArray a2 = pGroup.toElementArray(size, e);

            assert a1.equals(a2) : "Failed to fill array of group elements!";

            a1.free();
            a2.free();

            size++;
        }
    }

    /**
     * Fill array with identical elements.
     */
    public void fillM() {
        TestLargeIntegerArray.memoryBased();
        fill();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Fill array with identical elements.
     */
    public void fillF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        fill();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Arithmetic.
     *
     * @throws ArithmException If a test failed.
     */
    protected void arithmetic()
        throws ArithmException {

        int size = 1;

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElementArray ones =
                pGroup.toElementArray(size, pGroup.getONE());

            final PGroupElementArray a =
                pGroup.randomElementArray(size, rs, 50);
            final PGroupElementArray b =
                pGroup.randomElementArray(size, rs, 50);
            final PGroupElementArray c =
                pGroup.randomElementArray(size, rs, 50);

            // Test multiplication by one.
            PGroupElementArray r = a.mul(ones);
            assert r.equals(a) : "Multiplication by one failed!";
            r.free();

            // Test inversion.
            final PGroupElementArray ainv = a.inv();
            r = ainv.mul(a);
            assert r.equals(ones) : "Inversion failed";
            ainv.free();
            r.free();

            ones.free();

            // Test commutativity of multiplication.
            final PGroupElementArray ab = a.mul(b);
            final PGroupElementArray abc = ab.mul(c);

            final PGroupElementArray cb = c.mul(b);
            final PGroupElementArray cba = cb.mul(a);

            assert abc.equals(cba) : "Multiplication is not commutative!";

            a.free();
            b.free();
            c.free();

            ab.free();
            abc.free();

            cb.free();
            cba.free();

            size++;
        }
        final PGroupElementArray a = pGroup.randomElementArray(1, rs, 50);
        final PGroupElementArray b = pGroup2.randomElementArray(1, rs, 50);

        boolean invalid = false;
        try {
            a.mul(b);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on multiplication!";

        a.free();
        b.free();
    }

    /**
     * Arithmetic.
     *
     * @throws ArithmException If a test failed.
     */
    public void arithmeticM()
        throws ArithmException {
        TestLargeIntegerArray.memoryBased();
        arithmetic();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Arithmetic.
     *
     * @throws ArithmException If a test failed.
     */
    public void arithmeticF()
        throws ArithmException {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        arithmetic();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Permutation of elements.
     */
    protected void permute() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final Permutation permutation = Permutation.random(size, rs, 10);

            final PGroupElementArray input =
                pGroup.randomElementArray(size, rs, 50);
            final PGroupElementArray middle = input.permute(permutation);
            final PGroupElementArray output = middle.permute(permutation.inv());

            assert input.equals(output) : "Failed to permute elements!";

            input.free();
            middle.free();
            output.free();

            size++;
        }
    }

    /**
     * Permutation of elements.
     */
    public void permuteM() {
        TestLargeIntegerArray.memoryBased();
        permute();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Permutation of elements.
     */
    public void permuteF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        permute();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Exercise toString.
     */
    protected void excToString() {

        for (int size = 1; size < 3; size++) {
            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 50);
            x.toString();
            x.free();
        }
    }

    /**
     * Exercise toString.
     */
    public void excToStringM() {
        TestLargeIntegerArray.memoryBased();
        excToString();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Exercise toString.
     */
    public void excToStringF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        excToString();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Returns a primitive array of random elements.
     *
     * @param size Number of elements.
     * @return Array of random integers.
     */
    private PGroupElement[] random(final int size) {
        final PGroupElement[] xs = new PGroupElement[size];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = pGroup.randomElement(rs, 10);
        }
        return xs;
    }

    /**
     * Copy of range of elements.
     */
    protected void copyOfRange() {
        final Timer timer = new Timer(testTime);

        int size = 2;

        while (!timer.timeIsUp()) {

            final PGroupElement[] xs = random(size);
            final PGroupElementArray x = pGroup.toElementArray(xs);

            final PGroupElementArray yc =
                pGroup.toElementArray(Arrays.copyOfRange(xs, 0, size - 1));
            final PGroupElementArray y = x.copyOfRange(0, size - 1);
            assert yc.equals(y) : "Failed to copy range!";

            final PGroupElementArray zc =
                pGroup.toElementArray(Arrays.copyOfRange(xs, 1, size));
            final PGroupElementArray z = x.copyOfRange(1, size);
            assert zc.equals(z) : "Failed to copy range!";

            final int end = Math.max(2, size - 1);
            final PGroupElementArray wc =
                pGroup.toElementArray(Arrays.copyOfRange(xs, 1, end));
            final PGroupElementArray w = x.copyOfRange(1, end);
            assert wc.equals(w) : "Failed to copy range!";

            x.free();
            yc.free();
            y.free();
            zc.free();
            z.free();
            wc.free();
            w.free();

            size++;
        }
    }

    /**
     * Copy of range of elements.
     */
    public void copyOfRangeM() {
        TestLargeIntegerArray.memoryBased();
        copyOfRange();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Copy of range of elements.
     */
    public void copyOfRangeF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        copyOfRange();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Extracting subset.
     */
    protected void extract() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            // Original array.
            final PGroupElement[] xs = random(size);
            final PGroupElementArray x = pGroup.toElementArray(xs);

            // Random subset.
            final boolean[] b = new boolean[size];
            for (int i = 0; i < b.length; i++) {
                b[i] = Math.abs(rs.getBytes(1)[0]) % 2 == 1;
            }

            // Extracted array.
            final PGroupElementArray y = x.extract(b);
            final PGroupElement[] ys = y.elements();

            int j = 0;
            for (int i = 0; i < xs.length; i++) {
                if (b[i]) {
                    assert xs[i].equals(ys[j]) : "Missing element in extract!";
                    j++;
                }
            }
            x.free();
            y.free();

            size++;
        }
    }

    /**
     * Extracting subset.
     */
    public void extractM() {
        TestLargeIntegerArray.memoryBased();
        extract();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Extracting subset.
     */
    public void extractF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        extract();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Component-wise exponentiation.
     */
    protected void exp1() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray b =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElement[] bs = b.elements();

            final PRingElementArray e =
                pGroup.getPRing().randomElementArray(size, rs, 10);
            final PRingElement[] es = e.elements();

            final PGroupElementArray r = b.exp(e);
            final PGroupElement[] rs = r.elements();

            for (int i = 0; i < bs.length; i++) {
                assert bs[i].exp(es[i]).equals(rs[i])
                    : "Component-wise exponentiation with ring element failed!";
            }

            size++;

            b.free();
            e.free();
            r.free();
        }

        final PGroupElementArray b = pGroup.randomElementArray(size, rs, 10);
        final PRingElementArray e =
            pGroup2.getPRing().randomElementArray(size, rs, 10);
        boolean invalid = false;
        try {
            b.exp(e);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on mismatching group and ring!";

        b.free();
        e.free();
    }

    /**
     * Component-wise exponentiation.
     */
    public void exp1M() {
        TestLargeIntegerArray.memoryBased();
        exp1();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Component-wise exponentiation.
     */
    public void exp1F() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        exp1();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Component-wise exponentiation with ring element.
     */
    protected void exp2() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray b =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElement[] bs = b.elements();

            final PRingElement e = pGroup.getPRing().randomElement(rs, 10);

            final PGroupElementArray r = b.exp(e);
            final PGroupElement[] rs = r.elements();

            for (int i = 0; i < bs.length; i++) {
                assert bs[i].exp(e).equals(rs[i])
                    : "Exponentiation with ring element failed!";
            }

            size++;

            b.free();
            r.free();
        }
        final PGroupElementArray b = pGroup.randomElementArray(size, rs, 10);
        final PRingElement e = pGroup2.getPRing().randomElement(rs, 10);

        boolean invalid = false;
        try {
            b.exp(e);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on mismatching group and ring!";

        b.free();
    }

    /**
     * Component-wise exponentiation with ring element.
     */
    public void exp2M() {
        TestLargeIntegerArray.memoryBased();
        exp2();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Component-wise exponentiation with ring element.
     */
    public void exp2F() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        exp2();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Power product.
     */
    protected void expProd() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElement[] xs = x.elements();

            final PRingElementArray e =
                pGroup.getPRing().randomElementArray(size, rs, 10);
            final PRingElement[] es = e.elements();

            PGroupElement res = pGroup.getONE();
            for (int i = 0; i < xs.length; i++) {
                res = res.mul(xs[i].exp(es[i]));
            }

            assert x.expProd(e).equals(res)
                : "Failed to compute power product!";

            size++;

            x.free();
            e.free();
        }
        final PGroupElementArray b = pGroup.randomElementArray(size, rs, 10);
        final PRingElementArray e =
            pGroup2.getPRing().randomElementArray(size, rs, 10);
        boolean invalid = false;
        try {
            b.expProd(e);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on mismatching group and ring!";

        b.free();
        e.free();
    }

    /**
     * Power product.
     */
    public void expProdM() {
        TestLargeIntegerArray.memoryBased();
        expProd();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Power product.
     */
    public void expProdF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        expProd();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Equality.
     */
    protected void equality() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElementArray y =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElementArray z =
                pGroup.toElementArray(x.elements());

            assert x.equals(x) : "Equality by reference failed!";
            assert x.equals(z) : "Equality by elements failed!";

            assert !x.equals(y) : "Inequality failed!";

            x.free();
            y.free();
            z.free();

            size++;
        }

        final PGroupElementArray x = pGroup.randomElementArray(1, rs, 10);
        assert !x.equals(new Object())
            : "Inequality with non-PGroupElementArray instance failed!";

        final PGroupElementArray y = pGroup2.randomElementArray(1, rs, 10);
        assert !x.equals(y)
            : "Inequality with array from different group instance failed!";
        x.free();
        y.free();
    }

    /**
     * Equality.
     */
    public void equalityM() {
        TestLargeIntegerArray.memoryBased();
        equality();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Equality.
     */
    public void equalityF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        equality();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Comparison.
     */
    protected void compareTo() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElement[] xs = x.elements();

            final PGroupElementArray y =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElement[] ys = y.elements();

            int cs = 0;
            for (int i = 0; cs == 0 && i < xs.length; i++) {
                cs = xs[i].compareTo(ys[i]);
            }

            final int c = x.compareTo(y);

            assert c == cs : "Comparison failed!";

            x.free();
            y.free();

            size++;
        }

        final PGroupElementArray x = pGroup.randomElementArray(1, rs, 10);
        final PGroupElementArray y = pGroup2.randomElementArray(1, rs, 10);
        boolean invalid = false;
        try {
            x.compareTo(y);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on distinct groups!";

        final PGroupElementArray z = otherPGroup.randomElementArray(1, rs, 10);
        invalid = false;
        try {
            x.compareTo(z);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to compare with non group element array!";

        x.free();
        y.free();
        z.free();
    }

    /**
     * Comparison.
     */
    public void compareToM() {
        TestLargeIntegerArray.memoryBased();
        compareTo();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Comparison.
     */
    public void compareToF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        compareTo();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Product.
     */
    protected void prod() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 10);
            final PGroupElement[] xs = x.elements();
            PGroupElement res = pGroup.getONE();
            for (int i = 0; i < xs.length; i++) {
                res = res.mul(xs[i]);
            }

            assert x.prod().equals(res) : "Failed to compute product!";

            size++;

            x.free();
        }
    }

    /**
     * Product.
     */
    public void prodM() {
        TestLargeIntegerArray.memoryBased();
        prod();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Product.
     */
    public void prodF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        prod();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Component-wise equality testing.
     */
    protected void equalsAll() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PGroupElement[] xs = random(size);
            final PGroupElement[] ys = random(size);

            for (int i = 0; i < xs.length; i++) {
                if (i % 3 == 0) {
                    xs[i] = ys[i];
                }
            }

            final PGroupElementArray x = pGroup.toElementArray(xs);
            final PGroupElementArray y = pGroup.toElementArray(ys);
            final boolean[] b = x.equalsAll(y);

            for (int i = 0; i < b.length; i++) {
                if (i % 3 == 0) {
                    assert b[i] : "Component-wise equality failed!";
                } else {
                    assert !b[i] : "Component-wise equality failed!";
                }
            }

            x.free();
            y.free();

            size++;
        }
    }

    /**
     * Component-wise equality testing.
     */
    public void equalsAllM() {
        TestLargeIntegerArray.memoryBased();
        equalsAll();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Component-wise equality testing.
     */
    public void equalsAllF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        equalsAll();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Shift and push.
     */
    protected void shiftPush() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElement[] xs = random(size);
            final PGroupElementArray x = pGroup.toElementArray(xs);

            final PGroupElementArray y = x.shiftPush(pGroup.getONE());
            final PGroupElement[] ys = y.elements();
            assert ys[0].equals(pGroup.getONE()) : "Failed to push!";

            for (int i = 1; i < ys.length; i++) {
                assert ys[i].equals(xs[i - 1]) : "Failed to shift!";
            }
            x.free();
            y.free();

            size++;
        }
    }

    /**
     * Shift and push.
     */
    public void shiftPushM() {
        TestLargeIntegerArray.memoryBased();
        shiftPush();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Shift and push.
     */
    public void shiftPushF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        shiftPush();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Arithmetic sugar.
     */
    protected void arithmeticSugar() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 50);
            final PGroupElementArray y =
                pGroup.randomElementArray(size, rs, 50);
            final PRingElementArray z =
                pGroup.getPRing().randomElementArray(size, rs, 50);
            final PRingElement a = pGroup.getPRing().randomElement(rs, 50);

            PGroupElementArray tmp = x.exp(z);
            PGroupElementArray w = tmp.mul(y);
            tmp.free();
            PGroupElementArray ww = x.expMul(z, y);
            assert ww.equals(w) : "Exp-mul failed!";
            w.free();
            ww.free();

            tmp = x.exp(a);
            w = tmp.mul(y);
            tmp.free();
            ww = x.expMul(a, y);
            assert ww.equals(w) : "Exp-mul failed!";
            w.free();
            ww.free();

            x.free();
            y.free();
            z.free();

            size++;
        }
    }

    /**
     * Arithmetic sugar.
     */
    public void arithmeticSugarM() {
        TestLargeIntegerArray.memoryBased();
        arithmeticSugar();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Arithmetic sugar.
     */
    public void arithmeticSugarF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        arithmeticSugar();
        TestLargeIntegerArray.resetBased();
    }
}
