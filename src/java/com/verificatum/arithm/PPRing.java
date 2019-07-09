
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

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;


/**
 * Implements an immutable direct power of a field. The elements of
 * this ring are implemented by the class {@link PPRingElement}. The
 * implementation keeps track of structure, i.e., taking products in
 * different orders give different rings. Operations such as addition,
 * multiplication, and exponentiations attempts to interpret an input
 * as belonging to the same ring. If this fails, then the operation is
 * mapped to the subrings. This allows us to view a product element as
 * a container of elements from subrings.
 *
 * @author Douglas Wikstrom
 */
public final class PPRing extends PRing {

    /**
     * Underlying rings.
     */
    PRing[] pRings;

    /**
     * Fixed size of raw representation of an element of this ring.
     */
    int byteLength;

    /**
     * Indicates if this is a power of the same ring or not.
     */
    private boolean isPower;

    /**
     * Initializes the zero and unit of this ring.
     */
    private void init() {

        // This exploits the internals of ByteTree.java
        byteLength = 5;
        for (int i = 0; i < pRings.length; i++) {
            byteLength += pRings[i].getByteLength();
        }

        isPower = true;
        for (int i = 0; isPower && i < pRings.length; i++) {
            if (!pRings[i].equals(pRings[0])) {
                isPower = false;
            }
        }
    }

    /**
     * Creates the direct product ring from the given rings.
     *
     * @param pRings Underlying rings.
     */
    public PPRing(final PRing... pRings) {
        this.pRings = pRings;
        init();
    }

    /**
     * Creates the direct power ring from the given ring.
     *
     * @param pRing Underlying ring.
     * @param degree Degree of power.
     */
    public PPRing(final PRing pRing, final int degree) {
        pRings = new PRing[degree];
        Arrays.fill(pRings, pRing);
        init();
    }

    /**
     * Returns the direct product element of the inputs provided that
     * the result is contained in this ring.
     *
     * @param els Elements we take the product of.
     * @return Direct product element of the inputs.
     */
    public PPRingElement product(final PRingElement... els) {
        if (els.length != pRings.length) {
            throw new ArithmError("Wrong number of elements!");
        }
        for (int i = 0; i < pRings.length; i++) {
            if (!els[i].pRing.equals(pRings[i])) {
                throw new ArithmError("Incompatible underlying ring!");
            }
        }
        return new PPRingElement(this, els);
    }

    /**
     * Returns the direct power element of the given element.
     *
     * @param el Element array we take the product of.
     * @return Direct product element of the inputs.
     */
    public PPRingElement product(final PRingElement el) {
        if (!isPower) {
            throw new ArithmError("Ring is not a power!");
        }
        if (!el.pRing.equals(pRings[0])) {
            throw new ArithmError("Element does not belong to component ring!");
        }
        final PRingElement[] res = new PRingElement[pRings.length];
        Arrays.fill(res, el);
        return new PPRingElement(this, res);
    }

    /**
     * Returns the direct power element array of the input arrays
     * provided that the result is contained in this ring.
     *
     * @param els Elements we take the product of.
     * @return Direct product element of the inputs.
     */
    public PPRingElementArray product(final PRingElementArray... els) {
        if (els.length != pRings.length) {
            throw new ArithmError("Wrong number of elements!");
        }
        for (int i = 0; i < pRings.length; i++) {
            if (!els[i].pRing.equals(pRings[i])) {
                throw new ArithmError("Incompatible underlying ring!");
            }
        }
        return new PPRingElementArray(this, els);
    }

    /**
     * Returns the direct power element array of the input element
     * array provided that the result is contained in this ring.
     *
     * @param el Element array we take the power of.
     * @return Direct product element of the inputs.
     */
    public PPRingElementArray product(final PRingElementArray el) {
        if (!isPower) {
            throw new ArithmError("Ring is not a power!");
        }
        if (!el.pRing.equals(pRings[0])) {
            throw new ArithmError("Elements of array does not belong to "
                                  + "component ring!");
        }
        final PRingElementArray[] res = new PRingElementArray[pRings.length];
        Arrays.fill(res, el);
        return new PPRingElementArray(this, res);
    }

    /**
     * Returns the projection of this ring to the chosen indices.
     *
     * @param indices Indices of chosen components.
     * @return Projection of this ring to the chosen subring.
     */
    public PRing project(final boolean[] indices) {
        if (indices.length != pRings.length) {
            throw new ArithmError("Wrong length!");
        }

        int count = 0;
        int k = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i]) {
                count++;
            }
            if (count < 1) {
                k++;
            }
        }

        if (count < 1) {
            throw new ArithmError("Empty projection!");
        } else if (count == 1) {
            return pRings[k];
        } else {

            final PRing[] newPRings = new PRing[count];
            for (int i = 0, j = 0; i < pRings.length; i++) {
                if (indices[i]) {
                    newPRings[j++] = pRings[i];
                }
            }
            return new PPRing(newPRings);
        }
    }

    /**
     * Returns the projection of this ring at the given index.
     *
     * @param i Index on which to project
     * @return Ring at the given index.
     */
    public PRing project(final int i) {
        return pRings[i];
    }

    /**
     * Returns the factors of this ring.
     *
     * @return Factors of this ring.
     */
    public PRing[] getFactors() {
        return Arrays.copyOfRange(pRings, 0, pRings.length);
    }

    /**
     * Returns the number of subrings wrapped by this ring. This does
     * not necessarily give the dimension of the ring viewed as a
     * direct product of the underlying field.
     *
     * @return Number of subrings.
     */
    public int getWidth() {
        return pRings.length;
    }

    // Documented in PRing.java

    @Override
    public PField getPField() {
        return pRings[0].getPField();
    }

    @Override
    public PPRingElement getZERO() {
        final PRingElement[] ZEROs = new PRingElement[pRings.length];
        for (int i = 0; i < ZEROs.length; i++) {
            ZEROs[i] = pRings[i].getZERO();
        }
        return new PPRingElement(this, ZEROs);
    }

    @Override
    public PPRingElement getONE() {
        final PRingElement[] ONEs = new PRingElement[pRings.length];
        for (int i = 0; i < ONEs.length; i++) {
            ONEs[i] = pRings[i].getONE();
        }
        return new PPRingElement(this, ONEs);
    }

    @Override
    public int getByteLength() {
        return byteLength;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PPRing(");
        sb.append(pRings[0].toString());
        for (int i = 1; i < pRings.length; i++) {
            sb.append(',');
            sb.append(pRings[i].toString());
        }
        sb.append(')');
        return sb.toString();
    }

    @CoberturaIgnore
    @Override
    public int getEncodeLength() {
        return getPField().getEncodeLength();
    }

    @Override
    public PPRingElement toElement(final ByteTreeReader btr)
        throws ArithmFormatException {
        final PRingElement[] elements = new PRingElement[pRings.length];
        try {
            for (int i = 0; i < pRings.length; i++) {
                elements[i] = pRings[i].toElement(btr.getNextChild());
            }
            return new PPRingElement(this, elements);
        } catch (final EIOException eioe) {
            throw new ArithmFormatException("Malformed data!", eioe);
        }
    }

    @Override
    public PPRingElement toElement(final byte[] bytes,
                                   final int offset,
                                   final int length) {
        final PRingElement[] values = new PRingElement[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            values[i] = pRings[i].toElement(bytes, offset, length);
        }
        return new PPRingElement(this, values);
    }

    @Override
    public PPRingElement randomElement(final RandomSource rs,
                                       final int statDist) {
        final PRingElement[] values = new PRingElement[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            values[i] = pRings[i].randomElement(rs, statDist);
        }
        return new PPRingElement(this, values);
    }

    @Override
    public PPRingElementArray randomElementArray(final int size,
                                                 final RandomSource rs,
                                                 final int statDist) {
        final PRingElementArray[] values = new PRingElementArray[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            values[i] = pRings[i].randomElementArray(size, rs, statDist);
        }
        return new PPRingElementArray(this, values);
    }

    /**
     * Changes the order of the dimensions.
     *
     * @param arrays Array to be decomposed.
     * @return Decomposed array.
     */
    protected PRingElement[][] decompose(final PRingElement[] arrays) {
        if (arrays.length == 0) {

            return new PRingElement[0][];

        } else if (equals(arrays[0].pRing)) {

            final PRingElement[][] res = new PRingElement[pRings.length][];
            for (int i = 0; i < pRings.length; i++) {
                res[i] = new PRingElement[arrays.length];
            }
            for (int i = 0; i < pRings.length; i++) {
                for (int j = 0; j < arrays.length; j++) {
                    res[i][j] = ((PPRingElement) arrays[j]).values[i];
                }
            }
            return res;
        }
        throw new ArithmError("Can not decompose!");
    }

    @Override
    public PPRingElementArray
        toElementArray(final PRingElement[] elements) {

        final PRingElement[][] decomposed = decompose(elements);

        final PRingElementArray[] arrays =
            new PRingElementArray[decomposed.length];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = pRings[i].toElementArray(decomposed[i]);
        }
        return new PPRingElementArray(this, arrays);
    }

    @Override
    public PPRingElementArray toElementArray(final int size,
                                             final PRingElement element) {
        final PRingElementArray[] arrays = new PRingElementArray[pRings.length];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] =
                pRings[i].toElementArray(size,
                                         ((PPRingElement) element).values[i]);
        }
        return new PPRingElementArray(this, arrays);
    }

    @Override
    public PPRingElementArray toElementArray(final int size,
                                             final ByteTreeReader btr)
        throws ArithmFormatException {
        if (btr.getRemaining() != pRings.length) {
            throw new ArithmFormatException("Wrong number of rings!");
        }
        try {
            final PRingElementArray[] res =
                new PRingElementArray[pRings.length];
            for (int i = 0; i < pRings.length; i++) {
                res[i] = pRings[i].toElementArray(size, btr.getNextChild());
            }
            return new PPRingElementArray(this, res);
        } catch (final EIOException eioe) {
            throw new ArithmFormatException("Malformed array!", eioe);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PPRing)) {
            return false;
        }

        final PPRing pPRing = (PPRing) obj;

        if (pPRing.pRings.length != pRings.length) {
            return false;
        }

        for (int i = 0; i < pRings.length; i++) {
            if (!pRings[i].equals(pPRing.pRings[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ByteTree toByteTree() {
        return new ByteTree(getPField().toByteTree(), toByteTreeInner());
    }

    /**
     * Packs the internal structure of this instance, i.e., the tree
     * of products of fields.
     *
     * @return Representation of the structure of this instance.
     */
    protected ByteTree toByteTreeInner() {
        final ByteTree[] children = new ByteTree[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            if (pRings[i] instanceof PField) {
                children[i] = new ByteTree(new ByteTree[0]);
            } else {
                children[i] = ((PPRing) pRings[i]).toByteTreeInner();
            }
        }
        return new ByteTree(children);
    }
}
