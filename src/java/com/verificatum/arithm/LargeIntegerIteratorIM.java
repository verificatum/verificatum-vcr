
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
 * Interface for an iterator over a {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerIteratorIM implements LargeIntegerIterator {

    /**
     * Current index.
     */
    private int current;

    /**
     * Underlying array.
     */
    final LargeIntegerArrayIM array;

    /**
     * Creates an iterator reading from the given array.
     *
     * @param array Underlying array.
     */
    public LargeIntegerIteratorIM(final LargeIntegerArrayIM array) {
        this.array = array;
        this.current = 0;
    }

    // Documented in LargeIntegerIterator.java

    @Override
    public LargeInteger next() {
        if (current < array.li.length) {
            return array.li[current++];
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return current < array.li.length;
    }

    /**
     * This method is needed to give a uniform interface for both
     * memory mapped and file mapped iterators. The latter must be
     * closed manually.
     */
    @Override
    public void close() {
    }
}
