
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
 * Interface for an iterator over a {@link PGroupElementArray}. All
 * elements must be traversed to let the underlying implementations
 * deallocate resources.
 *
 * @author Douglas Wikstrom
 */
public interface PGroupElementIterator {

    /**
     * Returns the next group element, or null if no more elements are
     * available.
     *
     * @return Next group element.
     */
    PGroupElement next();

    /**
     * Returns true if and only if there is another element in the
     * iterator.
     *
     * @return True or false depending on if there is another element
     * in the iterator.
     */
    boolean hasNext();

    /**
     * Closes this iterator and any potential underlying sources of
     * elements.
     */
    void close();
}
