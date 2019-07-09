
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
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PFieldElement;
import com.verificatum.arithm.Polynomial;
import com.verificatum.arithm.PPRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link Polynomial}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPolynomial extends TestClass {

    /**
     * Concrete field used for testing.
     */
    private final PField pField;

    /**
     * Secondary field used for failure testing.
     */
    private final PField pField2;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPolynomial(final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.pField = new PField(SafePrimeTable.safePrime(512));
        this.pField2 = new PField(SafePrimeTable.safePrime(640));
    }

    /**
     * Constructors.
     */
    public void constructors() {

        final Timer timer = new Timer(testTime);

        int size = 2;

        while (!timer.timeIsUp()) {

            // Random polynomial.
            final PRingElement[] a = pField.randomElements(size, rs, 50);
            final Polynomial p = new Polynomial(a);

            // Polynomial with leading zero coefficients.
            a[a.length - 1] = pField.getZERO();
            final Polynomial q = new Polynomial(a);
            final PRingElement[] b = Arrays.copyOfRange(a, 0, a.length - 1);
            final Polynomial qq = new Polynomial(b);
            assert qq.equals(q) : "Failed to canonicalize!";

            // Check coefficients.
            for (int i = 0; i < a.length + 2; i++) {
                if (i < a.length) {
                    assert q.getCoefficient(i).equals(a[i])
                        : "Wrong coefficient!";
                } else {
                    assert q.getCoefficient(i).equals(pField.getZERO())
                        : "Failed to extend coefficients with zeros!";
                }
            }

            // Copy polynomial.
            final Polynomial r = new Polynomial(p);
            assert r.equals(p) : "Failed to copy polynomial!";

            size++;
        }

        boolean invalid = false;
        try {
            new Polynomial(new PFieldElement[0]);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on lack of coefficients!";
    }

    /**
     * Equals.
     */
    public void equality() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PRingElement[] a = pField.randomElements(size, rs, 50);
            final Polynomial p = new Polynomial(a);
            final Polynomial pp = new Polynomial(a);

            assert p.equals(p) : "Equality of references failed!";
            assert pp.equals(p) : "Equality of content failed!";

            final PRingElement[] b = pField.randomElements(size, rs, 50);
            final Polynomial q = new Polynomial(b);

            assert !p.equals(q) : "Inequality failed!";

            assert !p.equals(new Object())
                : "Inequality with non polynomial failed!";

            size++;
        }
        final PRingElement[] a = pField.randomElements(size, rs, 50);
        final Polynomial p = new Polynomial(a);

        final PRingElement[] b = pField2.randomElements(size, rs, 50);
        final Polynomial q = new Polynomial(b);

        assert !p.equals(q)
            : "Failed to fail on equality of polynomials over distinct fields!";
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCode() {
        final PRingElement[] a = pField.randomElements(10, rs, 50);
        final Polynomial p = new Polynomial(a);
        p.hashCode();
    }

    /**
     * Degree.
     */
    public void degree() {

        final Timer timer = new Timer(testTime);

        final int size = 1;

        while (!timer.timeIsUp()) {

            final PRingElement[] a = new PRingElement[size];

            Arrays.fill(a, pField.getZERO());

            assert new Polynomial(a).getDegree() == 0
                : "Wrong degree of zero polynomial from non-one length "
                + "coefficients!";

            for (int i = 0; i < size; i++) {

                a[i] = pField.getONE();

                assert new Polynomial(a).getDegree() == i
                    : "Wrong degree of polynomial created from coefficients!";
            }
        }
    }

    /**
     * Addition.
     */
    public void add() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PRingElement[] a = pField.randomElements(size, rs, 50);
            final Polynomial pa = new Polynomial(a);

            final Polynomial zero = new Polynomial(pField.getZERO());
            assert pa.add(zero).equals(pa)
                : "Addition with zero polynomial failed!";

            final PRingElement[] b = pField.randomElements(size, rs, 50);
            final Polynomial pb = new Polynomial(b);

            final PRingElement[] c = pField.randomElements(size + 1, rs, 50);
            final Polynomial pc = new Polynomial(c);

            assert pa.add(pb).add(pc).equals(pc.add(pb).add(pa))
                : "Addition does not transpose!";

            size++;
        }

        final PRingElement[] a = pField.randomElements(size, rs, 50);
        final Polynomial pa = new Polynomial(a);

        final PRingElement[] b = pField2.randomElements(size, rs, 50);
        final Polynomial pb = new Polynomial(b);

        boolean invalid = false;
        try {
            pa.add(pb);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on adding polynomials over distinct rings!";
    }

    /**
     * Evaluate.
     */
    public void evaluate() {

        final Timer timer = new Timer(testTime);

        int size = 7;

        while (!timer.timeIsUp()) {

            final PRingElement[] pc = pField.randomElements(size, rs, 50);
            final Polynomial p = new Polynomial(pc);

            final PFieldElement x = pField.randomElement(rs, 20);

            final PRingElement[] qc = new PRingElement[pc.length];
            PFieldElement t = x;
            qc[0] = pc[0];
            for (int i = 1; i < qc.length; i++) {
                qc[i] = pc[i].mul(t);
                t = t.mul(x);
            }
            final Polynomial q = new Polynomial(qc);

            final PFieldElement y = pField.randomElement(rs, 20);
            final PFieldElement z = (PFieldElement) x.mul(y);

            assert q.evaluate(y).equals(p.evaluate(z)) : "Failed to evaluate!";

            size++;
        }

        final PRingElement[] coefficients = pField.randomElements(3, rs, 50);
        final Polynomial p = new Polynomial(coefficients);
        final LargeInteger liVal = new LargeInteger(20, rs);
        final int iVal = liVal.intValue();
        final PFieldElement fVal = pField.toElement(liVal);

        assert p.evaluate(iVal).equals(p.evaluate(fVal))
            : "Failed to evaluate on int!";

        boolean invalid = false;
        try {
            p.evaluate(pField2.getONE());
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid
            : "Failed to fail on evaluation on element from wrong ring!";
    }

    /**
     * Factor.
     */
    public void factor() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PPRing pPRing = new PPRing(pField, size);

            final PRingElement[] coefficients =
                pPRing.randomElements(size, rs, 50);

            final Polynomial pp = new Polynomial(coefficients);

            final Polynomial[] p = pp.getFactors();

            final PFieldElement value = pField.randomElement(rs, 50);

            final PRingElement pout = pp.evaluate(value);

            final PRingElement[] out = new PRingElement[size];

            for (int i = 0; i < size; i++) {
                out[i] = p[i].evaluate(value);
            }

            assert pout.equals(pPRing.product(out))
                : "Evaluation of factor polynomials is inconsistent with "
                + "evaluation of polynomial!";

            size++;
        }

        boolean invalid = false;
        try {
            final PRingElement[] coefficients =
                pField.randomElements(3, rs, 50);
            final Polynomial p = new Polynomial(coefficients);
            p.getFactors();
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on polynomial over prime order field!";
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTree()
        throws ArithmFormatException {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final PRingElement[] array = pField.randomElements(size, rs, 50);

            final Polynomial p = new Polynomial(array);
            final ByteTreeReader btr = p.toByteTree().getByteTreeReader();

            final Polynomial q = new Polynomial(pField, p.getDegree(), btr);

            assert p.equals(q)
                : "Polynomial recovered from byte tree does not match "
                + "original!";

            size++;
        }
    }
}
