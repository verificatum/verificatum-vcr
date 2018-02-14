
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.verificatum.eio.ByteTreeReader;
import com.verificatum.util.ArrayWorker;


/**
 * Abstract class representing a basic group of prime order for
 * cryptographic use. Elements in the group are represented by the
 * abstract class {@link BPGroupElement}. The {@link
 * BPGroupElementArrayIM} and {@link BPGroupElementArrayF} classes is
 * used for arrays of elements.
 *
 * @author Douglas Wikstrom
 */
public abstract class BPGroup extends PGroup {

    /**
     * Creates a group. It is the responsibility of the programmer to
     * initialize this instance by calling {@link PGroup#init(PRing)}.
     */
    protected BPGroup() {
        super();
    }

    /**
     * Creates a group with the given associated ring.
     *
     * @param pRing Ring associated with this instance.
     */
    protected BPGroup(final PRing pRing) {
        super(pRing);
    }

    /**
     * It is possible to instantiate elements that have not been fully
     * verified using {@link
     * PGroup#unsafeToElement(ByteTreeReader)}. This is needed to
     * implement use threading when reading elements from file.
     *
     * @param elements Elements to be verified.
     * @throws ArithmFormatException If any element in the input does
     * not verify correctly. The first encountered element that can
     * not be verified is the source of the exception.
     */
    protected void verifyUnsafe(final PGroupElement[] elements)
        throws ArithmFormatException {

        // We collect potential exceptions and report the first one.
        final LinkedList<ArithmFormatException> list =
            new LinkedList<ArithmFormatException>();
        final List<ArithmFormatException> exceptions =
            Collections.synchronizedList(list);

        final ArrayWorker worker =
            new ArrayWorker(elements.length) {
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {
                        try {
                            ((BPGroupElement) elements[i]).verifyUnsafe();
                            exceptions.add(null);
                        } catch (final ArithmFormatException afe) {
                            exceptions.add(afe);
                        }
                    }
                }
            };
        worker.work();

        for (final ArithmFormatException afe : exceptions) {
            if (afe != null) {
                throw afe;
            }
        }
    }

    // Documented in PGroup.java

    @Override
    public PGroupElementArray
        toElementArray(final PGroupElement[] elements) {

        if (LargeIntegerArray.inMemory) {
            return new BPGroupElementArrayIM(this, elements);
        } else {
            return new BPGroupElementArrayF(this, elements);
        }
    }

    @Override
    public PGroupElementArray
        toElementArray(final PGroupElementArray... arrays) {

        for (int i = 0; i < arrays.length; i++) {
            if (!arrays[i].getPGroup().equals(this)) {
                final String e =
                    "Attempting to concatenate elements from different groups!";
                throw new ArithmError(e);
            }
        }

        if (LargeIntegerArray.inMemory) {

            int total = 0;
            for (int i = 0; i < arrays.length; i++) {
                total += arrays[i].size();
            }

            final PGroupElement[] res = new PGroupElement[total];
            int offset = 0;
            for (int i = 0; i < arrays.length; i++) {
                final int len = arrays[i].size();
                System.arraycopy(arrays[i].elements(), 0, res, offset, len);
                offset += len;
            }
            return new BPGroupElementArrayIM(this, res);

        } else {
            return new BPGroupElementArrayF(this, arrays);
        }
    }

    @Override
    public PGroupElementArray toElementArray(final int size,
                                             final ByteTreeReader btr)
        throws ArithmFormatException {

        if (LargeIntegerArray.inMemory) {
            return new BPGroupElementArrayIM(this, size, btr, true);
        } else {
            return new BPGroupElementArrayF(this, size, btr, true);
        }
    }

    @Override
    public PGroupElementArray
        unsafeToElementArray(final int size, final ByteTreeReader btr) {
        try {
            if (LargeIntegerArray.inMemory) {
                return new BPGroupElementArrayIM(this, size, btr, false);
            } else {
                return new BPGroupElementArrayF(this, size, btr, false);
            }
        } catch (final ArithmFormatException afe) {
            throw new ArithmError("Malformed array!", afe);
        }
    }

    @Override
    public PGroupElementArray toElementArray(final int size,
                                             final PGroupElement element) {
        if (LargeIntegerArray.inMemory) {

            final PGroupElement[] res = new PGroupElement[size];
            Arrays.fill(res, element);
            return new BPGroupElementArrayIM(this, res);

        } else {
            return new BPGroupElementArrayF(this, size, element);
        }
    }

    /**
     * Returns all elements in <code>bases</code> to the respective
     * powers in <code>integers</code>.
     *
     * @param bases Bases to be exponentiated.
     * @param integers Powers to be taken.
     * @return All bases to the powers of the given integers.
     */
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final LargeInteger[] integers) {

        if (bases.length != integers.length) {
            throw new ArithmError("Different lengths!");
        }
        final PGroupElement[] res = new PGroupElement[bases.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {

                    for (int i = start; i < end; i++) {
                        res[i] = ((BPGroupElement) bases[i]).exp(integers[i]);
                    }
                }
            };
        worker.work();

        return res;
    }

    @Override
    public PGroupElement expProd(final PGroupElement[] bases,
                                 final PRingElement[] exponents) {

        // Convert exponents to integers and compute the maximal bit
        // length of the exponents.
        final LargeInteger[] integers = new LargeInteger[exponents.length];
        int bitLength = 0;

        for (int i = 0; i < exponents.length; i++) {

            integers[i] = ((PFieldElement) exponents[i]).toLargeInteger();
            bitLength = Math.max(integers[i].bitLength(), bitLength);
        }

        return expProd(bases, integers, bitLength);
    }
}
