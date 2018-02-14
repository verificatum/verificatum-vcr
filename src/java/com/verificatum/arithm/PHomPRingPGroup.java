
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
