
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

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.Permutation;
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PRingElementArray;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Pair;
import com.verificatum.util.Timer;


/**
 * Tests {@link PRingElementArray}.
 *
 * @author Douglas Wikstrom
 */
public class TestPRingElementArray extends TestClass {

    /**
     * Concrete ring used for testing.
     */
    protected PRing pRing;

    /**
     * Separate ring of the same class used for failure testing.
     */
    protected PRing pRing2;

    /**
     * Ring of different class used for failure testing.
     */
    protected PRing otherPRing;

    /**
     * Constructs test.
     *
     * @param pRing Ring used for testing.
     * @param pRing2 Secondary ring used for failure testing.
     * @param otherPRing Ring from other class used for failure
     * testing.
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    protected TestPRingElementArray(final PRing pRing,
                                    final PRing pRing2,
                                    final PRing otherPRing,
                                    final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.pRing = pRing;
        this.pRing2 = pRing2;
        this.otherPRing = otherPRing;
    }

    /**
     * Exercise toString.
     */
    public void excToString() {
        final PRingElementArray x = pRing.randomElementArray(10, rs, 50);
        x.toString();
        x.free();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCode() {
        final PRingElementArray x = pRing.randomElementArray(3, rs, 10);
        x.hashCode();
    }

    /**
     * Excercise free.
     */
    public void excFree() {
        final int size = 10;
        final PRingElementArray x = pRing.randomElementArray(size, rs, 50);
        x.free();
        final PRingElementArray y = pRing.randomElementArray(size, rs, 50);
        PRingElementArray.free(y);
        PRingElementArray.free(null);
    }

    /**
     * Exercise size.
     */
    public void excSize() {
        final int size = 10;
        final PRingElementArray x = pRing.randomElementArray(size, rs, 50);
        assert size == x.size() : "Failed to get size!";
        x.free();
    }

    /**
     * To elements.
     */
    public void elements() {

        final Timer timer = new Timer(testTime);

        int size = 1;
        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 50);
            final PRingElement[] pa = x.elements();
            final PRingElementArray y = pRing.toElementArray(pa);

            assert x.equals(y)
                : "Mapping integer arrays to and from element arrays failed!";

            x.free();
            y.free();

            size++;
        }
    }

    /**
     * Fill array.
     */
    public void fill() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PRingElement e = pRing.randomElement(rs, 50);

            final PRingElement[] es = new PRingElement[size];
            Arrays.fill(es, e);
            final PRingElementArray x = pRing.toElementArray(es);

            final PRingElementArray y = pRing.toElementArray(size, e);

            assert x.equals(y) : "Failed to fill array of field elements!";

            x.free();
            y.free();

            size++;
        }
    }

    /**
     * Equals.
     */
    public void equality() {

        final Timer timer = new Timer(testTime);

        final int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 30);
            final PRingElementArray y = pRing.toElementArray(x.elements());
            final PRingElementArray z = pRing.randomElementArray(size, rs, 30);

            assert x.equals(y) : "Failed to check equality of element arrays!";
            assert !x.equals(z)
                : "Failed to check inequality of element arrays!";

            x.free();
            y.free();
            z.free();
        }

        final PRingElementArray x = pRing.randomElementArray(1, rs, 30);
        assert x.equals(x) : "Failed to compare references!";
        assert !x.equals(new Object())
            : "Failed to fail on equality with non-field element array!";

        final PRingElementArray y = pRing2.randomElementArray(1, rs, 30);
        assert !x.equals(y)
            : "Failed to fail on equality of element arrays "
            + "from distinct rings!";

        x.free();
        y.free();
    }

    /**
     * Arithmetic.
     *
     * @throws ArithmException If a test failed.
     */
    public void arithmetic()
        throws ArithmException {

        int size = 1;

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElementArray a =
                pRing.randomElementArray(size, rs, 50);
            final PRingElementArray b =
                pRing.randomElementArray(size, rs, 50);
            final PRingElementArray c =
                pRing.randomElementArray(size, rs, 50);

            // Test negation.
            final PRingElementArray aneg = a.neg();
            final PRingElementArray zeros =
                pRing.toElementArray(size, pRing.getZERO());
            PRingElementArray r = aneg.add(a);
            assert r.equals(zeros) : "Negation failed!";
            aneg.free();
            zeros.free();
            r.free();

            // Test inversion
            final PRingElementArray ainv = a.inv();
            final PRingElementArray ones =
                pRing.toElementArray(size, pRing.getONE());
            r = ainv.mul(a);
            assert r.equals(ones)
                : "Inversion failed";
            ainv.free();
            ones.free();
            r.free();

            // Test addition
            PRingElementArray r1 = a.add(b);
            PRingElementArray r2 = r1.add(c);
            PRingElementArray s1 = c.add(b);
            PRingElementArray s2 = s1.add(a);
            assert r2.equals(s2) : "Addition of arrays is not commutative!";
            r1.free();
            r2.free();
            s1.free();
            s2.free();

            // Test addition
            r1 = a.add(b);
            r2 = r1.add(c);
            s1 = c.add(b);
            s2 = s1.add(a);
            assert r2.equals(s2)
                : "Addition of arrays is not commutative!";
            r1.free();
            r2.free();
            s1.free();
            s2.free();

            // Test multiplication.
            final PRingElementArray d = b.add(c);
            r1 = a.mul(d);
            d.free();

            final PRingElementArray e = a.mul(b);
            final PRingElementArray f = a.mul(c);
            r2 = e.add(f);
            e.free();
            f.free();

            assert r1.equals(r2)
                : "Multiplication is not distributive over addition!";
            r1.free();
            r2.free();

            // Test multiplication.
            final PRingElementArray g = a.mul(pRing.getONE());
            g.free();

            a.free();
            b.free();
            c.free();

            size++;
        }

        final PRingElementArray aa = pRing.randomElementArray(1, rs, 50);
        final PRingElementArray ba = pRing2.randomElementArray(1, rs, 50);
        PRingElementArray r = null;

        // Fail on addition of elements from distinct fields.
        boolean invalid = false;
        try {
            r = aa.add(ba);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on addition of elements from different fields!";
        if (r != null) {
            r.free();
        }

        // Fail on multiplication of elements from distinct fields.
        invalid = false;
        try {
            r = aa.mul(ba);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on multiplications of elements from different "
            + "fields!";
        if (r != null) {
            r.free();
        }

        // Fail on multiplication of elements from distinct fields.
        invalid = false;
        try {
            r = aa.mul(pRing2.getONE());
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on multiplications of elements from different "
            + "fields!";
        if (r != null) {
            r.free();
        }

        aa.free();
        ba.free();

        // Fail on inversion of zero.
        final PRingElementArray ca = pRing.toElementArray(1, pRing.getZERO());
        try {
            r = ca.inv();
        } catch (final ArithmException ae) {
            invalid = true;
        }
        if (r != null) {
            r.free();
        }
        assert invalid : "Failed to fail on inversion of zero!";
        ca.free();
    }

    /**
     * Arithmetic sugar.
     */
    public void arithmeticSugar() {

        final Timer timer = new Timer(testTime);

        final int size = 10;

        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 50);
            final PRingElementArray y = pRing.randomElementArray(size, rs, 50);
            final PRingElementArray z = pRing.randomElementArray(size, rs, 50);
            final PRingElement a = pRing.randomElement(rs, 50);

            PRingElementArray t = x.mul(y);
            PRingElementArray w = t.add(z);
            PRingElementArray ww = x.mulAdd(y, z);
            assert ww.equals(w) : "Mul-add failed!";
            t.free();
            w.free();
            ww.free();

            t = x.mul(a);
            w = t.add(z);
            ww = x.mulAdd(a, z);
            assert ww.equals(w) : "Mul-add failed!";
            t.free();
            w.free();
            ww.free();

            x.free();
            y.free();
            z.free();
        }
    }

    /**
     * Verifies inner product. The arithmetic is verified in {@link
     * TestLargeIntegerArray}.
     */
    public void innerProduct() {

        int size = 1;

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElementArray a =
                pRing.randomElementArray(size, rs, 50);
            final PRingElementArray b =
                pRing.randomElementArray(size, rs, 50);

            a.innerProduct(b);

            a.free();
            b.free();
            size++;
        }

        // Fail on wrong field.
        final PRingElementArray a = pRing.randomElementArray(size, rs, 50);
        final PRingElementArray b = pRing2.randomElementArray(size, rs, 50);
        boolean invalid = false;
        try {
            a.innerProduct(b);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on inversion of zero!";
        a.free();
        b.free();
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTree1()
        throws ArithmFormatException {

        int size = 1;

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            // Check that conversion of array of field elements with
            // right size works.
            final PRingElementArray a1 =
                pRing.randomElementArray(size, rs, 50);
            final ByteTreeBasic bta = a1.toByteTree();
            final ByteTreeReader btr = bta.getByteTreeReader();
            final PRingElementArray a2 =
                pRing.unsafeToElementArray(size, btr);
            btr.close();

            assert a1.equals(a2)
                : "Failed to convert array of field elements and byte tree "
                + "sloppily!";


            // Check that conversion of array of field elements with
            // too small size fails.
            final ByteTreeReader btr2 = bta.getByteTreeReader();
            try {
                pRing.toElementArray(size + 1, btr2);

                assert false
                    : "Failed to fail convert array of field elements and "
                    + "byte tree!";

            } catch (final ArithmFormatException afe) {

                // This means the test passed.
                btr2.close();
            }

            // Check that conversion of array of field elements with
            // too big size fails.
            final ByteTreeReader btr3 = bta.getByteTreeReader();
            try {
                pRing.toElementArray(size + 1, btr3);

                assert false
                    : "Failed to fail convert array of field elements and "
                    + "byte tree!";

            } catch (final ArithmFormatException afe) {

                // This means the test passed.
                btr3.close();
            }

            a1.free();
            a2.free();

            size++;
        }
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTree2()
        throws ArithmFormatException {

        int size = 1;

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElementArray a1 =
                pRing.randomElementArray(size, rs, 50);
            final ByteTreeBasic bta = a1.toByteTree();
            final ByteTreeReader btr = bta.getByteTreeReader();
            final PRingElementArray a2 = pRing.unsafeToElementArray(0, btr);
            btr.close();

            assert a1.equals(a2)
                : "Failed to convert array of field elements and byte tree "
                + "sloppily!";

            a1.free();
            a2.free();
            size++;
        }
    }

    /**
     * Verifies copying a range of elements from an array stored on
     * file.
     */
    public void copyOfRange() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final int startIndex = size / 4;
            final int endIndex = Math.max(startIndex + 1, 3 * (size / 4));

            final PRingElementArray x =
                pRing.randomElementArray(size, rs, 10);

            final PRingElement[] ys =
                Arrays.copyOfRange(x.elements(), startIndex, endIndex);
            final PRingElementArray y = pRing.toElementArray(ys);

            final PRingElementArray yy = x.copyOfRange(startIndex, endIndex);

            assert yy.equals(y) : "Failed to copy range of integers!";

            x.free();
            y.free();
            yy.free();

            size++;
        }
    }

    /**
     * Permute elements.
     */
    public void permute() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PRingElementArray x =
                pRing.randomElementArray(size, rs, 10);

            final Permutation permutation = Permutation.random(size, rs, 100);
            final PRingElementArray y = x.permute(permutation);

            final PRingElementArray xx = y.permute(permutation.inv());

            assert xx.equals(x) : "Failed to permute integers!";

            x.free();
            xx.free();

            size++;
        }
    }

    /**
     * Get element.
     */
    public void get() {

        final int size = 10;

        final PRingElementArray x = pRing.randomElementArray(size, rs, 10);
        final PRingElement[] xs = x.elements();

        final PRingElement[] ys = new PRingElement[xs.length];
        for (int i = 0; i < xs.length; i++) {
            ys[i] = x.get(i);
        }
        final PRingElementArray y = pRing.toElementArray(ys);

        assert y.equals(x) : "Failing to get elements!";

        x.free();
        y.free();
    }

    /**
     * Shift and push.
     */
    public void shiftPush() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 10);
            final PRingElement[] xs = x.elements();

            final PRingElementArray y = x.shiftPush(pRing.getONE());
            final PRingElement[] ys = y.elements();

            assert ys[0].equals(pRing.getONE()) : "Failed to push!";

            for (int i = 1; i < ys.length; i++) {
                assert ys[i].equals(xs[i - 1]) : "Failed to shift!";
            }
            x.free();
            y.free();

            size++;
        }
    }

    /**
     * Summation of arrays.
     */
    public void sum() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 10);
            final PRingElement[] xs = x.elements();

            final PRingElement a = x.sum();
            PRingElement b = pRing.getZERO();
            for (int i = 0; i < xs.length; i++) {
                b = b.add(xs[i]);
            }

            assert a.equals(b) : "Sum failed!";

            x.free();

            size++;
        }
    }

    /**
     * Product.
     */
    public void prod() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 10);
            final PRingElement[] xs = x.elements();

            final PRingElement a = x.prod();
            PRingElement b = pRing.getONE();
            for (int i = 0; i < xs.length; i++) {
                b = b.mul(xs[i]);
            }

            assert a.equals(b) : "Product failed!";

            x.free();

            size++;
        }
    }

    /**
     * Products.
     */
    public void prods() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PRingElementArray x = pRing.randomElementArray(size, rs, 10);
            final PRingElement[] xs = x.elements();

            final PRingElementArray p = x.prods();
            final PRingElement[] ps = p.elements();

            PRingElement res = pRing.getONE();
            for (int i = 0; i < xs.length; i++) {
                res = res.mul(xs[i]);
                assert res.equals(ps[i]) : "Products failed!";
            }

            x.free();
            p.free();

            size++;
        }
    }

    /**
     * Exercises modular recursive linear expression appearing in some
     * zero knowledge proofs.
     */
    public void excRecLin() {

        final int size = 10;

        final PRingElementArray x = pRing.randomElementArray(size, rs, 10);

        final PRingElementArray y = pRing.randomElementArray(size, rs, 10);
        final Pair<PRingElementArray, PRingElement> pair = x.recLin(y);
        y.free();
        pair.first.free();
        x.free();
    }
}
