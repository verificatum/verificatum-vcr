
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

import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.util.Functions;
import com.verificatum.util.ArrayWorker;


/**
 * Abstract class representing an immutable group element in a group
 * where each element has prime order. The group is represented by (a
 * subclass of) the class {@link PGroup}. Keep in mind that {@link
 * PPGroup} is a subclass of this class, so some functions are more
 * general than would be expected from a group of prime order.
 *
 * @author Douglas Wikstrom
 */
public abstract class PGroupElement
    implements Comparable<PGroupElement>, ByteTreeConvertible,
               PGroupAssociated {

    /**
     * Group to which this element belongs.
     */
    protected PGroup pGroup;

    /**
     * Initializes this instance.
     *
     * @param pGroup Group to which this element belongs.
     */
    protected PGroupElement(final PGroup pGroup) {
        this.pGroup = pGroup;
    }

    /**
     * Returns a {@link String} representation of this element. This
     * is mainly useful for debugging. It should not be used to store
     * elements.
     *
     * @return Representation of this element.
     */
    @Override
    public abstract String toString();

    /**
     * Recovers a <code>byte[]</code> from its encoding as an element
     * in the group, i.e., the output of
     * {@link PGroup#encode(byte[],int,int)}. At most
     * {@link PGroup#getEncodeLength()} bytes are written. Every group
     * element must decode to some <code>byte[]</code>, so this method
     * never fails.
     *
     * @param array Where the bytes are stored.
     * @param startIndex Start index where to put the decoded bytes.
     * @return Number of bytes written to <code>array</code>.
     */
    public abstract int decode(byte[] array, int startIndex);

    /**
     * Returns the product of the input and this instance.
     *
     * @param el Element with which this instance is multiplied.
     * @return Product of this element and the input.
     */
    public abstract PGroupElement mul(PGroupElement el);

    /**
     * Returns the inverse of this instance.
     *
     * @return Inverse of this element.
     */
    public abstract PGroupElement inv();

    /**
     * Returns this element to the power of the input.
     *
     * @param exponent Power used to raise this element.
     * @return This element raised to the input.
     */
    public abstract PGroupElement exp(PRingElement exponent);

    /**
     * Returns the array of element-wise powers of this element.
     *
     * @param exponents Powers used to raise this element.
     * @return This element raised to the input.
     */
    public abstract PGroupElementArray exp(PRingElementArray exponents);

    /**
     * Orders the elements in the group "lexicographically". The
     * ordering is obviously not compatible with the binary group
     * operator in any interesting way, but it is useful to have some
     * ordering to be able to sort elements.
     *
     * @param el Instance to which this element is compared.
     * @return -1, 0, or 1 depending on if this element comes before,
     * is equal to, or comes after the input.
     */
    @Override
    public abstract int compareTo(PGroupElement el);

    /**
     * Returns true if and only if the input represents the same group
     * element as this instance.
     *
     * @param obj Element compared to this instance.
     * @return true if this element equals the input and false
     * otherwise.
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Returns this instance to the powers of the elements in
     * <code>exponents</code>.
     *
     * @param exponents Powers to be taken.
     * @return Basis to the powers of the given exponents.
     */
    public PGroupElement[] naiveExp(final PRingElement[] exponents) {
        final PGroupElement[] res = new PGroupElement[exponents.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > pGroup.expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = exp(exponents[i]);
                    }
                }
            };
        worker.work();
        return res;
    }

    /**
     * Returns this instance to the powers of the elements in
     * <code>exponents</code>.
     *
     * @param exponents Powers to be taken.
     * @return Basis to the powers of the given exponents.
     */
    public abstract PGroupElement[] exp(final PRingElement[] exponents);

    // ############ Implemented in terms of the above. ###########

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    /**
     * Returns a raw <b>fixed-size</b> representation of the instance.
     * It should only be used as input to hash functions etc. In
     * particular it should not be used for storing elements. The
     * method is an injective map from the set of group elements to
     * the set of <code>byte[]</code> of length
     * {@link PGroup#getByteLength()}.
     *
     * @return Raw fixed size representation of this element as a
     * <code>byte[]</code>.
     */
    public byte[] toByteArray() {
        return toByteTree().toByteArray();
    }

    /**
     * Returns this instance divided by the input.
     *
     * @param el Divisor.
     * @return This instance divided by the input.
     */
    public PGroupElement div(final PGroupElement el) {
        final PGroupElement inverted = el.inv();
        return mul(inverted);
    }

    /**
     * Returns this element to the power of the input.
     *
     * @param exponent Exponent used to raise this element.
     * @return This element raised to the input.
     */
    public PGroupElement exp(final int exponent) {
        return exp(new LargeInteger(exponent));
    }

    /**
     * Returns this element to the power of the input.
     *
     * @param exponent Exponent used to raise this element.
     * @return This element raised to the input.
     */
    public PGroupElement exp(final LargeInteger exponent) {
        return exp(pGroup.pRing.getPField().toElement(exponent));
    }

    /**
     * Raises this element to the given scalar and multiplies the
     * result by the factor.
     *
     * @param scalar Scalar exponent.
     * @param factor Multiplier.
     * @return This instance raised to the given scalar power and
     * multiplied by factor.
     */
    public PGroupElement expMul(final PRingElement scalar,
                                final PGroupElement factor) {
        final PGroupElement expd = exp(scalar);
        return expd.mul(factor);
    }

    /**
     * Recovers a <code>byte[]</code> from its encoding as an element
     * in the group, i.e., the output of
     * {@link PGroup#encode(byte[],int,int)}.
     *
     * @return Decoded bytes.
     */
    public byte[] decode() {
        final byte[] tmp = new byte[pGroup.getByteLength()];
        final int len = decode(tmp, 0);
        return Arrays.copyOf(tmp, len);
    }

    // Documented in PGroupAssociated.java

    @Override
    public PGroup getPGroup() {
        return pGroup;
    }
}
