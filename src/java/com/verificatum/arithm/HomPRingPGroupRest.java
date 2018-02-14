
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

import com.verificatum.eio.ByteTreeBasic;

/**
 * Restriction of a bilinear map to a homomorphism.
 *
 * @author Douglas Wikstrom
 */
public final class HomPRingPGroupRest implements HomPRingPGroup {

    /**
     * Underlying bilinear map.
     */
    final BiPRingPGroup bi;

    /**
     * Restriction inducing a homomorphism.
     */
    final PGroupElement restriction;

    /**
     * Creates the homomorphism from the given restriction.
     *
     * @param bi Underlying bilinear map.
     * @param restriction Restriction of the bilinear map.
     */
    public HomPRingPGroupRest(final BiPRingPGroup bi,
                              final PGroupElement restriction) {
        this.bi = bi;
        this.restriction = restriction;
    }

    // Documented in HomPRingPGroup.java

    @Override
    public PRing getDomain() {
        return bi.getPRingDomain();
    }

    @Override
    public PGroup getRange() {
        return bi.getRange();
    }

    @Override
    public PGroupElement map(final PRingElement element) {
        return bi.map(element, restriction);
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return restriction.toByteTree();
    }
}
