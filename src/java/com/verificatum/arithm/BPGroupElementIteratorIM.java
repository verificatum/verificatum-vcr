
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
 * Iterator over a {@link BPGroupElementArrayIM}.
 *
 * @author Douglas Wikstrom
 */
public class BPGroupElementIteratorIM implements PGroupElementIterator {

    /**
     * Underlying array.
     */
    protected BPGroupElementArrayIM array;

    /**
     * Index of current element.
     */
    protected int current;

    /**
     * Creates an instance over a {@link BPGroupElementArrayIM}.
     *
     * @param array Underlying array.
     */
    public BPGroupElementIteratorIM(final BPGroupElementArrayIM array) {
        this.array = array;
        this.current = 0;
    }

    // Documented in PGroupElementIterator.java

    @Override
    public PGroupElement next() {
        if (current < array.size()) {
            return this.array.values[current++];
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return current < array.size();
    }

    @Override
    public void close() {
    }
}
