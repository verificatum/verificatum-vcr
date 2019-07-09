
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

/**
 * Implementation of simultaneous exponentiation. A good reference for
 * this technique is Menezes et al., Handbook of Cryptography.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerSimModPowTab {

    /**
     * Width of table of pre-computed values.
     */
    final int width;

    /**
     * Table of pre-computed values.
     */
    final LargeInteger[] pre;

    /**
     * Modulus.
     */
    final LargeInteger modulus;

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

        } while (cost < oldCost);

        return Math.max(1, width - 1);
    }

    /**
     * Creates a pre-computed table.
     *
     * @param bases Bases used for pre-computation.
     * @param offset Position of first basis element to use.
     * @param width Number of bases elements to use.
     * @param modulus Underlying modulus.
     */
    public LargeIntegerSimModPowTab(final LargeInteger[] bases,
                                    final int offset,
                                    final int width,
                                    final LargeInteger modulus) {
        this.width = width;
        this.modulus = modulus;

        // Make room for table.
        pre = new LargeInteger[1 << width];

        // Precalculation Start
        Arrays.fill(pre, LargeInteger.ONE);

        // Init precalc with bases provided.
        for (int i = 1, j = offset; i < pre.length; i = i * 2, j++) {
            pre[i] = bases[j];
        }

        // Perform precalculation using masking for efficiency.
        for (int mask = 0; mask < pre.length; mask++) {
            final int onemask = mask & (-mask);
            pre[mask] = pre[mask ^ onemask].mul(pre[onemask]).mod(modulus);
        }
    }

    /**
     * Compute a power-product using the given integer exponents.
     *
     * @param integers Integer exponents.
     * @param offset Position of first exponent to use.
     * @param bitLength Expected bit length of exponents.
     * @return Power product of the generators used during
     * pre-computation to the given exponents.
     */
    public LargeInteger modPowProd(final LargeInteger[] integers,
                                   final int offset,
                                   final int bitLength) {

        // Loop over bits in integers starting at bitLength - 1.
        LargeInteger res = LargeInteger.ONE;

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
            res = res.mul(res).mod(modulus);

            // Multiply.
            res = res.mul(pre[k]).mod(modulus);
        }
        return res;
    }
}
