
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

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.eio.ByteTreeBasic;


/**
 * Implements an immutable element of a {@link PPRing} instance.
 *
 * @author Douglas Wikstrom
 */
public final class PPRingElement extends PRingElement {

    /**
     * Underlying ring elements.
     */
    PRingElement[] values;

    /**
     * Creates an instance. The input array is not copied.
     *
     * @param pRing Ring to which the created instance belongs.
     * @param values Representative of the created ring element.
     */
    protected PPRingElement(final PPRing pRing, final PRingElement... values) {
        super(pRing);
        this.values = values;
    }

    /**
     * Returns the projection of this element to the given indices.
     *
     * @param indices Indices on which we project.
     * @return Projection of this element to the chosen indices.
     */
    public PRingElement project(final boolean[] indices) {

        if (values.length != indices.length) {
            throw new ArithmError("Mismatching degrees!");
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
            return values[k];
        } else {

            final PRingElement[] res = new PRingElement[count];
            for (int i = 0, j = 0; i < indices.length; i++) {
                if (indices[i]) {
                    res[j++] = values[i];
                }
            }

            final PRing respPRing = ((PPRing) pRing).project(indices);
            return new PPRingElement((PPRing) respPRing, res);
        }
    }

    /**
     * Returns the projection of this element at the given index.
     *
     * @param i Index on which to project
     * @return Element at the given index.
     */
    public PRingElement project(final int i) {
        return values[i];
    }

    /**
     * Returns the "factorization" of this element, i.e., an array
     * containing the underlying elements of this element.
     *
     * @return Factorization of this element.
     */
    public PRingElement[] getFactors() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Returns the ring associated with this element.
     *
     * @return Ring associated with this element.
     */
    @CoberturaIgnore
    public PPRing getPPRing() {
        return (PPRing) pRing;
    }

    // Documented in PRingElement.java

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values[i].toString());
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return pRing.toByteTree(values);
    }

    /**
     * Adds the input to this instance. If the input belongs to the
     * same ring as this instance, then the sum is defined in the
     * natural way and otherwise an attempt is made to add the input
     * to the components of this instance.
     *
     * @param el Element to add to this instance.
     * @return Sum of this instance and the input.
     */
    @Override
    public PPRingElement add(final PRingElement el) {
        final PRingElement[] res = new PRingElement[values.length];

        if (el.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(((PPRingElement) el).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(el);
            }
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public PPRingElement neg() {
        final PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].neg();
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param el Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    @Override
    public PPRingElement mul(final PRingElement el) {
        final PRingElement[] res = new PRingElement[values.length];

        if (el.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(((PPRingElement) el).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(el);
            }
        }
        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public PPRingElement inv() throws ArithmException {
        final PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }

        return new PPRingElement((PPRing) pRing, res);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof PPRingElement) {

            final PRingElement[] ovalues = ((PPRingElement) obj).values;

            if (values.length == ovalues.length) {

                for (int i = 0; i < values.length; i++) {
                    if (!values[i].equals(ovalues[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
