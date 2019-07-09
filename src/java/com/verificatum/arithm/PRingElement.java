
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

import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.util.Functions;


/**
 * Implements an immutable ring element of a {@link PRing} instance.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRingElement
    implements PRingAssociated, ByteTreeConvertible {

    /**
     * Ring to which this element belongs.
     */
    protected PRing pRing;

    /**
     * Creates an instance.
     *
     * @param pRing Ring to which the resulting element should belong.
     */
    protected PRingElement(final PRing pRing) {
        this.pRing = pRing;
    }

    /**
     * Returns a human readable representation of this instance. This
     * should only be used for debugging.
     *
     * @return Representation of this instance.
     */
    @Override
    public abstract String toString();

    /**
     * Returns the sum of this instance and the input.
     *
     * @param term Element added to this instance.
     * @return Sum of this instance and the input.
     */
    public abstract PRingElement add(PRingElement term);

    /**
     * Returns the negative of this instance (additive inverse).
     *
     * @return Negative of this instance.
     */
    public abstract PRingElement neg();

    /**
     * Returns the product of the input and this instance.
     *
     * @param factor Element multiplied with this instance.
     * @return Product of this instance and the input.
     */
    public abstract PRingElement mul(PRingElement factor);

    /**
     * Returns the multiplicative inverse of this instance. If this
     * instance is not invertible an exception is thrown.
     *
     * @return Inverse of this instance.
     *
     * @throws ArithmException If this element is not invertible.
     */
    public abstract PRingElement inv() throws ArithmException;

    /**
     * Returns true if and only if the input represents the same ring
     * element as this instance.
     *
     * @param obj Element compared to this instance.
     * @return true if this instance equals the input and false
     * otherwise.
     */
    @Override
    public abstract boolean equals(Object obj);

    // Documented in PRingAssociated.java

    @Override
    public PRing getPRing() {
        return pRing;
    }

    // Implemented in terms of the above.

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    /**
     * Returns a raw <b>fixed-size</b> representation of the instance.
     * It should only be used as input to hash functions etc. In
     * particular it should not be used for storing elements. The
     * method is an injective map from the set of ring elements to the
     * set of <code>byte[]</code> of length
     * {@link PRing#getByteLength()}.
     *
     * @return Raw representation of this instance.
     */
    public byte[] toByteArray() {
        return toByteTree().toByteArray();
    }

    /**
     * Multiplies this instance with the scalar, adds the term, and
     * returns the result.
     *
     * @param scalar Scalar element.
     * @param term Term element to add.
     * @return Product of this instance and scalar plus the term.
     */
    public PRingElement mulAdd(final PRingElement scalar,
                               final PRingElement term) {
        return mul(scalar).add(term);
    }

    /**
     * Returns the difference between this instance and the input.
     *
     * @param term Element subtracted from this instance.
     * @return Difference between this instance and the input.
     */
    public PRingElement sub(final PRingElement term) {
        return add(term.neg());
    }

    /**
     * Returns the product of this instance and the inverse of the
     * input. If the input is not invertible an exception is thrown.
     *
     * @param el Divisor.
     * @return This instance divided by the input.
     *
     * @throws ArithmException If input is not invertible.
     */
    public PRingElement div(final PRingElement el) throws ArithmException {
        return mul(el.inv());
    }
}
