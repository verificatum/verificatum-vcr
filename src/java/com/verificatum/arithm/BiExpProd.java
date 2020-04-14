
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
 * Bilinear map capturing exponentiated products. The multiple group
 * elements and exponents are represented by {@link PPGroupElement}
 * and {@link PPRingElement} instances that must contain the same
 * number of underlying elements and with matching groups and rings.
 *
 * @author Douglas Wikstrom
 */
public final class BiExpProd extends BiPRingPGroup {

    /**
     * Underlying group.
     */
    private final PGroup pGroup;

    /**
     * Group domain of map.
     */
    private final PGroup pGroupDomain;

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     * @param width Number of bases in exponentiated product.
     */
    public BiExpProd(final PGroup pGroup, final int width) {
        this.pGroup = pGroup;
        this.pGroupDomain = new PPGroup(pGroup, width);
    }

    // Documented in BiPRingPGroup.java

    @Override
    public PRing getPRingDomain() {
        return pGroupDomain.getPRing();
    }

    @Override
    public PGroup getPGroupDomain() {
        return pGroupDomain;
    }

    @Override
    public PGroup getRange() {
        return pGroup;
    }

    @Override
    public PGroupElement map(final PRingElement ringElement,
                             final PGroupElement groupElement) {

        if (!groupElement.getPGroup().equals(pGroupDomain)
            || !ringElement.getPRing().equals(pGroupDomain.getPRing())) {

            throw new ArithmError("Inputs not in domains!");
        }

        final PRingElement[] ringFactors =
            ((PPRingElement) ringElement).getFactors();
        final PGroupElement[] groupFactors =
            ((PPGroupElement) groupElement).getFactors();

        PGroupElement res = pGroup.getONE();
        for (int i = 0; i < groupFactors.length; i++) {
            res = res.mul(groupFactors[i].exp(ringFactors[i]));
        }
        return res;
    }
}
