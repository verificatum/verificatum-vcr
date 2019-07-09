
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

package com.verificatum.arithm;

import java.util.Arrays;

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.util.Functions;
import com.verificatum.eio.ByteTreeReader;


/**
 * Implements an immutable polynomial over a {@link PRing} instance.
 *
 * @author Douglas Wikstrom
 */
public class Polynomial implements ByteTreeConvertible, PRingAssociated {

    /**
     * Coefficients of the polynomial.
     */
    protected PRingElement[] coefficients;

    /**
     * Creates an uninitialized polynomial. It is the responsibility
     * of the programmer to properly initialize the created instance
     * if this constructor is used.
     */
    protected Polynomial() {

        // UNCOVERABLE (Meant for subclasses in other packages.)
        super();
    }

    /**
     * Creates a partially initialized polynomial. It is the
     * responsibility of the programmer to properly initialize the
     * created instance if this constructor is used.
     *
     * @param degree Degree of the polynomial.
     */
    protected Polynomial(final int degree) {
        this();
        this.coefficients = new PRingElement[degree + 1];
    }

    /**
     * Creates a zero-degree polynomial.
     *
     * @param constant Value of the constant coefficient.
     */
    public Polynomial(final PRingElement constant) {
        this(0);
        this.coefficients[0] = constant;
    }

    /**
     * Canonicalizes the underlying array of ring elements if needed,
     * i.e., reduces the degree of the polynomial as long as the
     * maximal degree coefficient is zero.
     */
    protected void canonicalize() {
        int d = coefficients.length - 1;
        while (d > 0 && coefficients[d].equals(getPRing().getZERO())) {
            d--;
        }
        if (d < coefficients.length - 1) {
            coefficients = Arrays.copyOfRange(coefficients, 0, d + 1);
        }
    }

    /**
     * Creates a polynomial from a list of coefficients. The
     * coefficients are not copied.
     *
     * @param coefficients Coefficients of the polynomial.
     */
    public Polynomial(final PRingElement... coefficients) {
        if (coefficients.length == 0) {
            throw new ArithmError("No coefficients!");
        }
        this.coefficients =
            Arrays.copyOfRange(coefficients, 0, coefficients.length);
        canonicalize();
    }

    /**
     * Returns the polynomials corresponding to this one over the
     * factors of the underlying ring.
     *
     * @return Polynomials corresponding to this one over the factors
     *         of the underlying ring.
     * @throws ArithmError If this instance is not defined over a
     *  product ring.
     */
    public Polynomial[] getFactors() throws ArithmError {

        if (!(coefficients[0] instanceof PPRingElement)) {
            throw new ArithmError("Element is not a product!");
        }
        final int width = ((PPRing) coefficients[0].getPRing()).getWidth();

        final PRingElement[][] factored =
            new PRingElement[coefficients.length][];
        for (int i = 0; i < factored.length; i++) {
            factored[i] = ((PPRingElement) coefficients[i]).getFactors();
        }
        final Polynomial[] polys = new Polynomial[width];
        for (int l = 0; l < width; l++) {
            final PRingElement[] tmp = new PRingElement[coefficients.length];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = factored[i][l];
            }
            polys[l] = new Polynomial(tmp);
        }
        return polys;
    }

    /**
     * Creates a polynomial from another instance.
     *
     * @param poly A polynomial.
     */
    public Polynomial(final Polynomial poly) {
        this(poly.coefficients);
    }

    /**
     * Initializes a polynomial from the input representation.
     *
     * @param pRing Ring over which this instance is defined.
     * @param maxDegree Maximal degree of polynomial.
     * @param btr Representation of polynomial.
     *
     * @throws ArithmFormatException If the input does not represent a
     *  polynomial over the given ring.
     */
    protected void init(final PRing pRing,
                        final int maxDegree,
                        final ByteTreeReader btr)
        throws ArithmFormatException {
        coefficients = pRing.toElements(maxDegree + 1, btr);
        canonicalize();
    }

    /**
     * Creates a polynomial from the input representation.
     *
     * @param pRing Ring over which this instance is defined.
     * @param maxDegree Maximal degree of polynomial.
     * @param btr Representation of polynomial.
     *
     * @throws ArithmFormatException If the input does not represent a
     *  polynomial over the given ring.
     */
    public Polynomial(final PRing pRing,
                      final int maxDegree,
                      final ByteTreeReader btr)
        throws ArithmFormatException {
        init(pRing, maxDegree, btr);
    }

    /**
     * Creates a representation of this instance.
     *
     * @return Representation.
     */
    @Override
    public ByteTreeBasic toByteTree() {
        return getPRing().toByteTree(coefficients);
    }

    /**
     * Returns the degree of the polynomial.
     *
     * @return Degree of this polynomial.
     */
    public int getDegree() {
        return coefficients.length - 1;
    }

    /**
     * Returns the <code>index</code>th coefficient of the polynomial.
     * This is zero for all indices greater than the degree.
     *
     * @param index Index of coefficient.
     * @return <code>index</code>th coefficient.
     */
    public PRingElement getCoefficient(final int index) {
        if (index < coefficients.length) {
            return coefficients[index];
        } else {
            return getPRing().getZERO();
        }
    }

    /**
     * Evaluates the polynomial at the integer given as input.
     *
     * @param j Input.
     * @return Image of input under polynomial.
     */
    public PRingElement evaluate(final int j) {
        return evaluate(coefficients[0].getPRing().getPField().toElement(j));
    }

    /**
     * Evaluates the polynomial at the point given as input and
     * returns the result.
     *
     * @param el Point at which this polynomial is evaluated.
     * @return Image of input under polynomial.
     */
    public PRingElement evaluate(final PFieldElement el) {
        if (!getPRing().getPField().equals(el.getPRing())) {
            throw new ArithmError("Distinct fields!");
        }
        PRingElement value = coefficients[0];
        PFieldElement elPower = el;

        for (int i = 1; i <= getDegree(); i++) {
            value = value.add(coefficients[i].mul(elPower));
            elPower = elPower.mul(el);
        }
        return value;
    }

    /**
     * Adds the input to this instance and returns the result, i.e.,
     * the sum of this polynomial and the input.
     *
     * @param poly Polynomial added to this instance.
     * @return Sum of this polynomial and the input.
     */
    public Polynomial add(final Polynomial poly) {
        if (!getPRing().equals(poly.getPRing())) {
            throw new ArithmError("Distinct rings!");
        }
        Polynomial p1 = this;
        Polynomial p2 = poly;
        if (p1.getDegree() < p2.getDegree()) {
            final Polynomial temp = p1;
            p1 = p2;
            p2 = temp;
        }
        final PRingElement[] sum = new PRingElement[p1.getDegree() + 1];
        int i = 0;
        for (; i <= p2.getDegree(); i++) {
            sum[i] = p1.coefficients[i].add(p2.coefficients[i]);
        }
        System.arraycopy(p1.coefficients, i, sum, i, p1.getDegree() + 1 - i);

        final Polynomial sumPoly = new Polynomial(sum);
        sumPoly.canonicalize();

        return sumPoly;
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the input represents the same polynomial as this polynomial or
     * not.
     *
     * @param obj Polynomial to compare with.
     * @return <code>true</code> or <code>false</code> depending on if
     *         the input equals this polynomial or not.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Polynomial)) {
            return false;
        }
        final Polynomial p = (Polynomial) obj;
        if (!getPRing().equals(p.getPRing())) {
            return false;
        }
        return Arrays.equals(coefficients, p.coefficients);
    }

    // Documented in PRingAssociated.java

    @Override
    public PRing getPRing() {
        return coefficients[0].getPRing();
    }
}
