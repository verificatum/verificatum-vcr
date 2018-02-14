
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

package com.verificatum.tests.arithm;

import java.io.File;
import java.util.Arrays;

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.LargeIntegerArray;
import com.verificatum.arithm.LargeIntegerArrayIM;
import com.verificatum.arithm.LargeIntegerIterator;
import com.verificatum.arithm.LargeIntegerIteratorF;
import com.verificatum.arithm.Permutation;
import com.verificatum.arithm.PermutationIM;
import com.verificatum.arithm.PermutationF;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.TempFile;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public final class TestLargeIntegerArray extends TestClass {

    /**
     * Batch size used.
     */
    public static final int TEST_BATCH_SIZE = 5;

    /**
     * Default bit length.
     */
    private final int bitLength;

    /**
     * Default modulus.
     */
    private final LargeInteger modulus;

    /**
     * Cached version of model.
     */
    private static boolean oldInMemory;

    /**
     * Cached batch size.
     */
    private static int oldBatchSize;

    /**
     * Store previous model and use file based model.
     *
     * @param batchSize Batch size.
     */
    public static void fileBased(final int batchSize) {
        oldInMemory = LargeIntegerArray.getMemoryBased();
        oldBatchSize = LargeIntegerArray.getBatchSize();
        LargeIntegerArray.useFileBased(batchSize);
    }

    /**
     * Store previous model and use memory based model.
     */
    public static void memoryBased() {
        oldInMemory = LargeIntegerArray.getMemoryBased();
        oldBatchSize = LargeIntegerArray.getBatchSize();
        LargeIntegerArray.useMemoryBased();
    }

    /**
     * Restore previous model.
     */
    public static void resetBased() {
        if (oldInMemory) {
            LargeIntegerArray.useMemoryBased();
        } else {
            LargeIntegerArray.useFileBased(oldBatchSize);
        }
    }

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If a test failed.
     */
    public TestLargeIntegerArray(final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.bitLength = 100;
        this.modulus = new LargeInteger(bitLength, rs).nextPrime(rs, 20);
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    private void byteTree()
        throws ArithmFormatException {

        final Timer timer = new Timer(testTime);

        final int size = 1;
        final int bitLength = 100;

        // General case.
        while (!timer.timeIsUp()) {
            final LargeInteger lb = LargeInteger.ZERO;
            final LargeInteger ub = LargeInteger.ONE.shiftLeft(bitLength);

            final LargeIntegerArray x =
                LargeIntegerArray.random(size, bitLength, rs);
            final ByteTreeReader btr =
                x.toByteTree(ub.toByteArray().length).getByteTreeReader();

            final LargeIntegerArray y =
                LargeIntegerArray.toLargeIntegerArray(size, btr, lb, ub);

            x.free();
            y.free();
        }

        // Fail on lower bound.
        boolean invalid = false;
        LargeIntegerArray xa = null;
        LargeIntegerArray ya = null;
        try {
            final LargeInteger lb = LargeInteger.ZERO;
            final LargeInteger ub = LargeInteger.ONE.shiftLeft(bitLength);

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            x[0] = lb.sub(LargeInteger.ONE);
            xa = LargeIntegerArray.toLargeIntegerArray(x);
            final ByteTreeReader btr =
                xa.toByteTree(ub.toByteArray().length).getByteTreeReader();

            ya = LargeIntegerArray.toLargeIntegerArray(size, btr, lb, ub);

        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on input outside of bounds!";
        xa.free();
        if (ya != null) {
            ya.free();
        }

        // Fail on upper bound.
        invalid = false;
        xa = null;
        ya = null;
        try {
            final LargeInteger lb = LargeInteger.ZERO;
            final LargeInteger ub = LargeInteger.ONE.shiftLeft(bitLength);

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            x[0] = ub.add(LargeInteger.ONE);
            xa = LargeIntegerArray.toLargeIntegerArray(x);
            final ByteTreeReader btr =
                xa.toByteTree(ub.toByteArray().length).getByteTreeReader();

            ya = LargeIntegerArray.toLargeIntegerArray(size, btr, lb, ub);

        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on input outside of bounds!";
        xa.free();
        if (ya != null) {
            ya.free();
        }
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTreeF()
        throws ArithmFormatException {
        fileBased(TEST_BATCH_SIZE);
        byteTree();
        resetBased();
    }

    /**
     * Concatenation.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTreeIM()
        throws ArithmFormatException {
        memoryBased();
        byteTree();
        resetBased();
    }

    /**
     * Concatenation.
     */
    private void concatenation() {
        final int size = 10;
        final int bitSize = 100;

        for (int len = 1; len < 5; len++) {
            final LargeIntegerArray[] xas = new LargeIntegerArray[len];
            for (int i = 0; i < len; i++) {
                xas[i] = LargeIntegerArray.random(size, bitSize, rs);
            }
            final LargeIntegerArray ya =
                LargeIntegerArray.toLargeIntegerArray(xas);
            final LargeInteger[] y = ya.integers();
            for (int i = 0; i < len; i++) {
                final LargeInteger[] x = xas[i].integers();
                for (int j = 0; j < size; j++) {
                    assert x[j].equals(y[i * size + j])
                        : "Failed to concatenate!";
                }
            }
            for (int i = 0; i < len; i++) {
                xas[i].free();
            }
            ya.free();
        }
    }

    /**
     * Concatenation.
     */
    public void concatenationF() {
        fileBased(TEST_BATCH_SIZE);
        concatenation();
        resetBased();
    }

    /**
     * Concatenation.
     */
    public void concatenationIM() {
        memoryBased();
        concatenation();
        resetBased();
    }

    /**
     * Exercises toString.
     */
    private void toStringC() {
        final LargeIntegerArray x = LargeIntegerArray.random(1, 10, rs);
        x.toString();
        x.free();
    }

    /**
     * Exercises toString.
     */
    public void toStringF() {
        fileBased(TEST_BATCH_SIZE);
        toStringC();
        resetBased();
    }

    /**
     * Exercises toString.
     */
    public void toStringIM() {
        memoryBased();
        toStringC();
        resetBased();
    }

    /**
     * Equals.
     */
    private void equality() {

        final Timer timer = new Timer(testTime);

        int size = 1;
        final int bitLength = 100;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeIntegerArray x =
                LargeIntegerArray.random(size, bitLength, rs);
            final LargeIntegerArray y =
                LargeIntegerArray.random(size, bitLength, rs);
            final LargeIntegerArray z =
                LargeIntegerArray.toLargeIntegerArray(x.integers());

            assert x.equals(x) : "Equality by reference failed!";
            assert x.equals(z) : "Equality by elements failed!";

            assert !x.equals(y) : "Inequality failed!";

            x.free();
            y.free();
            z.free();

            size++;
        }

        final LargeIntegerArray x = LargeIntegerArray.random(1, 10, rs);
        assert !x.equals(new Object())
            : "Inequality with non-LargeIntegerArray instance failed!";
        x.free();
    }

    /**
     * Equals.
     */
    public void equalityF() {
        fileBased(TEST_BATCH_SIZE);
        equality();
        resetBased();
    }

    /**
     * Equals.
     */
    public void equalityIM() {
        memoryBased();
        equality();
        resetBased();
    }

    /**
     * Extracting subset.
     */
    private void extract() {

        final Timer timer = new Timer(testTime);

        final int size = 1;
        final int bitLength = 100;

        // General case.
        while (!timer.timeIsUp()) {

            // Random subset.
            final boolean[] b = new boolean[size];
            for (int i = 0; i < b.length; i++) {
                b[i] = Math.abs(rs.getBytes(1)[0]) % 2 == 1;
            }

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);
            final LargeIntegerArray ya = xa.extract(b);
            final LargeInteger[] y = ya.integers();

            int j = 0;
            for (int i = 0; i < x.length; i++) {
                if (b[i]) {
                    assert x[i].equals(y[j]) : "Missing element in extract!";
                    j++;
                }
            }
            xa.free();
            ya.free();
        }
    }

    /**
     * Extracting subset.
     */
    public void extractF() {
        fileBased(TEST_BATCH_SIZE);
        extract();
        resetBased();
    }

    /**
     * Extracting subset.
     */
    public void extractIM() {
        memoryBased();
        extract();
        resetBased();
    }

    /**
     * Exercises hash code computation.
     */
    private void hashCodeC() {
        final LargeIntegerArray xa = LargeIntegerArray.random(10, 100, rs);
        xa.hashCode();
        xa.free();
    }

    /**
     * Exercises hash code computation.
     *
     * @throws ArithmException If a test failed.
     * @throws ArithmFormatException If a test failed.
     */
    public void hashCodeF()
        throws ArithmException, ArithmFormatException {
        fileBased(TEST_BATCH_SIZE);
        hashCodeC();
        resetBased();
    }

    /**
     * Exercises hash code computation.
     */
    public void hashCodeIM() {
        memoryBased();
        hashCodeC();
        resetBased();
    }

    /**
     * Exercises modular recursive linear expression appearing in some
     * zero knowledge proofs.
     */
    private void excModRecLin() {
        final LargeIntegerArray x = LargeIntegerArray.random(10, bitLength, rs);
        final LargeIntegerArray y = LargeIntegerArray.random(10, bitLength, rs);

        x.modRecLin(y, modulus);

        x.free();
        y.free();
    }

    /**
     * Exercises modular recursive linear expression appearing in some
     * zero knowledge proofs.
     */
    public void excModRecLinF() {
        fileBased(TEST_BATCH_SIZE);
        excModRecLin();
        resetBased();
    }

    /**
     * Exercises modular recursive linear expression appearing in some
     * zero knowledge proofs.
     */
    public void excModRecLinIM() {
        memoryBased();
        excModRecLin();
        resetBased();
    }

    /**
     * Arithmetic using integer arrays.
     *
     * @throws ArithmException If a test failed.
     */
    private void modArithmetic()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        int size = 1;
        final int bitLength = 100;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);

            final LargeInteger[] y = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray ya =
                LargeIntegerArray.toLargeIntegerArray(y);

            final LargeInteger[] z = LargeInteger.modAdd(x, y, modulus);
            final LargeIntegerArray za =
                LargeIntegerArray.toLargeIntegerArray(z);
            final LargeIntegerArray zza = xa.modAdd(ya, modulus);

            assert zza.equals(za) : "Addition failed!";

            final LargeIntegerArray na = xa.modNeg(modulus);
            final LargeIntegerArray zero1 =
                LargeIntegerArray.fill(xa.size(), LargeInteger.ZERO);
            final LargeIntegerArray zero2 = xa.modAdd(na, modulus);

            assert zero1.equals(zero2) : "Negation failed!";

            final LargeInteger[] w = LargeInteger.modMul(x, y, modulus);
            final LargeIntegerArray wa =
                LargeIntegerArray.toLargeIntegerArray(w);
            final LargeIntegerArray wwa = xa.modMul(ya, modulus);

            assert zza.equals(za) : "Multiplication failed!";

            final LargeInteger s = new LargeInteger(bitLength, rs);
            final LargeInteger[] u = LargeInteger.modMul(x, s, modulus);
            final LargeIntegerArray ua =
                LargeIntegerArray.toLargeIntegerArray(u);
            final LargeIntegerArray uua = xa.modMul(s, modulus);

            assert uua.equals(ua) : "Multiplication with scalar failed!";

            final LargeInteger[] v = LargeInteger.modInv(x, modulus);
            final LargeIntegerArray va =
                LargeIntegerArray.toLargeIntegerArray(v);
            final LargeIntegerArray vva = xa.modInv(modulus);

            assert vva.equals(va) : "Inversion failed!";

            xa.free();
            ya.free();
            za.free();
            zza.free();
            na.free();
            zero1.free();
            zero2.free();
            wa.free();
            wwa.free();
            ua.free();
            uua.free();
            va.free();
            vva.free();

            size++;
        }
    }

    /**
     * Arithmetic using integer arrays.
     *
     * @throws ArithmException If a test failed.
     */
    public void modArithmeticF()
        throws ArithmException {
        fileBased(TEST_BATCH_SIZE);
        modArithmetic();
        resetBased();
    }

    /**
     * Arithmetic using integer arrays.
     *
     * @throws ArithmException If a test failed.
     */
    public void modArithmeticIM()
        throws ArithmException {
        memoryBased();
        modArithmetic();
        resetBased();
    }

    /**
     * Verifies copying a range of elements from an array stored on
     * file.
     */
    private void copyOfRange() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final int startIndex = size / 4;
            final int endIndex = Math.max(startIndex + 1, 3 * (size / 4));

            final LargeIntegerArray xa =
                LargeIntegerArray.random(size, modulus, 20, rs);

            final LargeInteger[] y =
                Arrays.copyOfRange(xa.integers(), startIndex, endIndex);
            final LargeIntegerArray ya =
                LargeIntegerArray.toLargeIntegerArray(y);

            final LargeIntegerArray yya = xa.copyOfRange(startIndex, endIndex);

            assert yya.equals(ya) : "Failed to copy range of integers!";

            xa.free();
            ya.free();
            yya.free();

            size++;
        }
    }

    /**
     * Verifies copying a range of elements from an array stored on
     * file.
     */
    public void copyOfRangeF() {
        fileBased(TEST_BATCH_SIZE);
        copyOfRange();
        resetBased();
    }

    /**
     * Verifies copying a range of elements from an array stored on
     * file.
     */
    public void copyOfRangeIM() {
        memoryBased();
        copyOfRange();
        resetBased();
    }

    /**
     * Verifies permutation of elements in an array stored on file.
     */
    private void permute() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final LargeIntegerArray a =
                LargeIntegerArray.random(size, modulus, 20, rs);

            final Permutation permutation = Permutation.random(size, rs, 100);
            final LargeIntegerArray permuted = a.permute(permutation);

            final LargeIntegerArray aa = permuted.permute(permutation.inv());

            assert a.equals(aa)
                : "Failed to permute integers!";

            a.free();
            aa.free();

            size++;
        }
    }

    /**
     * Verifies permutation of elements in an array stored on file.
     */
    public void permuteF() {
        fileBased(TEST_BATCH_SIZE);
        permute();

        final LargeIntegerArray x = LargeIntegerArray.random(1, 10, rs);
        Permutation p = new PermutationIM(1);
        try {
            x.permute(p);
        } catch (final ArithmError ae) {
            p = null;
        }
        assert p == null : "Failed to fail on memory based permutation!";

        resetBased();
    }

    /**
     * Verifies permutation of elements in an array stored on file.
     */
    public void permuteIM() {
        memoryBased();
        permute();

        final LargeIntegerArray x = LargeIntegerArray.random(1, 10, rs);
        Permutation p = new PermutationF(1);
        try {
            x.permute(p);
        } catch (final ArithmError ae) {
            p = null;
        }
        assert p == null : "Failed to fail on file based permutation!";

        resetBased();
    }

    /**
     * Getting elements from an array.
     */
    private void get() {

        final int size = 10;
        final int bitLength = 100;

        final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
        final LargeIntegerArray xa = LargeIntegerArray.toLargeIntegerArray(x);

        final LargeInteger[] y = new LargeInteger[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = xa.get(i);
        }
        final LargeIntegerArray ya = LargeIntegerArray.toLargeIntegerArray(y);

        assert ya.equals(xa) : "Failing to get elements!";

        xa.free();
        ya.free();
    }

    /**
     * Getting elements from an array.
     */
    public void getF() {
        fileBased(TEST_BATCH_SIZE);
        get();
        resetBased();
    }

    /**
     * Getting elements from an array.
     */
    public void getIM() {
        memoryBased();
        get();
        resetBased();
    }

    /**
     * Shift and push.
     */
    private void shiftPush() {

        final Timer timer = new Timer(testTime);

        int size = 1;
        final int bitLength = 100;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);

            final LargeIntegerArray ya = xa.shiftPush(LargeInteger.ONE);
            final LargeInteger[] y = ya.integers();
            assert y[0].equals(LargeInteger.ONE) : "Failed to push!";

            for (int i = 1; i < y.length; i++) {
                assert y[i].equals(x[i - 1]) : "Failed to shift!";
            }
            xa.free();
            ya.free();

            size++;
        }
    }

    /**
     * Shift and push.
     */
    public void shiftPushF() {
        fileBased(TEST_BATCH_SIZE);
        shiftPush();
        resetBased();
    }

    /**
     * Shift and push.
     */
    public void shiftPushIM() {
        memoryBased();
        shiftPush();
        resetBased();
    }

    /**
     * Modular reduction.
     */
    private void mod() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeInteger[] y = new LargeInteger[x.length];
            for (int i = 0; i < x.length; i++) {
                y[i] = x[i].mod(modulus);
            }
            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);
            final LargeIntegerArray ya =
                LargeIntegerArray.toLargeIntegerArray(y);
            final LargeIntegerArray za = xa.mod(modulus);

            assert za.equals(ya) : "Modular reduction failed!";

            xa.free();
            ya.free();
            za.free();

            size++;
        }
    }

    /**
     * Modular reduction.
     */
    public void modF() {
        fileBased(TEST_BATCH_SIZE);
        mod();
        resetBased();
    }

    /**
     * Modular reduction.
     */
    public void modIM() {
        memoryBased();
        mod();
        resetBased();
    }

    /**
     * Modular summation of arrays.
     */
    private void modSum() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger modulus = new LargeInteger(bitLength, rs);
            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeInteger s = LargeInteger.modSum(x, modulus);

            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);
            final LargeInteger sa = xa.modSum(modulus);

            assert sa.equals(s) : "Modular sum failed!";

            xa.free();

            size++;
        }
    }

    /**
     * Modular summation of arrays.
     */
    public void modSumF() {
        fileBased(TEST_BATCH_SIZE);
        modSum();
        resetBased();
    }

    /**
     * Modular summation of arrays.
     */
    public void modSumIM() {
        memoryBased();
        modSum();
        resetBased();
    }

    /**
     * Modular power of arrays.
     */
    private void modPow() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] b = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray ba =
                LargeIntegerArray.toLargeIntegerArray(b);

            final LargeInteger[] e = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray ea =
                LargeIntegerArray.toLargeIntegerArray(e);

            final LargeInteger[] r = LargeInteger.modPow(b, e, modulus);
            final LargeIntegerArray ra =
                LargeIntegerArray.toLargeIntegerArray(r);
            final LargeIntegerArray rra = ba.modPow(ea, modulus);

            assert rra.equals(ra) : "Component-wise modular powers failed!";

            final LargeInteger es = new LargeInteger(bitLength, rs);
            final LargeInteger[] u = LargeInteger.modPow(b, es, modulus);
            final LargeIntegerArray ua =
                LargeIntegerArray.toLargeIntegerArray(u);
            final LargeIntegerArray uua = ba.modPow(es, modulus);

            assert uua.equals(ua) : "Modular power with scalar failed!";

            final LargeInteger[] v = new LargeInteger[e.length];
            for (int i = 0; i < e.length; i++) {
                v[i] = b[0].modPow(e[i], modulus);
            }
            final LargeIntegerArray va =
                LargeIntegerArray.toLargeIntegerArray(v);
            final LargeIntegerArray vva = ea.modPowVariant(b[0], modulus);

            assert vva.equals(va);

            ba.free();
            ea.free();
            ra.free();
            rra.free();
            ua.free();
            uua.free();
            va.free();
            vva.free();

            size++;
        }
    }

    /**
     * Modular power of arrays.
     */
    public void modPowF() {
        fileBased(TEST_BATCH_SIZE);
        modPow();
        resetBased();
    }

    /**
     * Modular power of arrays.
     */
    public void modPowIM() {
        memoryBased();
        modPow();
        resetBased();
    }

    /**
     * Modular power product.
     */
    private void modPowProd() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] b = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray ba =
                LargeIntegerArray.toLargeIntegerArray(b);

            final LargeInteger[] e = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray ea =
                LargeIntegerArray.toLargeIntegerArray(e);

            final LargeInteger r = LargeInteger.modPowProd(b, e, modulus);
            final LargeInteger rr = ba.modPowProd(ea, modulus);

            assert rr.equals(r) : "Modular power product failed!";

            ba.free();
            ea.free();

            size++;
        }
    }

    /**
     * Modular power product.
     */
    public void modPowProdF() {
        fileBased(TEST_BATCH_SIZE);
        modPowProd();
        resetBased();
    }

    /**
     * Modular power product.
     */
    public void modPowProdIM() {
        memoryBased();
        modPowProd();
        resetBased();
    }

    /**
     * Modular product.
     */
    private void modProd() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeInteger s = LargeInteger.modProd(x, modulus);

            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);
            final LargeInteger sa = xa.modProd(modulus);

            assert sa.equals(s) : "Modular product failed!";

            xa.free();

            size++;
        }
    }

    /**
     * Modular product.
     */
    public void modProdF() {
        fileBased(TEST_BATCH_SIZE);
        modProd();
        resetBased();
    }

    /**
     * Modular product.
     */
    public void modProdIM() {
        memoryBased();
        modProd();
        resetBased();
    }

    /**
     * Modular products.
     */
    private void modProds() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeInteger[] p =
                LargeInteger.modProds(LargeInteger.ONE, x, modulus);
            final LargeIntegerArray pa =
                LargeIntegerArray.toLargeIntegerArray(p);

            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);
            final LargeIntegerArray ppa = xa.modProds(modulus);

            assert ppa.equals(pa) : "Modular products failed!";

            pa.free();
            xa.free();
            ppa.free();

            size++;
        }
    }

    /**
     * Modular products.
     */
    public void modProdsF() {
        fileBased(TEST_BATCH_SIZE);
        modProds();
        resetBased();
    }

    /**
     * Modular products.
     */
    public void modProdsIM() {
        memoryBased();
        modProds();
        resetBased();
    }

    /**
     * Modular inner product.
     */
    private void modInnerProduct() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray xa =
                LargeIntegerArray.toLargeIntegerArray(x);

            final LargeInteger[] y = LargeInteger.random(size, bitLength, rs);
            final LargeIntegerArray ya =
                LargeIntegerArray.toLargeIntegerArray(y);

            final LargeInteger r = LargeInteger.modInnerProduct(x, y, modulus);
            final LargeInteger rr = xa.modInnerProduct(ya, modulus);

            assert r.equals(rr) : "Modular inner product failed!";

            xa.free();
            ya.free();

            size++;
        }
    }

    /**
     * Modular inner product.
     */
    public void modInnerProductF() {
        fileBased(TEST_BATCH_SIZE);
        modInnerProduct();
        resetBased();
    }

    /**
     * Modular inner product.
     */
    public void modInnerProductIM() {
        memoryBased();
        modInnerProduct();
        resetBased();
    }

    /**
     * Verifies component-wise equality testing.
     */
    private void equalsAll() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final LargeIntegerArray a =
                LargeIntegerArray.random(size, modulus, 20, rs);

            final LargeInteger[] aa = a.integers();
            final LargeInteger[] bb = a.integers();

            for (int i = 0; i < aa.length; i++) {
                if (i % 3 == 0) {
                    aa[i] = aa[i].add(bb[i]);
                }
            }

            final LargeIntegerArray fa =
                LargeIntegerArray.toLargeIntegerArray(aa);
            final LargeIntegerArray fb =
                LargeIntegerArray.toLargeIntegerArray(aa);
            final boolean[] fr = fa.equalsAll(fb);

            final LargeIntegerArray ma = new LargeIntegerArrayIM(aa);
            final LargeIntegerArray mb = new LargeIntegerArrayIM(aa);
            final boolean[] mr = ma.equalsAll(mb);

            assert Arrays.equals(fr, mr)
                : "Failed computing equality of arrays!";

            a.free();
            fa.free();
            fb.free();
            ma.free();
            mb.free();

            size++;
        }
    }

    /**
     * Verifies component-wise equality testing.
     */
    public void equalsAllF() {
        fileBased(TEST_BATCH_SIZE);
        equalsAll();
        resetBased();
    }

    /**
     * Verifies component-wise equality testing.
     */
    public void equalsAllIM() {
        memoryBased();
        equalsAll();
        resetBased();
    }

    /**
     * Quadratic residues.
     */
    private void quadraticResidues() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        LargeInteger nonResidue = LargeInteger.ONE;
        while (nonResidue.legendre(modulus) == 1) {
            nonResidue = new LargeInteger(512, rs);
        }

        while (!timer.timeIsUp()) {

            final LargeIntegerArray a =
                LargeIntegerArray.random(size, modulus, 20, rs);
            final LargeIntegerArray aa = a.modMul(a, modulus);
            final LargeIntegerArray bb = aa.modMul(nonResidue, modulus);

            assert aa.quadraticResidues(modulus)
                : "Failed to verify quadratic residues!";

            assert !bb.quadraticResidues(modulus)
                : "Failed to discover quadratic non-residues!";

            a.free();
            aa.free();
            bb.free();

            size++;
        }
    }

    /**
     * Quadratic residues.
     */
    public void quadraticResiduesF() {
        fileBased(TEST_BATCH_SIZE);
        quadraticResidues();
        resetBased();
    }

    /**
     * Quadratic residues.
     */
    public void quadraticResiduesIM() {
        memoryBased();
        quadraticResidues();
        resetBased();
    }

    /**
     * Excercise comparison.
     */
    private void compareTo() {

        final Timer timer = new Timer(testTime);

        final int size = 1;
        final int bitSize = 100;

        while (!timer.timeIsUp()) {
            final LargeIntegerArray x =
                LargeIntegerArray.random(size, bitSize, rs);
            final LargeIntegerArray y =
                LargeIntegerArray.random(size, bitSize, rs);

            x.compareTo(y);
        }
    }

    /**
     * Excercise comparison.
     */
    public void compareToF() {
        fileBased(TEST_BATCH_SIZE);
        compareTo();
        resetBased();
    }

    /**
     * Excercise comparison.
     */
    public void compareToIM() {
        memoryBased();
        compareTo();
        resetBased();
    }

    /**
     * Iterator for file model.
     */
    public void iteratorF() {
        fileBased(TEST_BATCH_SIZE);

        ByteTree bt = new ByteTree(new byte[1]);
        bt = new ByteTree(new ByteTree(bt));

        final File file = TempFile.getFile();
        try {
            bt.writeTo(file);
        } catch (final EIOException eioe) { // NOPMD
        }

        boolean invalid = false;
        try {

            final LargeIntegerIterator iter = new LargeIntegerIteratorF(file);
            iter.next();
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";

        resetBased();
    }
}
