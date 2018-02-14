
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


/**
 * An array of elements associated with a {@link PPGroup}-instance.
 *
 * @author Douglas Wikstrom
 */
public final class PPGroupElementArray extends PGroupElementArray {

    /**
     * Representation of this array.
     */
    PGroupElementArray[] values;

    /**
     * Constructs an array of elements of the given group.
     *
     * @param pGroup Group to which the elements of this array
     * belongs.
     * @param values Elements of this array.
     */
    protected PPGroupElementArray(final PGroup pGroup,
                                  final PGroupElementArray[] values) {
        super(pGroup);
        this.values = Arrays.copyOfRange(values, 0, values.length);
    }

    /**
     * Returns the projection of this array to the subgroup defined by
     * the given indices.
     *
     * @param indices Indices on which we project.
     * @return Projection of this array to the subgroup.
     */
    public PGroupElementArray project(final boolean[] indices) {
        if (values.length != indices.length) {
            throw new ArithmError("Mismatching degrees!");
        }
        int count = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i]) {
                count++;
            }
        }
        if (count < 1) {
            throw new ArithmError("Empty projection!");
        } else if (count == 1) {
            for (int i = 0; i < indices.length; i++) {
                if (indices[i]) {
                    return values[i];
                }
            }
            throw new ArithmError("Indices are empty! (this can not happen)");

        } else {

            final PGroupElementArray[] res = new PGroupElementArray[count];
            for (int i = 0, j = 0; i < indices.length; i++) {
                if (indices[i]) {
                    res[j++] = values[i];
                }
                i++;
            }

            final PGroup respPGroup = ((PPGroup) pGroup).project(indices);
            return new PPGroupElementArray(respPGroup, res);
        }
    }

    /**
     * Returns the projection of this element at the given index.
     *
     * @param i Index on which to project
     * @return Element array at the given index.
     */
    public PGroupElementArray project(final int i) {
        return values[i];
    }

    /**
     * Returns the "factorization" of this element, i.e., an array
     * containing the underlying arrays of this element.
     *
     * @return Factorization of this element.
     */
    public PGroupElementArray[] getFactors() {
        return Arrays.copyOf(values, values.length);
    }

    // Documented in PGroupElementArray.java

    @Override
    public PGroupElementIterator getIterator() {
        final PGroupElementIterator[] iterators =
            new PGroupElementIterator[values.length];
        for (int i = 0; i < values.length; i++) {
            iterators[i] = values[i].getIterator();
        }
        return new PPGroupElementIterator((PPGroup) pGroup, iterators);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append('(');
        sb.append(values[0].toString());
        for (int i = 1; i < values.length; i++) {
            sb.append(',');
            sb.append(values[i].toString());
        }
        sb.append(')');
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

    @Override
    public PPGroupElement get(final int index) {
        final PGroupElement[] res = new PGroupElement[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].get(index);
        }
        return ((PPGroup) pGroup).toElement(res);
    }

    @Override
    public PGroupElementArray mul(final PGroupElementArray factors) {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (factors.pGroup.equals(pGroup)) {

            final PGroupElementArray[] fvalues =
                ((PPGroupElementArray) factors).values;

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(fvalues[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(factors);
            }

        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray inv() {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray exp(final PRingElementArray exponents) {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (exponents.pRing.equals(pGroup.pRing)) {

            final PRingElementArray[] evalues =
                ((PPRingElementArray) exponents).values;

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(evalues[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponents);
            }

        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray exp(final PRingElement exponent) {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (exponent.pRing.equals(pGroup.pRing)) {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(((PPRingElement) exponent).values[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponent);
            }

        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElement prod() {
        final PGroupElement[] res = new PGroupElement[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].prod();
        }
        return ((PPGroup) pGroup).toElement(res);
    }

    @Override
    public PGroupElement expProd(final PRingElementArray exponents) {
        final PGroupElement[] res = new PGroupElement[values.length];

        if (exponents.pRing.equals(pGroup.pRing)) {

            final PRingElementArray[] exponentss =
                ((PPRingElementArray) exponents).values;

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].expProd(exponentss[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].expProd(exponents);
            }

        }
        return ((PPGroup) pGroup).toElement(res);
    }

    @Override
    public int compareTo(final PGroupElementArray array) {
        if (array instanceof PPGroupElementArray) {
            final PPGroupElementArray pparray = (PPGroupElementArray) array;
            if (pparray.pGroup.equals(pGroup)) {
                for (int i = 0; i < values.length; i++) {
                    final int cmp = values[i].compareTo(pparray.values[i]);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }
        }
        throw new ArithmError("Illegal comparison!");
    }

    @Override
    public boolean equals(final Object array) {
        if (array instanceof PPGroupElementArray) {
            final PPGroupElementArray pparray = (PPGroupElementArray) array;

            if (pparray.pGroup.equals(pGroup)) {
                for (int i = 0; i < values.length; i++) {
                    if (!pparray.values[i].equals(values[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean[] equalsAll(final PGroupElementArray array) {
        if (array.pGroup.equals(pGroup)) {
            final PGroupElementArray[] arrays =
                ((PPGroupElementArray) array).values;

            final boolean[] res = new boolean[size()];
            Arrays.fill(res, true);

            for (int i = 0; i < values.length; i++) {
                final boolean[] tmp = values[i].equalsAll(arrays[i]);
                for (int j = 0; j < res.length; j++) {
                    res[j] = res[j] && tmp[j];
                }
            }
            return res;
        } else {
            throw new ArithmError("Wrong group or size!");
        }
    }

    @Override
    public int size() {
        return values[0].size();
    }

    @Override
    public PGroupElementArray permute(final Permutation permutation) {

        final PGroupElementArray[] res = new PGroupElementArray[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].permute(permutation);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray shiftPush(final PGroupElement el) {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].shiftPush(((PPGroupElement) el).values[i]);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PPGroupElementArray copyOfRange(final int startIndex,
                                           final int endIndex) {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].copyOfRange(startIndex, endIndex);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElementArray extract(final boolean[] valid) {
        final PGroupElementArray[] res = new PGroupElementArray[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].extract(valid);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    @Override
    public PGroupElement[] elements() {
        final PGroupElement[][] tmp = new PGroupElement[values.length][];
        for (int i = 0; i < values.length; i++) {
            tmp[i] = values[i].elements();
        }
        return ((PPGroup) pGroup).compose(tmp);
    }

    @Override
    public void free() {
        for (int i = 0; i < values.length; i++) {
            values[i].free();
        }
        values = null;
    }
}
