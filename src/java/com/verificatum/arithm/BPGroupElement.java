
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

import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.ByteTreeWriterF;
import com.verificatum.util.ArrayWorker;

/**
 * Abstract class representing an immutable group element in a prime
 * order group. The group is represented by the class {@link BPGroup}.
 *
 * @author Douglas Wikstrom
 */
public abstract class BPGroupElement extends PGroupElement {

    /**
     * Initializes this instance.
     *
     * @param pGroup Group to which this instance will belong.
     */
    protected BPGroupElement(final PGroup pGroup) {
        super(pGroup);
    }

    /**
     * Using {@link BPGroup#unsafeToElement(ByteTreeReader)} elements
     * can be instantiated that are not verified completely. This is
     * needed to thread, e.g., reading large amounts of data from
     * file. This method must complete verification, i.e.,
     * <code>unsafeToElement(ByteTreeReader).verifyUnsafe()</code> must be
     * equivalent to <code>toElement(ByteTreeReader)</code>.
     *
     * @throws ArithmFormatException If the instantiated element is
     * not valid.
     */
    protected abstract void verifyUnsafe() throws ArithmFormatException;

    /**
     * Returns this element to the power of the input.
     *
     * @param exponent Power to which we take this element.
     * @return This element to the power of the input.
     */
    public abstract PGroupElement exp(LargeInteger exponent);

    // Documented in PGroupElement.java

    @Override
    public PGroupElement exp(final PRingElement exponent) {
        if (pGroup.getPRing().equals(exponent.getPRing())) {
            return exp(((PFieldElement) exponent).toLargeInteger());
        } else {
            throw new ArithmError(PGroup.MISMATCHING_GROUP_RING);
        }
    }

    /**
     * Returns this instance to the given powers.
     *
     * @param integers Powers to be taken.
     * @param bitLength Maximal bitlength of exponents.
     * @return Basis to the powers of the given exponents.
     */
    public PGroupElement[] exp(final LargeInteger[] integers,
                               final int bitLength) {

        // Optimal width for the given bit length and number of
        // exponents.
        final int width =
            PGroupFixExpTab.optimalWidth(bitLength, integers.length);

        final PGroupFixExpTab tab = new PGroupFixExpTab(this, bitLength, width);

        // Compute result.
        final PGroupElement[] res = new PGroupElement[integers.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > pGroup.expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {

                        res[i] = tab.exp(integers[i]);
                    }
                }
            };
        worker.work();

        return res;
    }

    @Override
    public PGroupElementArray exp(final PRingElementArray exponentsArray) {
        if (!pGroup.getPRing().equals(exponentsArray.getPRing())) {
            throw new ArithmError(PGroup.MISMATCHING_GROUP_RING);
        }
        if (LargeIntegerArray.inMemory) {

            final LargeIntegerArray integerArray =
                ((PFieldElementArray) exponentsArray).values;

            final LargeInteger[] integers = integerArray.integers();

            int bitLength = 0;
            for (int i = 0; i < integers.length; i++) {
                bitLength = Math.max(bitLength, integers[i].bitLength());
            }

            return new BPGroupElementArrayIM(pGroup, exp(integers, bitLength));

        } else {

            final BPGroupElementArrayF res =
                new BPGroupElementArrayF(pGroup, exponentsArray.size());
            final ByteTreeWriterF btw = res.getWriter();

            final LargeIntegerArrayF integersArray =
                (LargeIntegerArrayF)
                ((PFieldElementArray) exponentsArray).values;
            final ByteTreeReader btr = integersArray.getReader();

            final LargeIntegerBatchReader br = new LargeIntegerBatchReader(btr);
            final BPGroupElementBatchWriter bw =
                new BPGroupElementBatchWriter(btw);

            LargeInteger[] integers = br.readNext();
            while (integers != null) {

                int bitLength = 0;
                for (int i = 0; i < integers.length; i++) {
                    bitLength = Math.max(bitLength, integers[i].bitLength());
                }

                bw.writeNext(exp(integers, bitLength));
                integers = br.readNext();
            }

            bw.close();
            br.close();

            return res;
        }
    }

    // Implemented in terms of the above.

    /**
     * Returns this instance to the powers of the elements in
     * <code>exponents</code>.
     *
     * @param exponents Powers to be taken.
     * @return Basis to the powers of the given exponents.
     */
    @Override
    public PGroupElement[] exp(final PRingElement[] exponents) {

        // Extract integers and determine the maximal bit length.
        int bitLength = 0;
        final LargeInteger[] integers = new LargeInteger[exponents.length];
        for (int i = 0; i < exponents.length; i++) {
            integers[i] = ((PFieldElement) exponents[i]).toLargeInteger();
            bitLength = Math.max(bitLength, integers[i].bitLength());
        }

        return exp(integers, bitLength);
    }
}
