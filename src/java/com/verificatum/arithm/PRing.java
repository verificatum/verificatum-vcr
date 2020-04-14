
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

import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.util.Functions;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;


/**
 * Abstract base class of immutable direct powers of a prime order
 * field. The elements of the ring are instances of the abstract class
 * {@link PRingElement} and arrays of such elements are instances of
 * the abstract class {@link PRingElementArray}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRing implements PRingAssociated, ByteTreeConvertible {

    /**
     * Returns the underlying field.
     *
     * @return Underlying field.
     */
    public abstract PField getPField();

    /**
     * Returns the zero element of this ring.
     *
     * @return Zero element of this ring.
     */
    public abstract PRingElement getZERO();

    /**
     * Returns the unit element of this ring.
     *
     * @return Unit element of this ring.
     */
    public abstract PRingElement getONE();

    /**
     * Returns the fixed number of bytes used to map an element
     * injectively to a fixed-size <code>byte[]</code>.
     *
     * @return Number of bytes in outputs from
     * {@link PRingElement#toByteArray()}.
     */
    public abstract int getByteLength();

    /**
     * Outputs a human readable description of the ring. This should
     * only be used for debugging.
     *
     * @return Representation of this ring.
     */
    @Override
    public abstract String toString();

    /**
     * Number of bytes that can be directly encoded into an element of
     * the ring.
     *
     * @return Number of bytes that can be encoded into an element of
     * the ring.
     */
    public abstract int getEncodeLength();

    /**
     * Recovers a ring element from the input representation.
     *
     * @param btr Representation of a ring element.
     * @return Ring element corresponding to the input.
     *
     * @throws ArithmFormatException If the input does not represent a
     * ring element.
     */
    public abstract PRingElement toElement(ByteTreeReader btr)
        throws ArithmFormatException;

    /**
     * Creates a ring element from a raw array of bytes. This should
     * be used to create an instance from outputs from hash functions
     * etc. The restriction of this map to any subring is injective.
     * This method should never be used to recover stored ring
     * elements. If more than {@link #getEncodeLength()} bytes are
     * input, then the output is undefined.
     *
     * @param bytes An array containing arbitrary bytes.
     * @param offset Index of first byte to use.
     * @param length Maximal number of bytes to use.
     * @return Ring element derived from input.
     */
    public abstract PRingElement toElement(byte[] bytes,
                                           int offset,
                                           int length);

    /**
     * Returns a random ring element.
     *
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Ring element chosen at random.
     */
    public abstract PRingElement randomElement(RandomSource rs, int statDist);

    /**
     * Returns an array of random ring elements.
     *
     * @param size Size of resulting array.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random ring elements.
     */
    public abstract PRingElementArray randomElementArray(int size,
                                                         RandomSource rs,
                                                         int statDist);

    /**
     * Creates an instance containing the given elements.
     *
     * @param elements Elements to be contained in the array.
     * @return Array containing the input group elements.
     */
    public abstract PRingElementArray toElementArray(PRingElement[] elements);

    /**
     * Creates an instance of the given size filled with copies of the
     * input element.
     *
     * @param size Number of elements in resulting array.
     * @param element Element to use.
     * @return Array containing the given number of copies of the
     * element.
     */
    public abstract PRingElementArray toElementArray(int size,
                                                     PRingElement element);

    /**
     * Recovers an array of ring elements from its representation and
     * throws an exception if the input is incorrect.
     *
     * @param size Expected number of elements in array. If the
     * expected size (number of elements) is set to zero, then the
     * input can have any size.
     * @param btr Representation of instance.
     * @return Array of ring elements represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * an array of ring elements.
     */
    public abstract PRingElementArray toElementArray(int size,
                                                     ByteTreeReader btr)
        throws ArithmFormatException;

    /**
     * Returns true if and only if the input represents the same ring
     * as this instance.
     *
     * @param obj Object compared to this instance.
     * @return true if this instance equals the input and false
     * otherwise.
     */
    @Override
    public abstract boolean equals(Object obj);


    // Documented in PRingAssociated.java.

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public PRing getPRing() {
        return this;
    }

    // Methods defined in terms of the abstract methods above.

    /**
     * Recovers a ring element from the input representation.
     *
     * @param btr Representation of a ring element.
     * @return Ring element corresponding to the input.
     *
     * @throws ArithmError If the input does not represent a ring
     * element.
     */
    public final PRingElement unsafeToElement(final ByteTreeReader btr)
        throws ArithmError {
        try {
            return toElement(btr);
        } catch (final ArithmFormatException afe) {
            throw new ArithmError("Unable to read element!", afe);
        }
    }

    /**
     * Recovers an array of ring elements from its representation and
     * throws an exception if the input is incorrect.
     *
     * @param size Expected number of elements in array. If the
     * expected size (number of elements) is set to zero, then the
     * input can have any size.
     * @param btr Representation of instance.
     * @return Array of ring elements represented by the input.
     *
     * @throws ArithmError If the input does not represent an array of
     * ring elements.
     */
    public final PRingElementArray
        unsafeToElementArray(final int size, final ByteTreeReader btr)
        throws ArithmError {
        try {
            return toElementArray(size, btr);
        } catch (final ArithmFormatException afe) {
            throw new ArithmError("Failed to read array!", afe);
        }
    }

    /**
     * Returns the characteristic of this ring.
     *
     * @return Characteristic of this ring.
     */
    public final LargeInteger getCharacteristic() {
        return getPField().getOrder();
    }

    /**
     * Returns true if the input is contained in this ring and false
     * otherwise.
     *
     * @param element Ring element.
     * @return true if the input is contained in this ring and false
     *         otherwise.
     */
    public boolean contains(final PRingElement element) {
        return element.getPRing().equals(this);
    }

    /**
     * Returns an array of random ring elements.
     *
     * @param size Size of resulting array.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random ring elements.
     */
    public final PRingElement[] randomElements(final int size,
                                               final RandomSource rs,
                                               final int statDist) {
        final PRingElement[] res = new PRingElement[size];
        for (int i = 0; i < size; i++) {
            res[i] = randomElement(rs, statDist);
        }
        return res;
    }

    /**
     * Creates a ring element from a raw array of bytes. The
     * restriction of this map to any subring is injective. This
     * method should be used to create an instance from outputs from
     * hash functions etc. It should never be used to recover stored
     * ring elements. If more than {@link #getEncodeLength()} bytes
     * are input, then the output is undefined.
     *
     * @param bytes An array containing arbitrary bytes.
     * @return Ring element derived from the input.
     */
    public PRingElement toElement(final byte[] bytes) {
        return toElement(bytes, 0, bytes.length);
    }

    /**
     * Returns a byte tree representation of the elements in the
     * input.
     *
     * @param elements Ring elements.
     * @return Representation of the input <code>PRingElement[]</code>
     *         .
     */
    public ByteTreeBasic toByteTree(final PRingElement[] elements) {
        final ByteTreeBasic[] byteTrees = new ByteTreeBasic[elements.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = elements[i].toByteTree();
        }
        return new ByteTreeContainer(byteTrees);
    }

    /**
     * Recovers an array of ring elements from the given
     * representation.
     *
     * @param maxSize Maximal number of elements read.
     * @param btr Representation of an array of elements.
     * @return Array of ring elements represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     *  an array of ring elements.
     */
    public final PRingElement[] toElements(final int maxSize,
                                           final ByteTreeReader btr)
        throws ArithmFormatException {
        try {
            if (btr.getRemaining() > maxSize) {
                throw new ArithmFormatException("Too many elements!");
            }

            final PRingElement[] res = new PRingElement[btr.getRemaining()];
            for (int i = 0; i < res.length; i++) {
                res[i] = toElement(btr.getNextChild());
            }
            return res;
        } catch (final EIOException eioe) {
            throw new ArithmFormatException("Malformed byte tree!", eioe);
        }
    }

    /**
     * Returns the inner product of the input arrays.
     *
     * @param elements1 Array of ring elements.
     * @param elements2 Array of ring elements.
     * @return Inner product of inputs.
     */
    public final PRingElement innerProduct(final PRingElement[] elements1,
                                           final PRingElement[] elements2) {
        PRingElement innerProduct = getZERO();
        for (int i = 0; i < elements1.length; i++) {
            innerProduct = innerProduct.add(elements1[i].mul(elements2[i]));
        }
        return innerProduct;
    }

    /**
     * Returns the result of scaling each element of
     * <code>factors</code> by <code>factor</code> and adding the
     * corresponding element of <code>terms</code>.
     *
     * @param factors Array of ring elements.
     * @param factor Scaling element.
     * @param terms Terms added after scaling.
     * @return Array of results.
     */
    public PRingElement[] mulAdd(final PRingElement[] factors,
                                 final PRingElement factor,
                                 final PRingElement[] terms) {
        final PRingElement[] res = new PRingElement[factors.length];
        for (int i = 0; i < factors.length; i++) {
            res[i] = factors[i].mul(factor).add(terms[i]);
        }
        return res;
    }
}
