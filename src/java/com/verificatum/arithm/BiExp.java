
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
 * Bilinear map capturing exponentiation.
 *
 * @author Douglas Wikstrom
 */
public final class BiExp extends BiPRingPGroup {

    /**
     * Underlying group.
     */
    private final PGroup pGroup;

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     */
    public BiExp(final PGroup pGroup) {
        this.pGroup = pGroup;
    }

    // Documented in BiPRingPGroup.java

    @Override
    public PRing getPRingDomain() {
        return pGroup.getPRing();
    }

    @Override
    public PGroup getPGroupDomain() {
        return pGroup;
    }

    @Override
    public PGroup getRange() {
        return pGroup;
    }

    @Override
    public PGroupElement map(final PRingElement ringElement,
                             final PGroupElement groupElement) {
        if (ringElement.pRing.equals(pGroup.pRing)
            && groupElement.pGroup.equals(pGroup)) {

            return groupElement.exp(ringElement);
        } else {
            throw new ArithmError("Input not contained in domain!");
        }
    }
}
