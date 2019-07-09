
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

/**
 * Implementation of fixed base exponentiation. A good reference for
 * this technique is Menezes et al., Handbook of Cryptography.
 *
 * @author Douglas Wikstrom
 */
public class PGroupFixExpTab {

    /**
     * Width of table of pre-computed values.
     */
    protected PGroupSimExpTab tab;

    /**
     * Bit length of each slice of an exponent.
     */
    protected int sliceSize;

    /**
     * Theoretically optimal width of pre-computed table.
     *
     * @param bitLength Bit length of exponents used to compute
     * power-products.
     * @param size Number of exponentiations that will be computed.
     * @return Theoretical optimal width.
     */
    public static int optimalWidth(final int bitLength, final int size) {

        int width = 2;
        double cost = 1.5 * bitLength;
        double oldCost;
        do {

            oldCost = cost;

            // Amortized cost for table.
            final double t =
                ((double) ((1 << width) - width + bitLength)) / size;

            // Cost for multiplication.
            final double m = ((double) bitLength) / width;

            cost = t + m;

            width++;

        } while (width < 17 && cost < oldCost);

        // We reduce the theoretical value by one to account for the
        // overhead.
        return width - 1;
    }

    /**
     * Creates a pre-computed table.
     *
     * @param basis Fixed basis used for pre-computation.
     * @param bitLength Bit length of exponents used to compute
     * power-products.
     * @param width Number of bases elements to use.
     */
    public PGroupFixExpTab(final PGroupElement basis,
                           final int bitLength,
                           final int width) {

        final PGroup pGroup = basis.getPGroup();
        pGroup.getONE();

        // Determine the number of bits associated with each bases.
        sliceSize = (bitLength + width - 1) / width;

        // Create radix element.
        final PField pField = pGroup.getPRing().getPField();
        final PFieldElement b =
            pField.toElement(LargeInteger.ONE.shiftLeft(sliceSize));

        // Create generators.
        final PGroupElement[] bases = new PGroupElement[width];
        bases[0] = basis;
        for (int i = 1; i < bases.length; i++) {
            bases[i] = bases[i - 1].exp(b);
        }

        // Invoke the pre-computation of the simultaneous
        // exponentiation code.
        tab = new PGroupSimExpTab(bases, 0, width);
    }

    /**
     * Cuts an integer into the appropriate number of slices.
     *
     * @param exponent Exponent to be slized.
     * @return Slices of exponent.
     */
    protected int[] slice(final LargeInteger exponent) {

        final int[] res = new int[sliceSize];

        for (int i = 0; i < sliceSize; i++) {

            res[i] = 0;

            for (int j = tab.width - 1; j >= 0; j--) {

                res[i] <<= 1;

                if (exponent.testBit(j * sliceSize + i)) {
                    res[i] |=  1;
                }
            }
        }

        return res;
    }

    /**
     * Compute power using the given integer.
     *
     * @param integer Integer exponent.
     * @return Power of element used for pre-computing.
     */
    public PGroupElement exp(final LargeInteger integer) {

        final int[] sliced = slice(integer);

        PGroupElement res = tab.pre[0].getPGroup().getONE();
        for (int i = sliced.length - 1; i >= 0; i--) {
            res = res.mul(res);
            res = res.mul(tab.pre[sliced[i]]);
        }
        return res;
    }
}
