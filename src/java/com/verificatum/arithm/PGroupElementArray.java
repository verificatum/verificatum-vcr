
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


/**
 * Implements an "array" of immutable group elements of a group of
 * prime order implemented as {@link PGroup}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PGroupElementArray
    implements ByteTreeConvertible, PGroupAssociated {

    /**
     * Frees the resources of the input array unless the input is null
     * in which case it is simply ignored.
     *
     * @param array Array to be freed.
     */
    public static void free(final PGroupElementArray array) {
        if (array != null) {
            array.free();
        }
    }

    /**
     * Group to which the elements of this array belongs.
     */
    PGroup pGroup;

    /**
     * Constructs an empty array of elements of the given group.
     *
     * @param pGroup Group to which the elements of this array
     * belongs.
     */
    protected PGroupElementArray(final PGroup pGroup) {
        this.pGroup = pGroup;
    }

    /**
     * Returns an iterator over the elements of this instance.
     *
     * @return Iterator over the elements of this instance.
     */
    public abstract PGroupElementIterator getIterator();

    /**
     * Returns a human readable description of this instance. This
     * should only be used for debugging.
     *
     * @return Human readable description of this instance.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        final PGroupElementIterator ei = getIterator();

        sb.append('(');
        for (;;) {
            final PGroupElement el = ei.next();
            if (el == null) {
                break;
            } else {
                sb.append(el.toString());
                sb.append(',');
            }
        }

        sb.append(')');
        return sb.toString();
    }

    /**
     * Returns the element at the given index. Note that the running
     * time of this operation may be linear in <code>index</code>.
     *
     * @param index Index of element.
     * @return Element at the given position.
     */
    public abstract PGroupElement get(int index);

    /**
     * Returns the element-wise product of this instance and the
     * input.
     *
     * @param factorsArray Factors to multiply with.
     * @return Element-wise product of this instance and the input.
     */
    public abstract PGroupElementArray mul(PGroupElementArray factorsArray);

    /**
     * Returns the element-wise inverse of this instance.
     *
     * @return Element-wise inverse of this instance.
     */
    public abstract PGroupElementArray inv();

    /**
     * Computes the element-wise power of the elements in this
     * instance to the given power.
     *
     * @param exponent Exponent to use when taking powers.
     * @return Element-wise power of the elements in this instance to
     * the given exponent.
     */
    public abstract PGroupElementArray exp(PRingElement exponent);

    /**
     * Computes the element-wise power of the elements in this
     * instance to the powers in the input array of exponents.
     *
     * @param exponentsArray Exponents to use when taking powers.
     * @return Element-wise power of the elements in this instance to
     * the powers in the input array of exponents.
     */
    public abstract PGroupElementArray exp(PRingElementArray exponentsArray);

    /**
     * Computes the product of the elements in this instance.
     *
     * @return Product of the elements in this instance.
     */
    public abstract PGroupElement prod();

    /**
     * Computes the product of the elements in this instance
     * exponentiated to the powers in the input array.
     *
     * @param exponentsArray Exponents to use when taking powers.
     * @return Product of the elements in this instance exponentiated
     * to the powers in the input array
     */
    public abstract PGroupElement expProd(PRingElementArray exponentsArray);

    /**
     * Orders the arrays over this group "lexicographically". The
     * ordering is obviously not compatible with the binary group
     * operator in any interesting way, but it is useful to have some
     * ordering to be able to sort elements.
     *
     * @param array Instance to which this instance is compared.
     * @return -1, 0, or 1 depending on if this array comes before, is
     * equal to, or comes after the input.
     */
    public abstract int compareTo(PGroupElementArray array);

    /**
     * Returns true if and only if the elements in this instance
     * equals the elements in the input.
     *
     * @param array Array of group elements.
     * @return true or false depending on if the elements in this
     * instance equal the elements in the input or not.
     */
    @Override
    public abstract boolean equals(Object array);

    /**
     * Performs an element-wise equality test of this instance and the
     * input and outputs the results of the tests as an array of
     * boolean.
     *
     * @param otherArray Array of group elements.
     * @return Array of equality testing results.
     */
    public abstract boolean[] equalsAll(PGroupElementArray otherArray);

    /**
     * Returns the number of elements in this array.
     *
     * @return Number of elements in this array.
     */
    public abstract int size();

    /**
     * Permute the elements in this instance using the permutation
     * given as input.
     *
     * @param permutation Permutation used.
     * @return Permuted list of elements.
     */
    public abstract PGroupElementArray permute(Permutation permutation);

    /**
     * Shifts all elements one step to the right (deleting the last
     * element) and sets the given element as the first element of the
     * array. This is a linear-time operation.
     *
     * @param el First element in resulting array.
     * @return Resulting array.
     */
    public abstract PGroupElementArray shiftPush(PGroupElement el);

    /**
     * Returns a copy of the elements from the given starting index
     * (inclusive) to the given ending index (exclusive).
     *
     * @param startIndex Starting index of range.
     * @param endIndex Ending index of range.
     * @return Copy of range of elements.
     */
    public abstract PGroupElementArray copyOfRange(int startIndex,
                                                   int endIndex);

    /**
     * Returns an array containing the elements in this instance at
     * the positions where <code>valid</code> contains true.
     *
     * @param valid Array of booleans.
     * @return Array of group elements.
     */
    public abstract PGroupElementArray extract(boolean[] valid);

    /**
     * Returns a primitive array containing the group elements of this
     * instance.
     *
     * @return Array of the group elements of this instance.
     */
    public abstract PGroupElement[] elements();

    /**
     * Releases any resources allocated by this instance, e.g., a file
     * based implementation may delete the underlying file. It is the
     * responsibility of the programmer to only call this method if
     * this instance is not used again.
     */
    public abstract void free();

    // ############ Implemented in terms of the above. ###########

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    /**
     * Computes the element-wise power of the elements in this
     * instance to the given power and multiplies this by the given
     * factors.
     *
     * @param exponent Exponent to use when taking powers.
     * @param factorsArray Mulipliers.
     * @return Element-wise power of the elements in this instance to
     * the given power.
     */
    public PGroupElementArray expMul(final PRingElement exponent,
                                     final PGroupElementArray factorsArray) {
        final PGroupElementArray tmp = exp(exponent);
        final PGroupElementArray res = tmp.mul(factorsArray);
        tmp.free();
        return res;
    }

    /**
     * Computes the element-wise power the elements in this instance
     * to the given powers and multiplies this by the given factors.
     *
     * @param exponentsArray Exponents to use when taking powers.
     * @param factorsArray Mulipliers.
     * @return Element-wise power of the elements in this instance to
     * the given power.
     */
    public PGroupElementArray expMul(final PRingElementArray exponentsArray,
                                     final PGroupElementArray factorsArray) {
        final PGroupElementArray tmp = exp(exponentsArray);
        final PGroupElementArray res = tmp.mul(factorsArray);
        tmp.free();
        return res;
    }

    // Documented in PGroupAssociated.java

    @Override
    public PGroup getPGroup() {
        return pGroup;
    }
}
