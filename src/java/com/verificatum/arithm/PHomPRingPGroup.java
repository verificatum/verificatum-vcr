
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

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;


/**
 * Product homomorphism.
 *
 * @author Douglas Wikstrom
 */
public class PHomPRingPGroup implements HomPRingPGroup {

    /**
     * Product of the underlying domains.
     */
    protected PPRing domain;

    /**
     * Product of the underlying ranges.
     */
    protected PPGroup range;

    /**
     * Underlying homomorphisms.
     */
    HomPRingPGroup[] homs;

    /**
     * Creates the product homomorphism of the input homomorphisms.
     *
     * @param homs Underlying homomorphisms.
     */
    public PHomPRingPGroup(final HomPRingPGroup... homs) {
        this.homs = homs;

        final PRing[] pRings = new PRing[homs.length];
        for (int i = 0; i < pRings.length; i++) {
            pRings[i] = homs[i].getDomain();
        }
        this.domain = new PPRing(pRings);

        final PGroup[] pGroups = new PGroup[homs.length];
        for (int i = 0; i < pGroups.length; i++) {
            pGroups[i] = homs[i].getRange();
        }
        this.range = new PPGroup(pGroups);
    }

    /**
     * Returns an array of the underlying arrays.
     *
     * @return Array of underlying homomorphisms.
     */
    public HomPRingPGroup[] getFactors() {
        return Arrays.copyOfRange(homs, 0, homs.length);
    }

    // Documented in HomPRingPGroup.java

    @Override
    public PRing getDomain() {
        return domain;
    }

    @Override
    public PGroup getRange() {
        return range;
    }

    @Override
    public PGroupElement map(final PRingElement element) {

        if (domain.contains(element)) {

            final PRingElement[] elements =
                ((PPRingElement) element).getFactors();

            final PGroupElement[] res = new PGroupElement[elements.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = homs[i].map(elements[i]);
            }
            return range.product(res);
        }
        throw new ArithmError("Element not in domain!");
    }

    @Override
    public ByteTreeBasic toByteTree() {
        final ByteTreeBasic[] bts = new ByteTreeBasic[homs.length];

        for (int i = 0; i < bts.length; i++) {
            bts[i] = homs[i].toByteTree();
        }

        return new ByteTreeContainer(homs);
    }
}
