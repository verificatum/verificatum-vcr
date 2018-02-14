
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

package com.verificatum.arithm;

import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.util.Functions;
import com.verificatum.util.Pair;

/**
 * Implements an array of instances of {@link PRingElement} belonging
 * to a {@link PRing}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRingElementArray
    implements ByteTreeConvertible, PRingAssociated {

    /**
     * Frees the resources of the input array unless the input is null
     * in which case it is simply ignored.
     *
     * @param array Array to be freed.
     */
    public static void free(final PRingElementArray array) {
        if (array != null) {
            array.free();
        }
    }

    /**
     * Underlying ring.
     */
    protected PRing pRing;

    /**
     * Constructs an array with the given underlying ring.
     *
     * @param pRing Underlying ring.
     */
    protected PRingElementArray(final PRing pRing) {
        this.pRing = pRing;
    }

    /**
     * Returns a human readable description of this instance. This
     * should only be used for debugging.
     *
     * @return Human readable description of this instance.
     */
    @Override
    public abstract String toString();

    /**
     * Returns the element-wise sum of this instance and the input.
     *
     * @param terms Array of terms.
     * @return Element-wise sum of this instance and the input.
     */
    public abstract PRingElementArray add(PRingElementArray terms);

    /**
     * Returns the element-wise additive inverse of this instance.
     *
     * @return Element-wise additive inverse.
     */
    public abstract PRingElementArray neg();

    /**
     * Returns the element-wise product of this instance and the
     * input.
     *
     * @param factor Scalar multiplier.
     * @return Element-wise product of this instance and the input.
     */
    public abstract PRingElementArray mul(PRingElement factor);

    /**
     * Returns the element-wise product of this instance and the
     * input.
     *
     * @param factorsArray Array of factors.
     * @return Element-wise product of this instance and the input.
     */
    public abstract PRingElementArray mul(PRingElementArray factorsArray);

    /**
     * Returns the element-wise inverse of this instance.
     *
     * @return Element-wise inverse of this instance.
     * @throws ArithmException If not all elements of this array can
     * be inverted.
     */
    public abstract PRingElementArray inv() throws ArithmException;

    /**
     * Returns the "inner product" of this instance and the input,
     * viewed as vectors.
     *
     * @param vector Array of elements.
     * @return Inner product of this instance and the input.
     */
    public abstract PRingElement innerProduct(PRingElementArray vector);

    /**
     * Returns the sum of the elements in this array.
     *
     * @return Sum of the elements in this array.
     */
    public abstract PRingElement sum();

    /**
     * Returns the product of the elements in this array.
     *
     * @return Product of the elements in this array.
     */
    public abstract PRingElement prod();

    /**
     * Returns the aggregated products of the elements in this array.
     *
     * @return Aggregated products of the elements in this array.
     */
    public abstract PRingElementArray prods();

    /**
     * Returns the number of elements in this array.
     *
     * @return Number of elements in this array.
     */
    public abstract int size();

    /**
     * Permutes the elements in this instance using the permutation
     * given as input.
     *
     * @param permutation Permutation used.
     * @return Permuted array of elements.
     */
    public abstract PRingElementArray permute(Permutation permutation);

    /**
     * Shifts all elements one step to the right (deleting the last
     * element) and sets the given element as the first element of the
     * new array. This is a linear-time operation in the size of this
     * array.
     *
     * @param el First element in resulting array.
     * @return Resulting array.
     */
    public abstract PRingElementArray shiftPush(PRingElement el);

    /**
     * Computes a linear recursive function. The current output is set
     * to zero. Then each produced actual output is computed by taking
     * the product of the previous output and the corresponding input
     * element plus the corresponding element of this instance. Note
     * that this means that the first output equals the first element
     * of this instance.
     *
     * @param array Array of ring elements.
     * @return Pair of the resulting array and the last element of the
     * array.
     */
    public abstract Pair<PRingElementArray, PRingElement>
        recLin(PRingElementArray array);

    /**
     * Returns a copy of the elements from the given starting index
     * (inclusive) to the given ending index (exclusive).
     *
     * @param startIndex Starting index of range.
     * @param endIndex Ending index of range.
     * @return Copy of range of elements.
     */
    public abstract PRingElementArray copyOfRange(int startIndex, int endIndex);

    /**
     * Returns the element at the given position. <b>This is a
     * linear-time operation. Do not use it to iterate over
     * elements!</b>
     *
     * @param index Index of element.
     * @return Element at the given position.
     */
    public abstract PRingElement get(int index);

    /**
     * Returns a primitive array of the elements of this instance.
     *
     * @return Array of the elements of this instance.
     */
    public abstract PRingElement[] elements();

    /**
     * Releases any resources allocated by this instance, e.g., a file
     * based implementation may delete the underlying file, or a
     * native implementation may release allocated memory.
     *
     * <p>
     *
     * WARNING! It is the responsibility of the programmer to call
     * this method and also to only call this method if this instance
     * is not used again.
     */
    public abstract void free();

    // Implemented in terms of the above.

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    /**
     * Returns the element-wise product-sum of this instance and the
     * inputs, i.e., each element in this instance is multiplied with
     * the scalar and added to the corresponding element in the input
     * array of terms.
     *
     * @param scalar Scalar multiplier.
     * @param terms Array of terms.
     * @return Element-wise product-sum of this instance and the
     * input.
     */
    public PRingElementArray mulAdd(final PRingElement scalar,
                                    final PRingElementArray terms) {
        final PRingElementArray tmp = mul(scalar);
        final PRingElementArray res = tmp.add(terms);
        tmp.free();
        return res;
    }

    /**
     * Returns the element-wise product-sum of this instance and the
     * inputs, i.e., each element in this instance is multiplied with
     * the corresponding scalar in the array of scalars and added to
     * the corresponding element in the input array of terms.
     *
     * @param scalars Scalar multipliers.
     * @param terms Array of terms.
     * @return Element-wise product-sum of this instance and the
     * input.
     */
    public PRingElementArray mulAdd(final PRingElementArray scalars,
                                    final PRingElementArray terms) {
        final PRingElementArray tmp = mul(scalars);
        final PRingElementArray res = tmp.add(terms);
        tmp.free();
        return res;
    }

    // Documented in PRingAssociated.java.

    @Override
    public PRing getPRing() {
        return pRing;
    }
}
