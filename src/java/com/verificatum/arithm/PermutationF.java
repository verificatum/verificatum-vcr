
/* Copyright 2008-2019 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.verificatum.arithm;

import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.ByteTreeUtil;

/**
 * Represents an immutable permutation stored on file which allows the
 * permutation and its inverse to be applied to arrays of elements.
 *
 * @author Douglas Wikstrom
 */
public final class PermutationF extends Permutation {

    /**
     * Generates an instance represented by the input.
     *
     * @param table Representation of a permutation.
     */
    protected PermutationF(final LargeIntegerArray table) {
        super(table);
    }

    /**
     * Generates the identity permutation of a given size.
     *
     * @param numberOfElements Number of elements to permute.
     */
    public PermutationF(final int numberOfElements) {
        super(numberOfElements);
    }

    /**
     * Creates a permutation from the input representation.
     *
     * @param size Expected size of the permutation.
     * @param btr Representation of permutation.
     * @throws ArithmFormatException If the input is incorrect or has
     *  wrong size.
     */
    public PermutationF(final int size, final ByteTreeReader btr)
        throws ArithmFormatException {
        super(size, btr);
    }

    /**
     * Generates a random permutation of suitable size using the given
     * source of randomness.
     *
     * @param numberOfElements Number of elements to permute.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param randomSource Source of randomness used to generate the
     * permutation.
     */
    public PermutationF(final int numberOfElements,
                        final RandomSource randomSource,
                        final int statDist) {

        final int aprLog = MathExt.log2c(numberOfElements);

        // The union bound gives this overly conservative bit size.
        final int bitLength = statDist + aprLog + aprLog;

        // Generate array of random positive integers of the given
        // nominal bit size.
        final LargeIntegerArrayF randomIntegers =
            new LargeIntegerArrayF(numberOfElements, bitLength, randomSource);
        final ByteTreeF randomByteTree = new ByteTreeF(randomIntegers.file);

        // Generate array of the integers 0, 1, 2, 3, ...
        final LargeIntegerArrayF consecutive =
            new LargeIntegerArrayF(0, numberOfElements);
        final ByteTreeF consecutiveByteTree = new ByteTreeF(consecutive.file);

        // Zip the two arrays together, sort with respect to the
        // first, and then project to the second to get randomly
        // re-ordered consecutive integers.
       final  ByteTreeF projected =
            ByteTreeUtil.zipSortProject(randomByteTree,
                                        consecutiveByteTree,
                                        new PermutationComparator());
        randomIntegers.free();
        consecutive.free();

        this.table = new LargeIntegerArrayF(numberOfElements, projected.file);
    }

    /**
     * Converts the input to a byte tree on file.
     *
     * @param lia Array of integers to be converted.
     * @return Byte tree on file representing the input array of
     * integers.
     */
    private static ByteTreeF toByteTreeF(final LargeIntegerArray lia) {
        return (ByteTreeF) lia.toByteTree();
    }

    @Override
    public Permutation inv() {
        final LargeIntegerArrayF consecutiveIntegers =
            new LargeIntegerArrayF(0, table.size());
        final ByteTreeF consecutiveByteTree =
            new ByteTreeF(consecutiveIntegers.file);

        final ByteTreeF projected =
            ByteTreeUtil.zipSortProject(toByteTreeF(table),
                                        consecutiveByteTree,
                                        new PermutationComparator());
        consecutiveIntegers.free();

        final LargeIntegerArrayF newTable =
            new LargeIntegerArrayF(table.size(), projected.file);
        return new PermutationF(newTable);
    }

    @Override
    public Permutation shrink(final int size) {
        final LargeInteger outside = new LargeInteger(table.size());

        final LargeIntegerArrayF cut = new LargeIntegerArrayF(0, size);
        final LargeIntegerArrayF mo =
            new LargeIntegerArrayF(table.size() - size, outside);

        final LargeIntegerArrayF hybrid = new LargeIntegerArrayF(cut, mo);
        final ByteTreeF hybridByteTree = new ByteTreeF(hybrid.file);

        cut.free();
        mo.free();

        final ByteTreeF permHyb =
            ByteTreeUtil.zipSortProject(toByteTreeF(table),
                                        hybridByteTree,
                                        new PermutationComparator());

        final LargeIntegerArrayF consecutive =
            new LargeIntegerArrayF(0, table.size());
        final ByteTreeF consecutiveByteTree = new ByteTreeF(consecutive.file);

        final ByteTreeF withJunk =
            ByteTreeUtil.zipSortProject(permHyb,
                                        consecutiveByteTree,
                                        new PermutationComparator());
        permHyb.free();
        consecutive.free();

        final LargeIntegerArrayF withJunkArray =
            new LargeIntegerArrayF(table.size(), withJunk.file);

        final LargeIntegerArray result = withJunkArray.copyOfRange(0, size);
        withJunk.free();

        return new PermutationF(result);
    }

    /**
     * Applies this permutation to the input byte tree viewed as an
     * array of byte trees and outputs the permuted byte trees as a
     * single byte trees, i.e., the children of the first byte tree
     * are permuted.
     *
     * @param input Byte tree representing an array of byte trees.
     * @return Permuted array of byte trees represented as a byte tree.
     */
    public ByteTreeF applyPermutation(final ByteTreeF input) {
        return ByteTreeUtil.zipSortProject(toByteTreeF(table),
                                           input,
                                           new PermutationComparator());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PermutationF)) {
            return false;
        }
        final LargeIntegerArray otherTable = ((PermutationF) obj).table;

        return table.equals(otherTable);
    }
}
