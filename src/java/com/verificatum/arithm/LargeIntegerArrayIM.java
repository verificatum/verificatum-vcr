
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

import java.util.Arrays;

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.util.Pair;
import com.verificatum.util.Functions;


/**
 * Simple wrapper of primitive arrays of {@link LargeInteger}. This
 * works well as long as the arrays are not too large. For very large
 * arrays you should use {@link LargeIntegerArrayF} which is file
 * based, but neither this nor <code>LargeIntegerArrayF</code> should
 * be used directly. Use the initialization and factory methods of the
 * {@link LargeIntegerArray} base class instead.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerArrayIM extends LargeIntegerArray {

    /**
     * Representation of the elements of this instance.
     */
    public LargeInteger[] li;

    /**
     * Constructs an instance with the given integers.
     *
     * @param integers Integers of this instance.
     */
    public LargeIntegerArrayIM(final LargeInteger[] integers) {
        li = Arrays.copyOf(integers, integers.length);
    }

    /**
     * Constructs the concatenation of the inputs.
     *
     * @param arrays Arrays to be concatenated.
     */
    public LargeIntegerArrayIM(final LargeIntegerArray... arrays) {

        int total = 0;
        for (int i = 0; i < arrays.length; i++) {
            total += arrays[i].size();
        }

        li = new LargeInteger[total];

        int offset = 0;
        for (int i = 0; i < arrays.length; i++) {
            final int len = arrays[i].size();
            System.arraycopy(((LargeIntegerArrayIM) arrays[i]).li,
                             0,
                             li,
                             offset,
                             len);
            offset += len;
        }
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param bitLength Number of bits in each integer.
     * @param randomSource Source of random bits used to initialize
     * the array.
     */
    LargeIntegerArrayIM(final int size,
                        final int bitLength,
                        final RandomSource randomSource) {
        li = LargeInteger.random(size, bitLength, randomSource);
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param modulus Modulus.
     * @param statDist Decides the statistical distance from the
     * @param randomSource Source of random bits used to initialize
     * the array.
     */
    LargeIntegerArrayIM(final int size,
                        final LargeInteger modulus,
                        final int statDist,
                        final RandomSource randomSource) {
        li = LargeInteger.random(size, modulus, statDist, randomSource);
    }

    /**
     * Constructs an array by repeating a given integer.
     *
     * @param size Number of elements to generate.
     * @param value Value to be repeated.
     */
    LargeIntegerArrayIM(final int size, final LargeInteger value) {
        li = new LargeInteger[size];
        Arrays.fill(li, value);
    }

    /**
     * Constructs an array of consecutive integers.
     *
     * @param begin Inclusive starting integer in sequence.
     * @param end Exclusive ending integer in sequence.
     */
    LargeIntegerArrayIM(final int begin, final int end) {
        li = new LargeInteger[end - begin];
        for (int i = 0; i < li.length; i++) {
            li[i] = new LargeInteger(i);
        }
    }

    /**
     * Returns the array of integers represented by the input. This
     * constructor requires that each integer falls into the given
     * interval, but also that the representation of each integer is
     * of equal size to the byte tree representation of the upper
     * bound.
     *
     * @param size Expected number of elements in array. If the
     * expected size (number of elements) is set to zero, then the
     * input can have any size.
     * @param btr Should contain a representation of an array of
     * integers.
     * @param lb Non-negative inclusive lower bound for integers.
     * @param ub Positive exclusive upper bound for integers.
     *
     * @throws ArithmFormatException If the input does not represent a
     *  an array of integers satisfying the given bounds.
     * @throws EIOException If the input byte tree has the wrong
     * format.
     */
    LargeIntegerArrayIM(final int size,
                        final ByteTreeReader btr,
                        final LargeInteger lb,
                        final LargeInteger ub)
        throws EIOException, ArithmFormatException {
        li = LargeInteger.toLargeIntegers(size, btr, lb, ub);
    }

    // Documented in LargeIntegerArray.java

    @Override
    public LargeIntegerIterator getIterator() {
        return new LargeIntegerIteratorIM(this);
    }

    @Override
    public LargeIntegerArray modInv(final LargeInteger modulus)
        throws ArithmException {
        return new LargeIntegerArrayIM(LargeInteger.modInv(li, modulus));
    }

    @Override
    public LargeIntegerArray copyOfRange(final int startIndex,
                                         final int endIndex) {
        final LargeInteger[] cli = Arrays.copyOfRange(li, startIndex, endIndex);
        return new LargeIntegerArrayIM(cli);
    }

    @Override
    public LargeInteger[] integers() {
        return Arrays.copyOf(li, li.length);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LargeIntegerArrayIM)) {
            return false;
        }

        return Arrays.equals(li, ((LargeIntegerArrayIM) obj).li);
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public LargeIntegerArray extract(final boolean[] valid) {
        int total = 0;
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                total++;
            }
        }
        final LargeInteger[] res = new LargeInteger[total];
        int i = 0;
        for (int index = 0; index < valid.length; index++) {
            if (valid[index]) {
                res[i] = li[index];
                i++;
            }
        }
        return new LargeIntegerArrayIM(res);
    }

    @Override
    public LargeIntegerArray permute(final Permutation permutation) {
        final LargeInteger[] permInts = new LargeInteger[li.length];

        if (!(permutation instanceof PermutationIM)) {
            final String e =
                "File-mapped permutation used with memory-mapped array!";
            throw new ArithmError(e);
        }
        ((PermutationIM) permutation).applyPermutation(li, permInts);
        return new LargeIntegerArrayIM(permInts);
    }

    @Override
    public LargeIntegerArray modAdd(final LargeIntegerArray termsArray,
                                    final LargeInteger modulus) {
        final LargeInteger[] terms = ((LargeIntegerArrayIM) termsArray).li;
        return new LargeIntegerArrayIM(LargeInteger.modAdd(li, terms, modulus));
    }

    @Override
    public LargeIntegerArray modNeg(final LargeInteger modulus) {
        return new LargeIntegerArrayIM(LargeInteger.modNeg(li, modulus));
    }

    @Override
    public LargeIntegerArray modMul(final LargeIntegerArray factorsArray,
                                    final LargeInteger modulus) {
        final LargeInteger[] factors = ((LargeIntegerArrayIM) factorsArray).li;
        return new LargeIntegerArrayIM(LargeInteger.modMul(li,
                                                           factors,
                                                           modulus));
    }

    @Override
    public LargeIntegerArray modMul(final LargeInteger scalar,
                                    final LargeInteger modulus) {
        return new LargeIntegerArrayIM(LargeInteger.modMul(li,
                                                           scalar,
                                                           modulus));
    }

    @Override
    public LargeIntegerArray modPow(final LargeIntegerArray exponentsArray,
                                    final LargeInteger modulus) {
        final LargeInteger[] exponents =
            ((LargeIntegerArrayIM) exponentsArray).li;
        final LargeInteger[] res = LargeInteger.modPow(li, exponents, modulus);
        return new LargeIntegerArrayIM(res);
    }

    @Override
    public LargeIntegerArray modPow(final LargeInteger exponent,
                                    final LargeInteger modulus) {
        final LargeInteger[] res = LargeInteger.modPow(li, exponent, modulus);
        return new LargeIntegerArrayIM(res);
    }

    @Override
    public LargeIntegerArray modPowVariant(final LargeInteger basis,
                                           final LargeInteger modulus) {
        final LargeInteger[] res = basis.modPow(li, modulus);
        return new LargeIntegerArrayIM(res);
    }

    @Override
    public LargeInteger modPowProd(final LargeIntegerArray exponentsArray,
                                   final LargeInteger modulus) {
        final LargeInteger[] exponents =
            ((LargeIntegerArrayIM) exponentsArray).li;
        return LargeInteger.modPowProd(li, exponents, modulus);
    }

    @Override
    public LargeIntegerArray modProds(final LargeInteger modulus) {
        final LargeInteger[] res =
            LargeInteger.modProds(LargeInteger.ONE, li, modulus);
        return new LargeIntegerArrayIM(res);
    }

    @Override
    public LargeInteger get(final int index) {
        return li[index];
    }

    @Override
    public LargeIntegerArray shiftPush(final LargeInteger integer) {
        final LargeInteger[] res = new LargeInteger[li.length];
        res[0] = integer;
        System.arraycopy(li, 0, res, 1, li.length - 1);
        return new LargeIntegerArrayIM(res);
    }

    @Override
    public Pair<LargeIntegerArray, LargeInteger>
        modRecLin(final LargeIntegerArray array, final LargeInteger modulus) {

        final LargeInteger[] integers1 = ((LargeIntegerArrayIM) array).li;

        final LargeInteger[] res = new LargeInteger[li.length];
        res[0] = li[0];
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i - 1].mul(integers1[i]).add(li[i]).mod(modulus);
        }
        final LargeIntegerArrayIM a = new LargeIntegerArrayIM(res);
        final LargeInteger li = res[res.length - 1];
        return new Pair<LargeIntegerArray, LargeInteger>(a, li);
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return LargeInteger.toByteTree(li);
    }

    @Override
    public ByteTreeBasic toByteTree(final int expectedByteLength) {
        return LargeInteger.toByteTree(expectedByteLength, li);
    }

    @Override
    public boolean quadraticResidues(final LargeInteger prime) {
        return LargeInteger.quadraticResidues(li, prime);
    }

    @CoberturaIgnore
    @Override
    public int size() {
        return li.length;
    }

    @Override
    public void free() {

        // Allow garbage collection of the underlying primitive array.
        li = null;
    }

    @Override
    public int compareTo(final LargeIntegerArray array) {
        return LargeInteger.compareTo(li, ((LargeIntegerArrayIM) array).li);
    }

    @Override
    public boolean[] equalsAll(final LargeIntegerArray array) {
        return LargeInteger.equalsAll(li, ((LargeIntegerArrayIM) array).li);
    }

    @Override
    public LargeInteger modSum(final LargeInteger modulus) {
        return LargeInteger.modSum(li, modulus);
    }

    @Override
    public LargeInteger modProd(final LargeInteger modulus) {
        return LargeInteger.modProd(li, modulus);
    }

    @Override
    public LargeInteger modInnerProduct(final LargeIntegerArray vectorArray,
                                 final LargeInteger modulus) {
        return LargeInteger.modInnerProduct(li,
                                     ((LargeIntegerArrayIM) vectorArray).li,
                                     modulus);
    }
}
