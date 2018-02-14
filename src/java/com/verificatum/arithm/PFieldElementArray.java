
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

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.util.Pair;

/**
 * Implements an array of field elements, i.e., elements
 * {@link PFieldElement} from {@link PField}. This is essentially a
 * wrapper of {@link LargeIntegerArray} in the same way that
 * {@link PFieldElement} is a wrapper of {@link LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
public final class PFieldElementArray extends PRingElementArray {

    /**
     * Field to which the elements of this array belong.
     */
    PField pField;

    /**
     * Representatives of the field elements.
     */
    LargeIntegerArray values;

    /**
     * Constructs an instance with the given elements. This assumes
     * that the inputs are canonically reduced.
     *
     * @param pField Field to which the elements of this array belong.
     * @param elements Field elements.
     */
    protected PFieldElementArray(final PField pField,
                                 final PRingElement[] elements) {
        super(pField);
        this.pField = pField;
        final LargeInteger[] tmp = pField.toLargeIntegers(elements);
        this.values = LargeIntegerArray.toLargeIntegerArray(tmp);
    }

    /**
     * Constructs an array with the elements corresponding to the
     * given representatives.
     *
     * <p>
     *
     * WARNING! Assumes that the input values are in the right
     * interval. The input array is not copied.
     *
     * @param pField Field to which the elements of this array belong.
     * @param values Representatives of the field elements.
     */
    protected PFieldElementArray(final PField pField,
                                 final LargeIntegerArray values) {
        super(pField);
        this.pField = pField;
        this.values = values;
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param pField Field to which the elements of this array belong.
     * @param size Expected number of elements in array. If the
     * expected size (number of elements) is set to zero, then the
     * input can have any size.
     * @param btr Representation of an instance.
     *
     * @throws ArithmFormatException If the contents of the file are
     *  incorrectly formatted.
     */
    protected PFieldElementArray(final PField pField,
                                 final int size,
                                 final ByteTreeReader btr)
        throws ArithmFormatException {
        super(pField);
        this.pField = pField;
        values = LargeIntegerArray.toLargeIntegerArray(size,
                                                       btr,
                                                       LargeInteger.ZERO,
                                                       pField.getOrder());
    }

    /**
     * Constructs an instance with the given number of randomly chosen
     * elements.
     *
     * @param pField Field to which the elements of this array belong.
     * @param size Number of elements to generate.
     * @param randomSource Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    protected PFieldElementArray(final PField pField,
                                 final int size,
                                 final RandomSource randomSource,
                                 final int statDist) {
        super(pField);
        this.pField = pField;
        this.values = LargeIntegerArray.random(size,
                                               pField.getOrder(),
                                               statDist,
                                               randomSource);
    }

    /**
     * Returns an array of canonical integer representatives of the
     * elements of this instance.
     *
     * @return Array of canonical integer representatives of the
     *         elements of this instance.
     */
    @CoberturaIgnore
    public LargeIntegerArray toLargeIntegerArray() {
        return values;
    }

    // Documented in PRingElementArray.java

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PFieldElementArray)) {
            return false;
        }
        final PFieldElementArray pea = (PFieldElementArray) obj;

        return pField.equals(pea.pField)
            && values.equals(((PFieldElementArray) obj).values);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public PFieldElementArray add(final PRingElementArray terms) {
        if (pRing.equals(terms.getPRing())) {
            final LargeIntegerArray newValues =
                values.modAdd(((PFieldElementArray) terms).values,
                              pField.order);
            return new PFieldElementArray(pField, newValues);
        }
        throw new ArithmError(PField.MISMATCHING_FIELDS);
    }

    @Override
    public PFieldElementArray neg() {
        return new PFieldElementArray(pField, values.modNeg(pField.order));
    }

    @Override
    public PFieldElementArray mul(final PRingElement factor) {
        if (pRing.equals(factor.getPRing())) {
            final LargeInteger integerFactor = ((PFieldElement) factor).value;
            return new PFieldElementArray(pField,
                                          values.modMul(integerFactor,
                                                        pField.order));
        }
        throw new ArithmError(PField.MISMATCHING_FIELDS);
    }

    @Override
    public PFieldElementArray mul(final PRingElementArray factors) {
        if (pRing.equals(factors.getPRing())) {
            final LargeIntegerArray integers =
                ((PFieldElementArray) factors).values;
            return new PFieldElementArray(pField,
                                          values.modMul(integers,
                                                        pField.order));
        }
        throw new ArithmError(PField.MISMATCHING_FIELDS);
    }

    @Override
    public PFieldElementArray inv() throws ArithmException {
        return new PFieldElementArray(pField, values.modInv(pField.order));
    }

    @Override
    public PFieldElement innerProduct(final PRingElementArray vector) {
        if (pRing.equals(vector.getPRing())) {
            final LargeInteger res =
                values.modInnerProduct(((PFieldElementArray) vector).values,
                                pField.order);
            return new PFieldElement(pField, res);
        }
        throw new ArithmError(PField.MISMATCHING_FIELDS);
    }

    @Override
    public PFieldElement sum() {
        return pField.toElement(values.modSum(pField.order));
    }

    @Override
    public PFieldElement prod() {
        return pField.toElement(values.modProd(pField.order));
    }

    @Override
    public PFieldElementArray prods() {
        return pField.unsafeToElementArray(values.modProds(pField.order));
    }

    @CoberturaIgnore
    @Override
    public int size() {
        return values.size();
    }

    @Override
    public PFieldElementArray permute(final Permutation permutation) {
        final LargeIntegerArray plia = values.permute(permutation);
        return new PFieldElementArray(pField, plia);
    }

    @Override
    public PFieldElementArray shiftPush(final PRingElement el) {
        final LargeIntegerArray sia =
            values.shiftPush(((PFieldElement) el).value);
        return new PFieldElementArray(pField, sia);
    }

    @Override
    public Pair<PRingElementArray, PRingElement>
        recLin(final PRingElementArray array) {

        final Pair<LargeIntegerArray, LargeInteger> p =
            values.modRecLin(((PFieldElementArray) array).values, pField.order);

        final PFieldElementArray a = new PFieldElementArray(pField, p.first);
        final PFieldElement e = new PFieldElement(pField, p.second);

        return new Pair<PRingElementArray, PRingElement>(a, e);
    }

    @Override
    public PFieldElementArray copyOfRange(final int startIndex,
                                          final int endIndex) {
        final LargeIntegerArray cia = values.copyOfRange(startIndex, endIndex);
        return new PFieldElementArray(pField, cia);
    }

    @Override
    public PFieldElement get(final int index) {
        return new PFieldElement(pField, values.get(index));
    }

    @Override
    public PFieldElement[] elements() {
        final LargeInteger[] li = values.integers();
        final PFieldElement[] elements = new PFieldElement[li.length];
        for (int i = 0; i < li.length; i++) {
            elements[i] = new PFieldElement(pField, li[i]);
        }
        return elements;
    }

    // This runs different code depending on if integer arrays are
    // stored on file or not.
    @Override
    public void free() {
        values.free();
        values = null;
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return values.toByteTree(pField.orderByteLength);
    }
}
