
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

import java.math.BigInteger;
import java.util.Arrays;

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
@SuppressWarnings({"PMD.NcssMethodCount", "PMD.CyclomaticComplexity"})
public class TestLargeInteger extends TestClass {

    /**
     * Default bit length.
     */
    protected int bitLength;

    /**
     * Default modulus.
     */
    protected LargeInteger modulus;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestLargeInteger(final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.bitLength = 100;
        this.modulus = new LargeInteger(bitLength, rs).nextPrime(rs, 20);
    }

    /**
     * Constructors.
     */
    public void constructors() {

        new LargeInteger(100, rs);

        boolean invalid = false;
        try {
            new LargeInteger(-100, rs);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on non-positive bit lengths!";
    }

    /**
     * Byte tree.
     *
     * @throws ArithmFormatException If a test failed.
     */
    public void byteTree()
        throws ArithmFormatException {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            LargeInteger n = new LargeInteger(size, rs);
            final int byteLength = n.toByteArray().length;

            // Arbitrary integer.
            ByteTreeBasic bt = n.toByteTree();
            ByteTreeReader btr = bt.getByteTreeReader();
            LargeInteger m = new LargeInteger(btr);
            assert n.equals(m) : "Failed to convert positive integer!";

            n = LargeInteger.ZERO.sub(n);
            bt = n.toByteTree(n.toByteArray().length);
            btr = bt.getByteTreeReader();
            m = new LargeInteger(btr);
            assert n.equals(m) : "Failed to convert negative integer!";

            // Fail on zero-length data.
            bt = new ByteTree(new byte[0]);
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(btr);
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on zero "
                                                + "length!");
            }

            // Fail on bad byte tree.
            ByteTree[] bta = new ByteTree[2];
            bta[0] = new ByteTree(new byte[1]);
            bta[1] = new ByteTree(new byte[1]);
            bt = new ByteTree(bta);
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(btr);
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on bad byte "
                                                + "tree!");
            }


            // Integer with bound on data size.
            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            m = new LargeInteger(byteLength, btr);
            assert n.equals(m) : "Failed to convert integer with bound!";

            // Fail on wrong byte length
            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(byteLength - 1, btr);
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on wrong byte "
                                                + "length!");
            }

            // Fail on zero-length data.
            bt = new ByteTree(new byte[0]);
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(0, btr);
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on zero-length "
                                                + "data!");
            }

            // Fail on bad byte tree.
            bta = new ByteTree[2];
            bta[0] = new ByteTree(new byte[1]);
            bta[1] = new ByteTree(new byte[1]);
            bt = new ByteTree(bta);
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(2, btr);
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on bad byte "
                                                + "tree!");
            }


            // Integer with fixed data size.
            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            m = new LargeInteger(byteLength, btr, new Object());
            assert n.equals(m) : "Failed to convert integer with fixed size!";

            // Fail on wrong data length.
            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(byteLength + 1, btr, new Object());
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on wrong data "
                                                + "length!");
            }

            // Fail on zero-length data.
            bt = new ByteTree(new byte[0]);
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(0, btr, new Object());
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on zero-length "
                                                + "data!");
            }

            // Fail on bad byte tree.
            bta = new ByteTree[2];
            bta[0] = new ByteTree(new byte[1]);
            bta[1] = new ByteTree(new byte[1]);
            bt = new ByteTree(bta);
            btr = bt.getByteTreeReader();
            try {
                m = new LargeInteger(2, btr, new Object());
            } catch (final ArithmFormatException afe) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail on bad byte "
                                                + "tree!");
            }


            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            m = LargeInteger.safeLargeInteger(byteLength, btr);
            assert n.equals(m) : "Failed to convert integer safe!";

            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            m = LargeInteger.safeLargeInteger(byteLength - 1, btr);
            assert m.equals(LargeInteger.ZERO) : "Failed to fail on safe!";


            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            m = LargeInteger.unsafeLargeInteger(byteLength, btr);
            assert n.equals(m) : "Failed to convert integer unsafe!";

            bt = n.toByteTree();
            btr = bt.getByteTreeReader();
            try {
                m = LargeInteger.unsafeLargeInteger(byteLength + 1, btr);
            } catch (final ArithmError ae) {
                m = null;
            }
            if (m != null) {
                throw new ArithmFormatException("Failed to fail!");
            }

            size++;
        }
    }

    /**
     * To string.
     *
     * @throws ArithmException If a test failed.
     */
    public void toStringC()
        throws ArithmException {
        LargeInteger n;

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            n = new LargeInteger(size, rs);

            String ns = n.toString(10);
            LargeInteger m = new LargeInteger(ns);
            assert m.equals(n) : "Failed to convert decimal!";

            ns = n.toString();
            m = new LargeInteger(ns, 16);
            assert m.equals(n) : "Failed to convert hexadecimal!";

            size++;
        }

        // Fail on non-decimal characters.
        try {
            n = new LargeInteger("a123");
        } catch (final ArithmError ae) {
            n = null;
        }
        if (n != null) {
            throw new ArithmException("Failed to fail on non-decimal "
                                      + "characters!");
        }

        // Fail on non-hexadecimal characters.
        try {
            n = new LargeInteger("g123", 16);
        } catch (final ArithmError ae) {
            n = null;
        }
        if (n != null) {
            throw new ArithmException("Failed to fail on non-hexadecimal "
                                      + "characters!");
        }
    }

    /**
     * To int value.
     */
    public void intC() {

        int k = -127;

        while (k < 128) {

            final LargeInteger n = new LargeInteger(k);
            assert n.intValue() == k : "Failed to convert to int!";
            k++;
        }
    }

    /**
     * Compare primitive arrays.
     *
     * @throws ArithmException If a test failed.
     */
    public void arrayCompareTo()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        int size = 1;
        final int bitLength = 100;

        while (!timer.timeIsUp()) {

            final LargeInteger[] x =
                LargeInteger.random(size, bitLength + size, rs);
            final LargeInteger[] y =
                LargeInteger.random(size, bitLength + size, rs);

            int c = 0;
            for (int i = 0; i < x.length; i++) {
                if (x[i].compareTo(y[i]) < 0) {
                    c = -1;
                    break;
                } else if (x[i].compareTo(y[i]) > 0) {
                    c = 1;
                    break;
                }
            }
            assert LargeInteger.compareTo(x, y) == c
                : "Comparison of different arrays failed!";

            assert LargeInteger.compareTo(x, x) == 0
                : "Comparison of identical arrays failed!";

            // Fail on arrays of different lengths.
            LargeInteger[] z =
                LargeInteger.random(size + 1, bitLength + size, rs);
            try {
                LargeInteger.compareTo(x, z);
            } catch (final ArithmError ae) {
                z = null;
            }
            if (z != null) {
                throw new ArithmException("Failed to fail on arrays of "
                                          + "different lengths!");
            }

            size++;
        }
    }

    /**
     * Excercise hashCode.
     */
    public void excHashCode() {
        final LargeInteger n = new LargeInteger(100, rs);
        n.hashCode();
    }

    /**
     * Fill primitive array.
     */
    public void fill() {
        final LargeInteger x = new LargeInteger(100, rs);
        final LargeInteger[] y = LargeInteger.fill(20, x);
        for (int i = 0; i < y.length; i++) {
            assert y[i].equals(x) : "Failed to fill!";
        }
    }

    /**
     * Comparison and equality.
     */
    public void compareToAndEquals() {

        final Timer timer = new Timer(testTime);
        int bitLength = 100;

        while (!timer.timeIsUp()) {

            final LargeInteger x = new LargeInteger(bitLength, rs);
            final LargeInteger y = new LargeInteger(bitLength, rs);
            final LargeInteger z = x.add(LargeInteger.ZERO);

            assert x.equals(z) : "Equality failed!";
            assert !x.equals(y) : "Inequality failed!";
            assert !x.equals(new Object()) : "Inequality failed!";

            assert x.compareTo(z) == 0 : "Comparison failed!";
            assert x.compareTo(y) != 0 : "Comparison failed!";

            bitLength++;
        }
    }

    /**
     * Component-wise equality of primitive arrays.
     *
     * @throws ArithmException If a test failed.
     */
    public void equalsAll()
        throws ArithmException {

        final Timer timer = new Timer(testTime);
        final int bitLength = 100;
        int size = 1;

        while (!timer.timeIsUp()) {

            // General case.
            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeInteger[] y = LargeInteger.random(size, bitLength, rs);

            final boolean[] c = LargeInteger.equalsAll(x, y);
            for (int i = 0; i < x.length; i++) {
                assert x[i].equals(y[i]) == c[i] : "Equality of arrays failed!";
            }

            // Fail on arrays of different lengths.
            LargeInteger[] z =
                LargeInteger.random(size + 1, bitLength + size, rs);
            try {
                LargeInteger.equalsAll(x, z);
            } catch (ArithmError ae) {
                z = null;
            }
            if (z != null) {
                throw new ArithmException("Failed to fail on arrays of "
                                          + "different lengths!");
            }

            size++;
        }
    }

    /**
     * Equality of primitive arrays.
     *
     * @throws ArithmException If a test failed.
     */
    public void arrayEquals()
        throws ArithmException {

        final Timer timer = new Timer(testTime);
        int size = 100;
        final int bitLength = 100;

        while (!timer.timeIsUp()) {

            final LargeInteger[] x =
                LargeInteger.random(size, bitLength + size, rs);
            final LargeInteger[] y =
                LargeInteger.random(size, bitLength + size, rs);

            assert !LargeInteger.equals(x, y)
                : "Failed to check inequality!";
            assert LargeInteger.equals(x, x)
                : "Failed to check enequality!";

            // Fail on arrays of different lengths.
            LargeInteger[] z =
                LargeInteger.random(size + 1, bitLength + size, rs);
            try {
                LargeInteger.equals(x, z);
            } catch (final ArithmError ae) {
                z = null;
            }
            if (z != null) {
                throw new ArithmException("Failed to fail on lengths of "
                                          + "different lengths!");
            }

            size++;
        }
    }

    /**
     * Verifies addition and negation of primitive arrays.
     */
    public void arrayArithmetic() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final LargeInteger[] x = LargeInteger.random(size, modulus, 0, rs);
            final LargeInteger[] n = LargeInteger.modNeg(x, modulus);

            final LargeInteger[] y = LargeInteger.modAdd(x, n, modulus);
            for (int i = 0; i < y.length; i++) {
                assert y[i].equals(LargeInteger.ZERO)
                    : "Failed to convert to int!";
            }
            size++;
        }
    }

    /**
     * To positive integer.
     *
     * @throws ArithmException If a test failed.
     */
    public void toPositive()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            byte[] bytes = rs.getBytes((size + 7) / 8);
            LargeInteger n = LargeInteger.toPositive(bytes);
            if (n.compareTo(LargeInteger.ZERO) < 0) {
                throw new ArithmException("Not non-zero!");
            }
            bytes = rs.getBytes((size + 7) / 8);
            n = LargeInteger.toPositive(bytes, 0, 1);
            if (n.compareTo(LargeInteger.ZERO) < 0) {
                throw new ArithmException("Not non-zero!");
            }

            size++;
        }
    }

    /**
     * Byte tree using primitive arrays.
     *
     * @throws ArithmException If a test failed.
     * @throws ArithmFormatException If a test failed.
     */
    public void arrayByteTree()
        throws ArithmException, ArithmFormatException {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final LargeInteger[] na = LargeInteger.random(size, 100, rs);

            final LargeInteger ub = LargeInteger.ONE.shiftLeft(100);

            // Array of arbitrary integers.
            ByteTreeBasic bt = LargeInteger.toByteTree(na);
            bt = LargeInteger.toByteTree(ub.toByteArray().length, na);
            ByteTreeReader btr = bt.getByteTreeReader();
            LargeInteger[] ma =
                LargeInteger.toLargeIntegers(na.length,
                                             btr,
                                             LargeInteger.ZERO,
                                             ub);
            assert Arrays.equals(na, ma)
                : "Failed to convert array of integers!";

            // Fail on too large integer outside the bound.
            na[0] = na[0].add(ub);
            try {
                bt = LargeInteger.toByteTree(ub.toByteArray().length, na);
                btr = bt.getByteTreeReader();
                ma = LargeInteger.toLargeIntegers(na.length,
                                                  btr,
                                                  LargeInteger.ZERO,
                                                  ub);
            } catch (final ArithmFormatException afe) {
                bt = null;
            }
            if (bt != null) {
                throw new ArithmException("Failed to fail on small integers!");
            }

            // Fail on too large integer outside the bound.
            na[0] = LargeInteger.ZERO;
            try {
                bt = LargeInteger.toByteTree(ub.toByteArray().length, na);
                btr = bt.getByteTreeReader();
                ma = LargeInteger.toLargeIntegers(na.length,
                                                  btr,
                                                  LargeInteger.ONE,
                                                  ub);
            } catch (final ArithmFormatException afe) {
                bt = null;
            }
            if (bt != null) {
                throw new ArithmException("Failed to fail on large integers!");
            }

            // Fail on malformed byte tree.
            try {
                bt = new ByteTree(new byte[1]);
                btr = bt.getByteTreeReader();
                ma = LargeInteger.toLargeIntegers(1,
                                                  btr,
                                                  LargeInteger.ONE,
                                                  ub);
            } catch (final ArithmFormatException afe) {
                bt = null;
            }
            if (bt != null) {
                throw new ArithmException("Failed to fail on large integers!");
            }

            size++;
        }
    }

    /**
     * Probable prime.
     */
    public void isProbablePrimeSmall() {

        final Timer timer = new Timer(testTime);

        int n = 256;

        while (!timer.timeIsUp()) {

            final LargeInteger sp = new LargeInteger(n);

            assert sp.isProbablePrime(rs)
                == sp.toBigInteger().isProbablePrime(50)
                : "Failed to check primality of small integers!";

            n++;
        }
    }

    /**
     * Verifies prime generation.
     */
    public void randomPrimeExact() {

        final Timer timer = new Timer(testTime);

        int size = 256;

        while (!timer.timeIsUp()) {

            final LargeInteger e =
                LargeInteger.randomPrimeExact(size, rs, 20);

            assert e.isProbablePrime(rs)
                : "Failed to generate prime!";

            size++;
        }
    }

    /**
     * Modular power.
     */
    public void modPow() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger basis = new LargeInteger(bitLength, rs);
            final LargeInteger exponent = new LargeInteger(bitLength, rs);

            final LargeInteger result = basis.modPow(exponent, modulus);

            final BigInteger correct =
                basis.toBigInteger().modPow(exponent.toBigInteger(),
                                            modulus.toBigInteger());

            assert result.toBigInteger().equals(correct)
                : "Failed modular power computation!";

            bitLength++;
        }
        final LargeInteger basis = new LargeInteger(512, rs);
        final LargeInteger exponent = new LargeInteger(512, rs);
        LargeInteger negModulus = LargeInteger.ZERO.sub(modulus);

        try {
            basis.modPow(exponent, negModulus);
        } catch (final ArithmError ae) {
            negModulus = null;
        }
        assert negModulus == null : "Failed to fail on negative modulus!";
    }

    /**
     * Modular power of primitive arrays.
     */
    public void arrayModPow1() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] bases =
                LargeInteger.random(size, bitLength, rs);
            final LargeInteger[] exponents =
                LargeInteger.random(size, bitLength, rs);

            final LargeInteger[] results =
                LargeInteger.modPow(bases, exponents, modulus);

            for (int i = 0; i < bases.length; i++) {
                assert bases[i].modPow(exponents[i], modulus).equals(results[i])
                    : "Taking modular powers failed!";
            }

            size++;
        }
    }

    /**
     * Modular power of primitive arrays with integers.
     */
    public void arrayModPow2() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger basis = new LargeInteger(bitLength, rs);
            final LargeInteger[] exponents =
                LargeInteger.random(size, bitLength, rs);

            final LargeInteger[] results =
                basis.modPow(exponents, modulus);

            final LargeInteger[] naiveResults =
                basis.naiveModPow(exponents, modulus);

            for (int i = 0; i < exponents.length; i++) {
                final LargeInteger res = basis.modPow(exponents[i], modulus);
                assert res.equals(results[i]) && res.equals(naiveResults[i])
                    : "Taking modular powers failed!";
            }

            size++;
        }
    }

    /**
     * Modular inner product.
     */
    public void modInnerProduct() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] integers1 =
                LargeInteger.random(size, bitLength, rs);
            final LargeInteger[] integers2 =
                LargeInteger.random(size, bitLength, rs);

            final LargeInteger res =
                LargeInteger.modInnerProduct(integers1, integers2, modulus);

            final LargeInteger[] tmp =
                LargeInteger.modMul(integers1, integers2, modulus);

            assert res.equals(LargeInteger.modSum(tmp, modulus))
                : "Failed to compute inner product!";

            size++;
        }
    }

    /**
     * Modular inversion of primitive array.
     *
     * @throws ArithmException If a test failed.
     */
    public void arrayModInv()
        throws ArithmException {

        final Timer timer = new Timer(testTime);
        final int bitLength = 100;
        int size = 1;

        LargeInteger prime = LargeInteger.ONE.shiftLeft(100);
        prime = prime.nextPrime(rs, 20);

        while (!timer.timeIsUp()) {

            final LargeInteger[] integers =
                LargeInteger.random(size, bitLength, rs);

            LargeInteger[] res = LargeInteger.modInv(integers, prime);
            for (int i = 0; i < integers.length; i++) {
                assert integers[i].modInv(prime).equals(res[i])
                    : "Taking modular inverses failed!";
            }

            // Modular inversion of array containing zero should fail.
            try {
                integers[0] = LargeInteger.ZERO;
                res = LargeInteger.modInv(integers, prime);
            } catch (final ArithmException ae) {
                res = null;
            }
            assert res == null : "Failed to fail on inversion of zero!";

            size++;
        }
    }

    /**
     * Modular power of primitive array using scalar.
     */
    public void arrayModPow3() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] bases =
                LargeInteger.random(size, bitLength, rs);
            final LargeInteger exponent = new LargeInteger(bitLength, rs);

            final LargeInteger[] results =
                LargeInteger.modPow(bases, exponent, modulus);

            for (int i = 0; i < bases.length; i++) {
                assert bases[i].modPow(exponent, modulus).equals(results[i])
                    : "Taking modular powers failed!";
            }

            size++;
        }
    }

    /**
     * Modular product of powers of primitive arrays.
     */
    public void modPowProd() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] bases = LargeInteger.random(size, 512, rs);
            final LargeInteger[] exponents = LargeInteger.random(size, 512, rs);

            for (int i = 0; i < size; i++) {
                bases[i] = bases[i].mod(modulus);
            }

            final LargeInteger res1 =
                LargeInteger.naiveModPowProd(bases, exponents, modulus);

            final LargeInteger res2 =
                LargeInteger.modPowProd(bases, exponents, modulus);

            assert res1.equals(res2)
                : "Failed to compute modular power product!";

            size++;
        }
    }

    /**
     * Modular product of elements of primitive array.
     */
    public void modProd() {

        final Timer timer = new Timer(testTime);

        int bitLength = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] x = LargeInteger.random(bitLength, 512, rs);

            LargeInteger res = LargeInteger.ONE;
            for (int i = 0; i < bitLength; i++) {
                res = res.mul(x[i]).mod(modulus);
            }

            assert LargeInteger.modProd(x, modulus).equals(res)
                : "Failed to compute modular product!";

            bitLength++;
        }
    }

    /**
     * Modular products of primitive array.
     */
    public void modProds() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);

            final LargeInteger[] res = new LargeInteger[x.length];
            final LargeInteger torig = new LargeInteger(bitLength, rs);
            LargeInteger t = torig;
            for (int i = 0; i < size; i++) {
                res[i] = t.mul(x[i]).mod(modulus);
                t = res[i];
            }

            assert LargeInteger.equals(res, LargeInteger.modProds(torig,
                                                                  x,
                                                                  modulus))
                : "Failed to compute modular products!";

            size++;
        }
    }

    /**
     * Modular sum of elements of primitive array.
     */
    public void modSum() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);

            LargeInteger res = LargeInteger.ZERO;
            for (int i = 0; i < x.length; i++) {
                res = res.add(x[i]).mod(modulus);
            }

            assert LargeInteger.modSum(x, modulus).equals(res)
                : "Failed to compute modular sum!";

            size++;
        }
    }

    /**
     * Modular product of primitive arrays, and primitive arrays with
     * scalar.
     */
    public void modMul() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            // Choose random elements
            final LargeInteger[] x = LargeInteger.random(size, bitLength, rs);
            final LargeInteger[] y = LargeInteger.random(size, bitLength, rs);

            final LargeInteger[] z = new LargeInteger[size];
            for (int i = 0; i < size; i++) {
                z[i] = x[i].mul(y[i]).mod(modulus);
            }

            LargeInteger[] res = LargeInteger.modMul(x, y, modulus);
            assert LargeInteger.equals(z, res)
                : "Failed to compute modular products!";

            final LargeInteger s = new LargeInteger(bitLength, rs);
            for (int i = 0; i < size; i++) {
                z[i] = x[i].mul(s).mod(modulus);
            }
            res = LargeInteger.modMul(x, s, modulus);
            assert LargeInteger.equals(z, res)
                : "Failed to compute modular products with scalar!";

            size++;
        }
    }

    /**
     * Computing next prime, including testing primality testing.
     */
    public void nextPrime() {

        final Timer timer = new Timer(testTime);

        int bitLength = 20;

        while (!timer.timeIsUp()) {

            final LargeInteger li = new LargeInteger(bitLength, rs);
            final LargeInteger prime = li.nextPrime(rs, 50);
            assert prime.toBigInteger().isProbablePrime(50)
                : "Next prime failed!";

            bitLength++;
        }
    }

    /**
     * Computing next safe prime, including testing safe primality
     * testing.
     *
     * @throws ArithmException If a test failed.
     */
    public void nextSafePrime()
        throws ArithmException {

        final Timer timer = new Timer(testTime);

        int bitLength = 20;

        // Test some trial division.
        assert !(new LargeInteger(2).isSafePrime(rs)) : "Trial failed 2!";
        assert !(new LargeInteger(3).isSafePrime(rs)) : "Trial failed 3!";
        assert new LargeInteger(5).isSafePrime(rs) : "Trial failed 5!";
        assert new LargeInteger(7).isSafePrime(rs) : "Trial failed 7!";
        assert !(new LargeInteger(9).isSafePrime(rs)) : "Trial failed 9!";

        while (!timer.timeIsUp()) {

            final LargeInteger li = new LargeInteger(bitLength, rs);
            LargeInteger prime = li.nextSafePrime(rs);

            assert prime.isSafePrime(rs) : "Failed to verify safe prime!";

            LargeInteger sub =
                prime.sub(LargeInteger.ONE).divide(LargeInteger.TWO);

            assert prime.toBigInteger().isProbablePrime(50)
                : "Safe prime is not prime!";
            assert sub.toBigInteger().isProbablePrime(50)
                : "Sub prime is not prime!";

            prime = li.randomSafePrime(40, rs);
            sub = prime.sub(LargeInteger.ONE).divide(LargeInteger.TWO);

            assert prime.toBigInteger().isProbablePrime(50)
                : "Safe prime is not prime!";
            assert sub.toBigInteger().isProbablePrime(50)
                : "Sub prime is not prime!";

            bitLength++;
        }
    }

    /**
     * Corner case of Jacobi symbol algorithm.
     */
    public void jacobiSymbol() {
        assert LargeInteger.jacobiSymbol(BigInteger.ZERO, BigInteger.ZERO) == 0
            : "Failed to fail on zero value!";
    }
}
