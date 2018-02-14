
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

/**
 * Implementation of simultaneous exponentiation. A good reference for
 * this technique is Menezes et al., Handbook of Cryptography.
 *
 * @author Douglas Wikstrom
 */
public class PGroupSimExpTab {

    /**
     * Width of table of pre-computed values.
     */
    protected int width;

    /**
     * Table of pre-computed values.
     */
    protected PGroupElement[] pre;

    /**
     * Theoretically optimal width of pre-computed table.
     *
     * @param bitLength Bit length of exponents used to compute
     * power-products.
     * @return Theoretical optimal width.
     */
    public static int optimalWidth(final int bitLength) {

        // This computes the theoretical optimum.
        int width = 1;
        double cost = 1.5 * bitLength;
        double oldCost;
        do {

            oldCost = cost;

            width++;
            final int widthExp = 1 << width;
            cost =
                ((double) (widthExp + (2 - 1 / widthExp) * bitLength)) / width;

        } while (width < 31 && cost < oldCost);

        return Math.max(1, width - 1);
    }

    /**
     * Creates a pre-computed table.
     *
     * @param bases Bases used for pre-computation.
     * @param offset Position of first basis element to use.
     * @param width Number of bases elements to use.
     */
    public PGroupSimExpTab(final PGroupElement[] bases,
                           final int offset,
                           final int width) {

        this.width = width;

        // Make room for table.
        pre = new PGroupElement[1 << width];

        // Precalculation Start
        Arrays.fill(pre, bases[0].getPGroup().getONE());

        // Init precalc with bases provided.
        for (int i = 1, j = offset; i < pre.length; i = i * 2, j++) {
            pre[i] = bases[j];
        }

        // Perform precalculation using masking for efficiency.
        for (int mask = 0; mask < pre.length; mask++) {
            final int onemask = mask & (-mask);
            pre[mask] = pre[mask ^ onemask].mul(pre[onemask]);
        }
    }

    /**
     * Compute a power-product using the given integer exponents.
     *
     * @param integers Integer exponents.
     * @param offset Position of first exponent to use.
     * @param bitLength Maximal bit length.
     * @return Power product of elements used for pre-computation.
     */
    public PGroupElement expProd(final LargeInteger[] integers,
                                 final int offset,
                                 final int bitLength) {

        // Loop over bits in integers starting at bitLength - 1.
        PGroupElement res = pre[0].getPGroup().getONE();

        for (int i = bitLength - 1; i >= 0; i--) {

            int k = 0;

            // Loop over integers to form a word from all the bits at
            // a given position.
            for (int j = offset; j < offset + width; j++) {

                if (integers[j].testBit(i)) {

                    k |= 1 << (j - offset);
                }
            }

            // Square.
            res = res.mul(res);

            // Multiply.
            res = res.mul(pre[k]);
        }
        return res;
    }
}
