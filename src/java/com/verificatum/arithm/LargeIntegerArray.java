
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

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.util.Pair;

/**
 * Implements an array of integers. Depending on if the method {@link
 * #useFileBased()} is called or not, this class represents its data
 * in memory using native arrays or on file. It is the responsibility
 * of the programmer to call {@link #useFileBased()} at most once
 * before any instances are created.
 *
 * @author Douglas Wikstrom
 */
public abstract class LargeIntegerArray implements ByteTreeConvertible {

    /**
     * Error message for arrays of different lengths.
     */
    public static final String DIFFERENT_LENGTHS = "Different lengths!";

    /**
     * Default number of integers processed in each batch.
     */
    public static final int DEFAULT_BATCH_SIZE = 1000000;

    /**
     * Determines if arrays are stored in memory or on file.
     */
    static boolean inMemory = true;

    /**
     * Number of elements processed in each batch.
     */
    static int batchSize;

    /**
     * Returns the number of elements processed in each batch.
     *
     * @return Number of elements processed in each batch.
     */
    @CoberturaIgnore
    public static int getBatchSize() {
        return batchSize;
    }

    /**
     * Initializes the module to use file based representations. This
     * should normally used only once.
     *
     * @param theBatchSize Batch size used when processing arrays.
     */
    @CoberturaIgnore // No observable change.
    public static void useFileBased(final int theBatchSize) {
        inMemory = false;
        batchSize = theBatchSize;
    }

    /**
     * Initializes the module to use file based representations. This
     * should only be used once.
     */
    @CoberturaIgnore // No observable change.
    public static void useFileBased() {
        useFileBased(DEFAULT_BATCH_SIZE);
    }

    /**
     * Initialize the module to use memory based representations.
     */
    @CoberturaIgnore // No observable change.
    public static void useMemoryBased() {
        inMemory = true;
    }

    /**
     * Get status for memory based representations.
     *
     * @return True or false depending on if memory based storage is
     * used or not.
     */
    @CoberturaIgnore // No observable change.
    public static boolean getMemoryBased() {
        return inMemory;
    }

    /**
     * Returns an instance containing the integers in the input.
     *
     * @param integers Integers to be stored in this instance.
     * @return Instance containing the given integers.
     */
    public static LargeIntegerArray
        toLargeIntegerArray(final LargeInteger[] integers) {
        if (inMemory) {
            return new LargeIntegerArrayIM(integers);
        } else {
            return new LargeIntegerArrayF(integers);
        }
    }

    /**
     * Returns an instance containing the integers of all the input
     * arrays in the given order.
     *
     * @param arrays Arrays of integers to be concatenated.
     * @return Concatenation of the input arrays.
     */
    public static LargeIntegerArray
        toLargeIntegerArray(final LargeIntegerArray... arrays) {

        if (inMemory) {
            return new LargeIntegerArrayIM(arrays);
        } else {
            return new LargeIntegerArrayF(arrays);
        }
    }

    /**
     * Returns an instance containing the integers in the input. This
     * requires that each integer falls into the given interval, but
     * also that the representation of each integer is of equal size
     * to the byte tree representation of the upper bound.
     *
     * @param size Expected number of elements in array. If the
     * expected size (number of elements) is set to zero, then the
     * input can have any size.
     * @param btr Representation of an instance.
     * @param lb Non-negative inclusive lower bound for integers.
     * @param ub Positive exclusive upper bound for integers.
     * @return Instance containing the integers read from the
     *         iterator.
     *
     * @throws ArithmFormatException If the data on file can not be
     *  parsed as a large integer array.
     */
    public static LargeIntegerArray
        toLargeIntegerArray(final int size,
                            final ByteTreeReader btr,
                            final LargeInteger lb,
                            final LargeInteger ub)
        throws ArithmFormatException {
        try {
            if (inMemory) {
                return new LargeIntegerArrayIM(size, btr, lb, ub);
            } else {
                return new LargeIntegerArrayF(size, btr, lb, ub);
            }
        } catch (final EIOException eioe) {
            throw new ArithmFormatException("Malformed input!", eioe);
        }
    }

    /**
     * Generates an array of random integers modulo the given modulus.
     *
     * @param size Number of integers to generate.
     * @param modulus Modulus.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param randomSource Source of random bits used to initialize
     * the array.
     * @return Array of random integers.
     */
    public static LargeIntegerArray random(final int size,
                                           final LargeInteger modulus,
                                           final int statDist,
                                           final RandomSource randomSource) {
        if (inMemory) {
            return new LargeIntegerArrayIM(size, modulus, statDist,
                                           randomSource);
        } else {

            final int bitLength = modulus.bitLength() + statDist;

            final LargeIntegerArray lia =
                new LargeIntegerArrayF(size, bitLength, randomSource);
            final LargeIntegerArray res = lia.mod(modulus);
            lia.free();

            return res;
        }
    }

    /**
     * Returns an instance containing the given number random integers
     * of the given bit length.
     *
     * @param size Number of integers to generate.
     * @param bitLength Bit length of random integers.
     * @param randomSource Source of randomness.
     * @return Instance containing the random integers.
     */
    public static LargeIntegerArray random(final int size, final int bitLength,
                                           final RandomSource randomSource) {
        if (inMemory) {
            return new LargeIntegerArrayIM(size, bitLength, randomSource);
        } else {
            return new LargeIntegerArrayF(size, bitLength, randomSource);
        }
    }

    /**
     * Returns an instance containing the a number of copies of the
     * given integer value.
     *
     * @param size Number of copies of the integers.
     * @param value Integer value to be copied.
     * @return Instance containing the integers.
     */
    public static LargeIntegerArray fill(final int size,
                                         final LargeInteger value) {
        if (inMemory) {
            return new LargeIntegerArrayIM(size, value);
        } else {
            return new LargeIntegerArrayF(size, value);
        }
    }


    /**
     * Constructs an array of consecutive integers.
     *
     * @param begin Inclusive starting integer in sequence.
     * @param end Exclusive ending integer in sequence.
     * @return Array of consecutive integers.
     */
    public static LargeIntegerArray consecutive(final int begin,
                                                final int end) {
        if (inMemory) {
            return new LargeIntegerArrayIM(begin, end);
        } else {
            return new LargeIntegerArrayF(begin, end);
        }
    }


    /**
     * Returns an iterator over the integers of this array.
     *
     * @return Iterator over the integers of this array.
     */
    public abstract LargeIntegerIterator getIterator();

    /**
     * Returns the element-wise modular inverse of this instance
     * modulo the given modulus.
     *
     * @param modulus Modulus.
     * @return Element-wise modular inverse of this instance.
     * @throws ArithmException If this integer is not invertible.
     */
    public abstract LargeIntegerArray modInv(LargeInteger modulus)
        throws ArithmException;

    /**
     * Returns a copy of the integers from the given starting index
     * (inclusive) to the given ending index (exclusive). Assumes that
     * startIndex is greater than endIndex.
     *
     * @param startIndex Starting index of range.
     * @param endIndex Ending index of range.
     * @return Copy of range of elements.
     */
    public abstract LargeIntegerArray copyOfRange(int startIndex, int endIndex);

    /**
     * Returns a primitive array of integers corresponding to this
     * instance.
     *
     * @return Array of integers in this instance.
     */
    public abstract LargeInteger[] integers();

    /**
     * Returns true or false depending on if this instance and the
     * input represent the same array of integers or not.
     *
     * @param obj Array of integers.
     * @return true or false depending on if this instance and the
     *         input represent the same array of integers or not.
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Returns an array containing the elements for which the
     * corresponding component in the input is true.
     *
     * @param valid Array identifying which elements to extract.
     * @return Array containing the chosen integers.
     */
    public abstract LargeIntegerArray extract(boolean[] valid);

    /**
     * Permute the integers in this instance using the permutation
     * given as input.
     *
     * @param permutation Permutation used.
     * @return Permuted list of integers.
     */
    public abstract LargeIntegerArray permute(Permutation permutation);

    /**
     * Computes the element-wise modular sum of this instance and the
     * input.
     *
     * @param termsArray Array of integers.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public abstract LargeIntegerArray modAdd(LargeIntegerArray termsArray,
                                             LargeInteger modulus);

    /**
     * Computes the element-wise modular negative of this instance.
     *
     * @param modulus Modulus.
     * @return Array of modular negatives.
     */
    public abstract LargeIntegerArray modNeg(LargeInteger modulus);

    /**
     * Computes the element-wise modular product of this instance and
     * the input.
     *
     * @param factorsArray Array of integers.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public abstract LargeIntegerArray modMul(LargeIntegerArray factorsArray,
                                             LargeInteger modulus);

    /**
     * Computes the element-wise modular product of this instance and
     * the input.
     *
     * @param scalar Scalar integer.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public abstract LargeIntegerArray modMul(LargeInteger scalar,
                                             LargeInteger modulus);

    /**
     * Computes the element-wise modular power of the elements in this
     * instance using the exponents in the input.
     *
     * @param exponentsArray Array of exponents.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public abstract LargeIntegerArray modPow(LargeIntegerArray exponentsArray,
                                             LargeInteger modulus);

    /**
     * Compute the element-wise modular power of the elements in this
     * instance using the exponent in the input.
     *
     * @param exponent Exponent.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public abstract LargeIntegerArray modPow(LargeInteger exponent,
                                             LargeInteger modulus);

    /**
     * Reduces each integer in this instance by the given modulus.
     *
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public final LargeIntegerArray mod(final LargeInteger modulus) {
        return modPow(LargeInteger.ONE, modulus);
    }

    /**
     * Compute the element-wise modular power of the given basis to
     * the elements in this instance.
     *
     * @param basis Basis.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public abstract LargeIntegerArray modPowVariant(LargeInteger basis,
                                                    LargeInteger modulus);

    /**
     * Computes the modular product of all elements in this instance
     * to the corresponding powers in the input array.
     *
     * @param exponentsArray Array of exponents.
     * @param modulus Modulus.
     * @return Modular product of all elements in this instance to the
     *         corresponding powers in the input array.
     */
    public abstract LargeInteger modPowProd(LargeIntegerArray exponentsArray,
                                            LargeInteger modulus);

    /**
     * Computes the partial modular products of all elements in this
     * instance.
     *
     * @param modulus Modulus.
     * @return Array of partial products of all elements in this
     *         instance.
     */
    public abstract LargeIntegerArray modProds(LargeInteger modulus);

    /**
     * Returns the element at the given position in this instance.
     * Note that this may have linear running time in the size of the
     * index.
     *
     * @param index Index of integer.
     * @return Integer at the given index.
     */
    public abstract LargeInteger get(int index);

    /**
     * Shifts all integers one step to the right (deleting the last
     * integer) and sets the given integer as the first integer of the
     * array.
     *
     * @param integer First integer in resulting array.
     * @return Resulting array.
     */
    public abstract LargeIntegerArray shiftPush(LargeInteger integer);

    /**
     * Computes a linear recursive function. The current output is set
     * to zero. Then each produced actual output is computed by taking
     * the product of the previous output and the corresponding input
     * integer plus the corresponding element of this instance. Note
     * that this means that the first integer in the output equals the
     * first element of this instance.
     *
     * @param array Array of integers.
     * @param modulus Modulus.
     * @return Pair of the resulting array and the last element of the
     *         arrray.
     */
    public abstract Pair<LargeIntegerArray, LargeInteger>
        modRecLin(LargeIntegerArray array, LargeInteger modulus);

    /**
     * Returns a representation of this instance. If this is called,
     * then {@link #toByteTree(int)} may not be called.
     *
     * @return Representation of this instance.
     */
    @Override
    public abstract ByteTreeBasic toByteTree();

    /**
     * Returns a representation of this instance where the number of
     * bytes used in each byte tree is fixed. If this is called, then
     * {@link #toByteTree()} may not be called and all subsequent
     * calls must have the same expected byte length.
     *
     * @param expectedByteLength Number of bytes used in the byte
     * array embedded into the byte tree representations of
     * the integers of this instance.
     * @return Representation of this instance.
     */
    public abstract ByteTreeBasic toByteTree(int expectedByteLength);

    /**
     * Returns the number of integers in this instance.
     *
     * @return Number of integers in this instance.
     */
    public abstract int size();

    /**
     * Returns true or false depending on if all integers in this
     * instance are quadratic residues modulo the given prime or not.
     *
     * @param prime Prime modulus.
     * @return True if and only if all integers in this instance are
     *         quadratic residues modulo the given prime.
     */
    public abstract boolean quadraticResidues(LargeInteger prime);

    /**
     * Releases any resources allocated by this instance, e.g., a file
     * based implementation may delete the underlying file. It is the
     * responsibility of the programmer to only call this method if
     * this instance is not used again and to call it as needed.
     */
    public abstract void free();

    // Implemented in terms of the above.

    /**
     * Returns a human readable description of this instance. This
     * should only be used for debugging.
     *
     * @return Human readable description of this instance.
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();

        final LargeIntegerIterator lii = getIterator();

        sb.append('(');
        while (lii.hasNext()) {

            sb.append(lii.next().toString());
            sb.append(',');

        }
        sb.delete(Math.max(0, sb.length() - 1), sb.length());
        lii.close();

        sb.append(')');
        return sb.toString();
    }

    /**
     * Orders arrays of integers with the same number of elements
     * lexicographically, i.e., comparisons are performed
     * componentwise starting with the component at index zero.
     *
     * @param array Instance to which this instance is compared.
     * @return -1, 0, or 1 depending on if this element comes before,
     *         is equal to, or comes after the input.
     */
    public int compareTo(final LargeIntegerArray array) {

        final LargeIntegerIterator lii1 = getIterator();
        final LargeIntegerIterator lii2 = array.getIterator();

        int res = 0;
        while (lii1.hasNext() && res == 0) {
            res = lii1.next().compareTo(lii2.next());
        }
        lii2.close();
        lii1.close();

        return res;
    }

    /**
     * Performs an element-wise equality test of this instance and the
     * input.
     *
     * @param array Array of integers.
     * @return Boolean array containing the result of the equality
     *         tests.
     */
    public boolean[] equalsAll(final LargeIntegerArray array) {

        final boolean[] res = new boolean[size()];

        final LargeIntegerIterator lii1 = getIterator();
        final LargeIntegerIterator lii2 = array.getIterator();

        int i = 0;
        while (lii1.hasNext()) {
            res[i++] = lii1.next().equals(lii2.next());
        }
        lii2.close();
        lii1.close();

        return res;
    }

    /**
     * Computes the modular sum of all elements in this instance.
     *
     * @param modulus Modulus.
     * @return Modular sum of all elements in this instance.
     */
    public LargeInteger modSum(final LargeInteger modulus) {

        final LargeIntegerIterator lii = getIterator();

        LargeInteger res = LargeInteger.ZERO;
        while (lii.hasNext()) {
            res = res.add(lii.next()).mod(modulus);
        }
        lii.close();

        return res;
    }

    /**
     * Computes the product of all elements in this instance.
     *
     * @param modulus Modulus.
     * @return Modular product of all elements in this instance.
     */
    public abstract LargeInteger modProd(final LargeInteger modulus);

    /**
     * Computes the modular inner product of this instance and the
     * input.
     *
     * @param vectorArray Array of integers.
     * @param modulus Modulus.
     * @return Array of all the results.
     */
    public LargeInteger modInnerProduct(final LargeIntegerArray vectorArray,
                                 final LargeInteger modulus) {

        final LargeIntegerIterator lii1 = getIterator();
        final LargeIntegerIterator lii2 = vectorArray.getIterator();

        LargeInteger res = LargeInteger.ZERO;
        while (lii1.hasNext()) {
            res = res.add(lii1.next().mul(lii2.next())).mod(modulus);
        }
        lii2.close();
        lii1.close();

        return res;
    }
}
