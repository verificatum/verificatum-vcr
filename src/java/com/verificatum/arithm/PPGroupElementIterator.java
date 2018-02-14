
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
 * Iterator over a {@link PPGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public final class PPGroupElementIterator implements PGroupElementIterator {

    /**
     * Underlying group.
     */
    PPGroup pPGroup;

    /**
     * Underlying iterators.
     */
    PGroupElementIterator[] iterators;

    /**
     * Creates an instance over a {@link PPGroupElementArray}.
     *
     * @param pPGroup Underlying group.
     * @param iterators Underlying iterators.
     */
    public PPGroupElementIterator(final PPGroup pPGroup,
                                  final PGroupElementIterator[] iterators) {
        this.pPGroup = pPGroup;
        this.iterators = Arrays.copyOf(iterators, iterators.length);
    }

    // Documented in PGroupElementIterator.java

    @Override
    public PGroupElement next() {
        final PGroupElement[] res = new PGroupElement[iterators.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = iterators[i].next();
        }
        if (res[0] == null) {
            return null;
        } else {
            return pPGroup.product(res);
        }
    }

    @Override
    public boolean hasNext() {
        boolean res = true;
        for (int i = 0; i < iterators.length; i++) {
            if (!iterators[i].hasNext()) {
                res = false;
            }
        }
        return res;
    }

    @Override
    public void close() {
        for (int i = 0; i < iterators.length; i++) {
            iterators[i].close();
        }
    }
}
