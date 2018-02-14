
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
public final class HomPGroupPGroupRest implements HomPGroupPGroup {

    /**
     * Underlying bilinear map.
     */
    final BiPRingPGroup bi;

    /**
     * Restriction inducing a homomorphism.
     */
    final PRingElement restriction;

    /**
     * Creates the homomorphism from the given restriction.
     *
     * @param bi Underlying bilinear map.
     * @param restriction Restriction of the bilinear map.
     */
    public HomPGroupPGroupRest(final BiPRingPGroup bi,
                               final PRingElement restriction) {
        this.bi = bi;
        this.restriction = restriction;
    }

    // Documented in HomPGroupPGroup.java

    @Override
    public PGroup getDomain() {
        return bi.getPGroupDomain();
    }

    @Override
    public PGroup getRange() {
        return bi.getRange();
    }

    @Override
    public PGroupElement map(final PGroupElement element) {
        return bi.map(restriction, element);
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return restriction.toByteTree();
    }
}
