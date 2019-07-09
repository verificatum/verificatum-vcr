
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

package com.verificatum.tests.arithm;

import java.util.Arrays;

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeIntegerArray;
import com.verificatum.arithm.Permutation;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link Permutation}.
 *
 * @author Douglas Wikstrom
 */
public class TestPermutation extends TestClass {

    /**
     * Batch size used.
     */
    public static final int TEST_BATCH_SIZE = 5;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPermutation(final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    protected void byteTree()
        throws ArithmFormatException {

        Permutation x;
        ByteTreeReader btr;
        Permutation y;

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            x = Permutation.random(size, rs, 10);
            btr = x.toByteTree().getByteTreeReader();
            y = Permutation.toPermutation(x.size(), btr);
            x.free();
            y.free();

            x = Permutation.random(size, rs, 10);
            btr = x.toByteTree().getByteTreeReader();
            y = Permutation.unsafeToPermutation(x.size(), btr);
            x.free();
            y.free();

            size++;
        }

        boolean invalid = false;
        try {
            btr = new ByteTree().getByteTreeReader();
            Permutation.unsafeToPermutation(10, btr);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTreeIM()
        throws ArithmFormatException {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        byteTree();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTreeF()
        throws ArithmFormatException {
        TestLargeIntegerArray.memoryBased();
        byteTree();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Map.
     */
    protected void map() {
        final int size = 10;
        final Permutation permutation = Permutation.identity(size);
        for (int i = 0; i < size; i++) {
            assert permutation.map(i) == i : "Failed to map index!";
        }
        permutation.free();
    }

    /**
     * Map.
     */
    public void mapIM() {
        TestLargeIntegerArray.memoryBased();
        map();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Map.
     */
    public void mapF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        map();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Excercise hashCode.
     */
    protected void excHashCode() {
        final Permutation permutation = Permutation.identity(10);
        permutation.hashCode();
        permutation.free();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCodeIM() {
        TestLargeIntegerArray.memoryBased();
        excHashCode();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCodeF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        excHashCode();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Excercise toString.
     */
    protected void excToString() {
        final Permutation permutation = Permutation.identity(10);
        permutation.toString();
        permutation.free();
    }

    /**
     * Excercise toString.
     */
    public void excToStringIM() {
        TestLargeIntegerArray.memoryBased();
        excToString();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Excercise toString.
     */
    public void excToStringF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        excToString();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Equals.
     */
    protected void equality() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final Permutation x = Permutation.identity(size);
            final Permutation y = Permutation.identity(size);

            assert x.equals(x) : "Equality by reference failed";
            assert x.equals(y) : "Equality by content failed!";

            // Random permutations of sufficient size are almost
            // surely distinct.
            if (size > 50) {
                final Permutation z = Permutation.random(size, rs, 10);
                assert !x.equals(z) : "Inequality failed!";
            }

            x.free();
            y.free();

            size++;
        }

        final Permutation x = Permutation.identity(1);
        assert !x.equals(new Object())
            : "Failed to fail on comparison with bad instance!";
        x.free();
    }

    /**
     * Equals.
     */
    public void equalityIM() {
        TestLargeIntegerArray.memoryBased();
        equality();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Equals.
     */
    public void equalityF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        equality();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Inversion.
     *
     * @throws ArithmException If a test failed.
     */
    protected void inv()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final Permutation x = Permutation.random(size, rs, 10);
            final Permutation y = x.inv();
            final Permutation z = y.inv();

            assert x.equals(z) : "Inversion failed!";

            // Random permutations of sufficient size are almost
            // surely not identical to their inverses.
            if (size > 50) {
                assert !x.equals(y) : "Inversion failed!";
            }

            x.free();
            y.free();
            z.free();

            size++;
        }
    }

    /**
     * Inversion.
     *
     * @throws ArithmException If a test failed.
     */
    public void invIM()
        throws ArithmException {
        TestLargeIntegerArray.memoryBased();
        inv();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Inversion.
     *
     * @throws ArithmException If a test failed.
     */
    public void invF()
        throws ArithmException {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        inv();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Shrink.
     */
    protected void shrink() {

        final Timer timer = new Timer(testTime);

        int size = 10;

        // General case.
        while (!timer.timeIsUp()) {

            final Permutation permutation = Permutation.random(size, rs, 10);

            for (int i = size; i >= 0; i--) {

                final int[] injection = new int[size];
                Arrays.fill(injection, -1);
                for (int j = 0; j < i; j++) {
                    injection[permutation.map(j)] = j;
                }

                final int[] compressed = new int[i];
                int k = 0;
                for (int l = 0; l < i; l++) { // NOPMD
                    while (injection[k] < 0) {
                        k++;
                    }
                    compressed[l] = injection[k];
                    k++;
                }

                final Permutation shrunk = permutation.shrink(i);

                assert shrunk.size() == i
                    : "Shrunk permutation has wrong size!";

                final Permutation inverse = shrunk.inv();

                for (int j = 0; j < i; j++) {
                    assert inverse.map(j) == compressed[j]
                        : "Shrunk permutation is incorrect!";
                }

                shrunk.free();
                inverse.free();
            }

            permutation.free();

            size++;
        }
    }

    /**
     * Shrink.
     */
    public void shrinkIM() {
        TestLargeIntegerArray.memoryBased();
        shrink();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Shrink.
     */
    public void shrinkF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        shrink();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Apply permutation.
     */
    protected void applyPermutation() {

        final Timer timer = new Timer(testTime);

        int size = 1;
        final int bitLength = 100;

        // We use large integer arrays here as an object for
        // permutation.

        // General case.
        while (!timer.timeIsUp()) {

            final LargeIntegerArray x =
                LargeIntegerArray.random(size, bitLength, rs);
            final Permutation permutation = Permutation.random(size, rs, 10);
            final Permutation inverse = permutation.inv();

            final LargeIntegerArray y = x.permute(permutation);
            final LargeIntegerArray z = y.permute(inverse);

            assert x.equals(z) : "Failed to permute integers!";

            x.free();
            y.free();
            z.free();

            permutation.free();
            inverse.free();

            size++;
        }
    }

    /**
     * Apply permutation.
     */
    public void applyPermutationIM() {
        TestLargeIntegerArray.memoryBased();
        applyPermutation();
        TestLargeIntegerArray.resetBased();
    }


    /**
     * Apply permutation.
     */
    public void applyPermutationF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        applyPermutation();
        TestLargeIntegerArray.resetBased();
    }
}
