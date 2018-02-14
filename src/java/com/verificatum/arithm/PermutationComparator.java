
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

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeComparator;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOError;
import com.verificatum.eio.EIOException;

/**
 * Comparator used to implement permutations using sorting. It
 * compares byte trees having exactly two children of which the first
 * is a leaf storing an array of bytes. The arrays of bytes are
 * interpreted as positive integers and compared. Note that this
 * comparator imposes orderings that are inconsistent with equals. Two
 * such "integers" are first compared with respect to length, i.e.,
 * shorter means smaller, and then content.
 *
 * @author Douglas Wikstrom
 */
public final class PermutationComparator implements ByteTreeComparator {

    /**
     * Validate that the width is two.
     *
     * @param width Width of byte tree.
     */
    private void validatePair(final int width) {
        if (width != 2) {
            throw new EIOError("Byte tree does not have 2 children!");
        }
    }

    /**
     * Validate that the byte tree reader points to a leaf.
     *
     * @param btr Source of byte tree.
     */
    private void validateLeaf(final ByteTreeReader btr) {
        if (!btr.isLeaf()) {
            throw new EIOError("Not a leaf!");
        }
    }

    @Override
    public int compare(final ByteTree leftByteTree,
                       final ByteTree rightByteTree) {
        try {

            // Extract left index from left byte tree.
            final ByteTreeReader lbtr = leftByteTree.getByteTreeReader();
            validatePair(lbtr.getRemaining());

            final ByteTreeReader lir = lbtr.getNextChild();
            validateLeaf(lir);
            final byte[] left = lir.read();


            // Extract right index from right byte tree.
            final ByteTreeReader rbtr = rightByteTree.getByteTreeReader();
            validatePair(rbtr.getRemaining());

            final ByteTreeReader rir = rbtr.getNextChild();
            validateLeaf(rir);
            final byte[] right = rir.read();


            // Indexes are binary representations of integer indexes,
            // so if one is shorter, then it represents a smaller
            // index.
            if (left.length < right.length) {
                return -1;
            } else if (left.length > right.length) {
                return 1;
            }

            // Integer comparison of indexes of the same byte length.
            for (int i = 0; i < left.length; i++) {

                final int li = (int) left[i] & 0xFF;
                final int ri = (int) right[i] & 0xFF;

                if (li < ri) {
                    return -1;
                } else if (li > ri) {
                    return 1;
                }
            }
            return 0;

        } catch (final EIOException eioe) {
            throw new EIOError("Fatal comparison!", eioe);
        }
    }
}
