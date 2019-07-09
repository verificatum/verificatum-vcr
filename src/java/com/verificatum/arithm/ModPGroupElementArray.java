
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
import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.TempFile;

/**
 * Implements an array of immutable group elements of a {@link
 * ModPGroup}. This is a wrapper of {@link LargeIntegerArray} in the
 * same way as {@link ModPGroupElement} is a wrapper of {@link
 * LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
public final class ModPGroupElementArray extends PGroupElementArray {

    /**
     * Stores canonical representatives of the group elements of this
     * instance.
     */
    public LargeIntegerArray values;

    /**
     * Byte tree file. We cache a byte tree on file if arrays are file
     * mapped.
     */
    private ByteTreeBasic byteTree;

    /**
     * Constructs an instance over the given group and with the group
     * elements derived from the given array of integers. It is the
     * responsibility of the programmer to ensure that all integers
     * are canonical representatives of group elements.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param values Integer representatives of group elements.
     */
    protected ModPGroupElementArray(final PGroup pGroup,
                                    final LargeIntegerArray values) {
        super(pGroup);
        this.values = values;
    }

    /**
     * Constructs an instance over the given group and with the given
     * group elements.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param elements Group elements.
     */
    protected ModPGroupElementArray(final ModPGroup pGroup,
                                    final PGroupElement[] elements) {
        super(pGroup);

        final LargeInteger[] integers = pGroup.toLargeIntegers(elements);
        this.values = LargeIntegerArray.toLargeIntegerArray(integers);
    }

    /**
     * Constructs the concatenation of the inputs.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param arrays Arrays of group elements.
     */
    protected ModPGroupElementArray(final ModPGroup pGroup,
                                    final PGroupElementArray... arrays) {
        super(pGroup);

        final LargeIntegerArray[] allValues =
            new LargeIntegerArray[arrays.length];

        for (int i = 0; i < allValues.length; i++) {
            if (!pGroup.equals(arrays[i].getPGroup())) {
                throw new ArithmError("Mismatching groups!");
            }
            allValues[i] = ((ModPGroupElementArray) arrays[i]).values;
        }
        this.values = LargeIntegerArray.toLargeIntegerArray(allValues);
    }

    /**
     * Creates an instance containing the given number of copies of
     * the input element.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param size Number of elements to generate.
     * @param element Element to use for all components of the the
     * resulting array.
     */
    protected ModPGroupElementArray(final PGroup pGroup,
                                    final int size,
                                    final PGroupElement element) {
        super(pGroup);
        values = LargeIntegerArray.fill(size,
                                        ((ModPGroupElement) element).value);
    }

    /**
     * Constructs a random element.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param size Number of elements to generate.
     * @param randomSource Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    protected ModPGroupElementArray(final PGroup pGroup,
                                    final int size,
                                    final RandomSource randomSource,
                                    final int statDist) {
        super(pGroup);
        final LargeInteger modulus = ((ModPGroup) pGroup).modulus;
        final LargeIntegerArray lia =
            LargeIntegerArray.random(size, modulus, statDist, randomSource);
        values = lia.modPow(((ModPGroup) pGroup).coOrder, modulus);
        lia.free();
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param size Expected number of elements in array. If this is
     * set to zero, then the input representation can have
     * any size.
     * @param btr Representation of an instance.
     *
     * @throws ArithmFormatException If the contents of the iterator
     *  are incorrectly formatted.
     */
    protected ModPGroupElementArray(final ModPGroup pGroup,
                                    final int size,
                                    final ByteTreeReader btr)
        throws ArithmFormatException {
        super(pGroup);

        final LargeInteger modulus = pGroup.modulus;

        values = LargeIntegerArray.toLargeIntegerArray(size,
                                                       btr,
                                                       LargeInteger.ONE,
                                                       modulus);
        if (!values.quadraticResidues(modulus)) {
            throw new ArithmFormatException("Quadratic non-residue!");
        }
    }

    // Documented in PGroupElementArray.java

    @Override
    public PGroupElementIterator getIterator() {
        return new ModPGroupElementIterator((ModPGroup) pGroup,
                                            values.getIterator());
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public ByteTreeBasic toByteTree() {

        ByteTreeBasic btb = null;
        if (byteTree == null) {
            btb = values.toByteTree(((ModPGroup) pGroup).modulusByteLength);
        }

        if (values instanceof LargeIntegerArrayF) {

            if (btb != null) {
                byteTree = btb;
            }
            return byteTree;

        } else {
            return btb;
        }
    }

    @Override
    public PGroupElementArray mul(final PGroupElementArray factors) {
        if (!pGroup.equals(factors.pGroup)) {
            throw new ArithmError(PGroup.MISMATCHING_GROUPS);
        }
        final LargeIntegerArray res =
            values.modMul(((ModPGroupElementArray) factors).values,
                          ((ModPGroup) pGroup).modulus);
        return new ModPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray inv() {
        try {
            final LargeIntegerArray inverses =
                values.modInv(((ModPGroup) pGroup).modulus);
            return new ModPGroupElementArray(pGroup, inverses);
        } catch (final ArithmException ae) {

            // UNCOVERABLE (Every element is invertible.)
            throw new ArithmError("This is a bug in the instantiation code!",
                                  ae);
        }
    }

    @Override
    public PGroupElementArray exp(final PRingElementArray exponents) {
        if (pGroup.pRing.equals(exponents.pRing)) {
            return exp(((PFieldElementArray) exponents).values);
        } else {
            throw new ArithmError(PGroup.MISMATCHING_GROUPS);
        }
    }

    /**
     * Takes this instance to the element-wise power of the input
     * integers.
     *
     * @param exponents Exponents used to take powers.
     * @return Array of results.
     */
    public PGroupElementArray exp(final LargeIntegerArray exponents) {
        final LargeIntegerArray res =
            values.modPow(exponents, ((ModPGroup) pGroup).modulus);
        return new ModPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray exp(final PRingElement exponent) {
        if (pGroup.pRing.equals(exponent.pRing)) {
            return exp(((PFieldElement) exponent).value);
        } else {
            throw new ArithmError(PGroup.MISMATCHING_GROUPS);
        }
    }

    /**
     * Takes this instance to the element-wise power of the input
     * integer.
     *
     * @param exponent Exponent used to take powers.
     * @return Array of results.
     */
    public PGroupElementArray exp(final LargeInteger exponent) {
        final LargeIntegerArray res =
            values.modPow(exponent, ((ModPGroup) pGroup).modulus);
        return new ModPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElement expProd(final PRingElementArray exponents) {
        if (pGroup.pRing.equals(exponents.pRing)) {
            return expProd(((PFieldElementArray) exponents).values);
        } else {
            throw new ArithmError(PGroup.MISMATCHING_GROUPS);
        }
    }

    /**
     * Takes this instance to the product power of the input integers.
     *
     * @param exponents Exponents used to take powers.
     * @return Resulting product power.
     */
    public PGroupElement expProd(final LargeIntegerArray exponents) {
        final LargeInteger li =
            values.modPowProd(exponents, ((ModPGroup) pGroup).modulus);
        return new ModPGroupElement(pGroup, li);
    }

    @Override
    public PGroupElement prod() {
        final LargeInteger li = values.modProd(((ModPGroup) pGroup).modulus);
        return new ModPGroupElement(pGroup, li);
    }

    @Override
    public PGroupElement get(final int index) {
        return new ModPGroupElement(pGroup, values.get(index));
    }

    @Override
    public int compareTo(final PGroupElementArray array) {
        if (array instanceof ModPGroupElementArray) {
            final ModPGroupElementArray modarray =
                (ModPGroupElementArray) array;
            if (modarray.pGroup.equals(pGroup)) {
                return values.compareTo(modarray.values);
            } else {
                throw new ArithmError(PGroup.MISMATCHING_GROUPS);
            }
        } else {
            throw new ArithmError("Illegal comparison!");
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModPGroupElementArray)) {
            return false;
        }

        final ModPGroupElementArray array = (ModPGroupElementArray) obj;
        return pGroup.equals(array.pGroup) && values.equals(array.values);
    }

    @Override
    public boolean[] equalsAll(final PGroupElementArray bArray) {
        return values.equalsAll(((ModPGroupElementArray) bArray).values);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public PGroupElementArray permute(final Permutation permutation) {
        final LargeIntegerArray plia = values.permute(permutation);
        return new ModPGroupElementArray(pGroup, plia);
    }

    @Override
    public PGroupElementArray shiftPush(final PGroupElement el) {
        final LargeIntegerArray sia =
            values.shiftPush(((ModPGroupElement) el).value);
        return new ModPGroupElementArray(pGroup, sia);
    }

    @Override
    public PGroupElementArray copyOfRange(final int startIndex,
                                          final int endIndex) {
        final LargeIntegerArray cia = values.copyOfRange(startIndex, endIndex);
        return new ModPGroupElementArray(pGroup, cia);
    }

    @Override
    public PGroupElementArray extract(final boolean[] valid) {
        return new ModPGroupElementArray(pGroup, values.extract(valid));
    }

    @Override
    public PGroupElement[] elements() {
        return ((ModPGroup) pGroup).toElements(values.integers());
    }

    @Override
    public void free() {
        values.free();
        values = null;
        if (byteTree != null) {
            TempFile.delete(((ByteTreeF) byteTree).file);
        }
    }
}
