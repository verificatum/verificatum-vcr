
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

/**
 * Mathematical utility functions for primitive types.
 *
 * @author Douglas Wikstrom
 */
public final class MathExt {

    /**
     * Bit-mask.
     */
    public static final int MASK = 0x80000000;

    /**
     * Maximal bit length of the input.
     */
    public static final int MAX_BITLENGTH = 32;

    /**
     * This prevents instantiation.
     */
    private MathExt() { }

    /**
     * Returns the least upper bound of the binary logarithm of the
     * input, which is assumed to be positive. The output is undefined
     * otherwise.
     *
     * @param x Integer of which the logarithmic upper bound is
     * requested.
     * @return Least upper bound on the binary logarithm of the input.
     */
    public static int log2c(final int x) {
        int mask = MASK;
        int res = MAX_BITLENGTH;
        while ((x & mask) == 0 && res >= 1) {
            mask >>>= 1;
            res--;
        }
        return res;
    }
}
