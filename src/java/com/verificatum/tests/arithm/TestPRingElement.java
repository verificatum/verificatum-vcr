
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

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link PRingElement}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestPRingElement extends TestClass {

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
    protected TestPRingElement(final PRing pRing,
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
        final PRingElement x = pRing.randomElement(rs, 50);
        x.toString();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCode() {
        final PRingElement a = pRing.randomElement(rs, 10);
        a.hashCode();
    }

    /**
     * Excercise toByteArray.
     */
    public void excToByteArray() {
        final PRingElement a = pRing.randomElement(rs, 10);
        a.toByteArray();
    }

    /**
     * Byte array to element.
     */
    public void toElement() {

        final Timer timer = new Timer(testTime);

        final byte[] data = rs.getBytes(2 * pRing.getEncodeLength());
        final int totalLength = pRing.getEncodeLength();

        for (int len = 0; len < totalLength && !timer.timeIsUp(); len++) {
            for (int offset = 0; offset < 3; offset++) {
                final PRingElement x =
                    pRing.toElement(data, offset, len);
                final byte[] subdata =
                    Arrays.copyOfRange(data, offset, offset + len);
                final PRingElement y = pRing.toElement(subdata, 0, len);

                assert x.equals(y) : "Failed to map data to elements!";

                final byte[] firstData = Arrays.copyOfRange(subdata, 0, len);
                final PRingElement z = pRing.toElement(firstData);

                assert x.equals(z) : "Failed to map data to elements!";
            }
            len++;
        }
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

            final PRingElement e1 = pRing.randomElement(rs, 50);
            final ByteTreeBasic bt = e1.toByteTree();
            final ByteTreeReader btr = bt.getByteTreeReader();
            final PRingElement e2 = pRing.toElement(btr);

            assert e1.equals(e2)
                : "Failed to convert field element and byte tree!";
        }

        final ByteTree bt = new ByteTree();
        final ByteTreeReader btr = bt.getByteTreeReader();
        boolean invalid = false;
        try {
            pRing.toElement(btr);
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }

    /**
     * Equals.
     */
    public void equality() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElement x = pRing.randomElement(rs, 30);
            final PRingElement y = x.mul(pRing.getONE());
            final PRingElement z = pRing.randomElement(rs, 30);

            assert x.equals(y) : "Failed to check equality of elements!";
            assert !x.equals(z) : "Failed to check inequality of elements!";
        }
        assert !pRing.getONE().equals(new Object())
            : "Failed to fail on equality with non-field element!";

        final PRingElement x = pRing.randomElement(rs, 30);
        assert x.equals(x) : "Failed to compare references!";

        final PRingElement y = pRing2.randomElement(rs, 30);
        assert !x.equals(y)
            : "Failed to fail on equality of elements from distinct rings!";
    }

    /**
     * Arithmetic.
     *
     * @throws ArithmException If a test failed.
     */
    public void arithmetic()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElement a = pRing.randomElement(rs, 50);

            assert a.add(pRing.getZERO()).equals(a)
                : "Addition with zero failed!";

            assert a.mul(pRing.getONE()).equals(a)
                : "Multiplication with one failed!";

            final PRingElement b = pRing.randomElement(rs, 50);
            final PRingElement c = pRing.randomElement(rs, 50);

            assert a.add(b).add(c).equals(c.add(b).add(a))
                : "Addition is not commutative!";

            assert a.neg().add(a).equals(pRing.getZERO())
                : "Value added with its negation does not give zero!";

            assert a.inv().mul(a).equals(pRing.getONE())
                : "Value multiplied by inverse does not give one!";

            assert a.mul(b.add(c)).equals(a.mul(b).add(a.mul(c)))
                : "Multiplication is not distributive over addition!";
        }

        final PRingElement a = pRing.randomElement(rs, 50);
        final PRingElement b = pRing2.randomElement(rs, 50);

        // Fail on addition of elements from distinct rings.
        boolean invalid = false;
        try {
            a.add(b);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on addition of elements from different rings!";

        // Fail on multiplication of elements from distinct rings.
        invalid = false;
        try {
            a.mul(b);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on addition of elements from different rings!";

        // Fail on inversion of zero.
        try {
            pRing.getZERO().inv();
        } catch (final ArithmException ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on inversion of zero!";
    }

    /**
     * Arithmetic sugar.
     */
    public void arithmeticSugar() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElement a = pRing.randomElement(rs, 50);
            final PRingElement b = pRing.randomElement(rs, 50);
            final PRingElement c = pRing.randomElement(rs, 50);

            assert a.sub(b).equals(a.add(b.neg())) : "Subtraction failed";

            if (!b.equals(pRing.getZERO())) {
                try {
                    assert a.div(b).equals(a.mul(b.inv())) : "Division failed";
                } catch (final ArithmException ae) { // NOPMD
                }
            }

            assert a.mulAdd(b, c).equals(a.mul(b).add(c)) : "Mul-add failed!";
        }
    }
}
