
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
