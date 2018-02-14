
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

package com.verificatum.eio;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator of queues of byte trees. Note that this comparator
 * compares the first elements in two queues of byte trees. Note that
 * this comparator imposes orderings that are inconsistent with
 * equals.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeQueueComparator
    implements Comparator<ByteTreeQueue>, Serializable {

    /**
     * Underlying comparator of byte trees.
     */
    private final ByteTreeComparator comparator;

    /**
     * Underlying comparator of byte trees.
     *
     * @param comparator Comparator of byte trees.
     */
    public ByteTreeQueueComparator(final ByteTreeComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(final ByteTreeQueue left, final ByteTreeQueue right) {
        return comparator.compare(left.peekByteTree(), right.peekByteTree());
    }
}
