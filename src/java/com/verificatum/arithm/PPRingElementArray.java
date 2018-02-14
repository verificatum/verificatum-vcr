
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

import java.util.Arrays;

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.util.Pair;


/**
 * Implements an array of instances of {@link PRingElement}.
 *
 * @author Douglas Wikstrom
 */
public final class PPRingElementArray extends PRingElementArray {

    /**
     * Representation of this array.
     */
    PRingElementArray[] values;

    /**
     * Constructs an array with the given underlying ring.
     *
     * @param pRing Underlying ring.
     * @param values Underlying values.
     */
    protected PPRingElementArray(final PPRing pRing,
                                 final PRingElementArray[] values) {
        super(pRing);
        this.values = Arrays.copyOfRange(values, 0, values.length);
    }

    /**
     * Returns the projection of this element at the given index.
     *
     * @param i Index on which to project
     * @return Element array at the given index.
     */
    public PRingElementArray project(final int i) {
        return values[i];
    }

    /**
     * Returns the factors of this element array.
     *
     * @return Factors of this element array.
     */
    public PRingElementArray[] getFactors() {
        return Arrays.copyOfRange(values, 0, values.length);
    }

    // Documented in PRingElementArray.java

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(values[0].toString());
        for (int i = 1; i < values.length; i++) {
            sb.append(',');
            sb.append(values[i].toString());
        }
        return sb.toString();
    }

    @Override
    public ByteTreeBasic toByteTree() {
        final ByteTreeBasic[] btb = new ByteTreeBasic[values.length];
        for (int i = 0; i < values.length; i++) {
            btb[i] = values[i].toByteTree();
        }
        return new ByteTreeContainer(btb);
    }

    /**
     * Adds the input to this instance. If the input belongs to the
     * same ring as this instance, then the sum is defined in the
     * natural way and otherwise an attempt is made to add the input
     * to the components of this instance.
     *
     * @param terms Element to add to this instance.
     * @return Sum of this instance and the input.
     */
    @Override
    public PPRingElementArray add(final PRingElementArray terms) {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        if (terms.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(((PPRingElementArray) terms).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(terms);
            }
        }

        return new PPRingElementArray((PPRing) pRing, res);
    }

    @Override
    public PPRingElementArray neg() {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].neg();
        }
        return new PPRingElementArray((PPRing) pRing, res);
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param factor Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    @Override
    public PPRingElementArray mul(final PRingElement factor) {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        if (factor.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(((PPRingElement) factor).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(factor);
            }
        }

        return new PPRingElementArray((PPRing) pRing, res);
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param factors Elements to multiply with this instance.
     * @return Product of this instance and the input.
     */
    @Override
    public PPRingElementArray mul(final PRingElementArray factors) {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        if (factors.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i]
                    .mul(((PPRingElementArray) factors).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(factors);
            }
        }

        return new PPRingElementArray((PPRing) pRing, res);
    }

    @Override
    public PPRingElementArray inv() throws ArithmException {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }
        return new PPRingElementArray((PPRing) pRing, res);
    }

    /**
     * Takes the "inner product" of this instance and the input. If
     * the input belongs to the same ring as this instance, then the
     * "inner product" is defined in the natural way and otherwise an
     * attempt is made to multiply the input with the components of
     * this instance.
     *
     * @param vector Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    @Override
    public PPRingElement innerProduct(final PRingElementArray vector) {
        final PRingElement[] res = new PRingElement[values.length];

        if (vector.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i]
                    .innerProduct(((PPRingElementArray) vector).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].innerProduct(vector);
            }
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public PPRingElement sum() {
        final PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].sum();
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public PPRingElement prod() {
        final PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].prod();
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public PPRingElementArray prods() {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].prods();
        }
        return new PPRingElementArray((PPRing) pRing, res);
    }

    @Override
    public int size() {
        return values[0].size();
    }

    @Override
    public PPRingElementArray permute(final Permutation permutation) {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].permute(permutation);
        }
        return new PPRingElementArray((PPRing) pRing, res);
    }

    @Override
    public PRingElementArray shiftPush(final PRingElement el) {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].shiftPush(((PPRingElement) el).values[i]);
        }
        return new PPRingElementArray((PPRing) pRing, res);
    }

    @Override
    public Pair<PRingElementArray, PRingElement>
        recLin(final PRingElementArray array) {

        final PRingElementArray[] res = new PRingElementArray[values.length];
        final PRingElement[] elementRes = new PRingElement[values.length];

        if (array.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                final Pair<PRingElementArray, PRingElement> p =
                    values[i].recLin(((PPRingElementArray) array).values[i]);
                res[i] = p.first;
                elementRes[i] = p.second;
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                final Pair<PRingElementArray, PRingElement> p =
                    values[i].recLin(array);
                res[i] = p.first;
                elementRes[i] = p.second;
            }
        }

        final PPRingElementArray a =
            new PPRingElementArray((PPRing) pRing, res);
        final PPRingElement e = new PPRingElement((PPRing) pRing, elementRes);
        return new Pair<PRingElementArray, PRingElement>(a, e);
    }

    @Override
    public PPRingElementArray copyOfRange(final int startIndex,
                                          final int endIndex) {
        final PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].copyOfRange(startIndex, endIndex);
        }
        return new PPRingElementArray((PPRing) pRing, res);
    }

    @Override
    public PPRingElement get(final int index) {
        final PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].get(index);
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public PPRingElement[] elements() {
        final PRingElement[][] tmp = new PRingElement[values.length][];
        for (int i = 0; i < values.length; i++) {
            tmp[i] = values[i].elements();
        }

        final PPRingElement[] res = new PPRingElement[tmp[0].length];
        for (int j = 0; j < res.length; j++) {

            final PRingElement[] els = new PRingElement[values.length];
            for (int i = 0; i < values.length; i++) {
                els[i] = tmp[i][j];
            }
            res[j] = new PPRingElement((PPRing) pRing, els);
        }
        return res;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PPRingElementArray)) {
            return false;
        }

        final PPRingElementArray array = (PPRingElementArray) obj;

        if (!pRing.equals(array.getPRing())) {
            return false;
        }

        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals(array.values[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void free() {
        for (int i = 0; i < values.length; i++) {
            values[i].free();
        }
        values = null;
    }
}
