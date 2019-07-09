
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

package com.verificatum.crypto;

import java.util.Arrays;

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;
import com.verificatum.util.Functions;


/**
 * Pseudo-random generator based on a hashfunction evaluated on a seed
 * concatenated with a counter, i.e., a natural construction of a PRG
 * from a pseudo-random function.
 *
 * @author Douglas Wikstrom
 */
public final class PRGHeuristic extends PRG {

    /**
     * Underlying hashfunction.
     */
    Hashfunction hashfunction;

    /**
     * Buffer holding the seed and the counter as bytes.
     */
    byte[] input;

    /**
     * Counter used as input to the hashfunction. This is increased
     * with each invocation of the hashfunction.
     */
    int counter;

    /**
     * Data to be output.
     */
    byte[] data;

    /**
     * Index to first unused byte in data.
     */
    int datapos;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Instructions for construction of an instance.
     * @return PRG represented by the input.
     *
     * @throws CryptoFormatException This is never thrown, but
     *  declared for consistency with the other PRGs.
     */
    public static PRGHeuristic newInstance(final ByteTreeReader btr)
        throws CryptoFormatException {
        try {
            final Hashfunction hashfunction =
                Marshalizer.unmarshal_Hashfunction(btr);

            return new PRGHeuristic(hashfunction);

        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed representation!", eioe);
        }
    }

    /**
     * Creates an instance using the given hashfunction.
     *
     * @param hashfunction Hashfunction on which the generator is
     * based.
     */
    public PRGHeuristic(final Hashfunction hashfunction) {
        this.hashfunction = hashfunction;
        input = new byte[minNoSeedBytes() + 4];
    }

    /**
     * Creates a seeded instance of the PRG based on SHA-256.
     *
     * @param seed Seed to the generator.
     */
    public PRGHeuristic(final byte[] seed) {
        this(new HashfunctionHeuristic("SHA-256"));
        setSeed(seed);
    }

    /**
     * Creates an unseeded, i.e., completely insecure, instance of the
     * PRG based on SHA-256.
     */
    public PRGHeuristic() {
        this(new byte[100]);
    }

    /**
     * Set the seed of the generator. If the input contains more bytes
     * than needed then they are xor-ed in a circular fashion.
     *
     * @param seed Seed to generator.
     */
    @Override
    public void setSeed(final byte[] seed) {

        // If the seed does not have enough bits, then we give up.
        if (minNoSeedBytes() > seed.length) {
            throw new CryptoError("Seed is too short!");
        }

        synchronized (this) {
            Arrays.fill(input, (byte) 0);
            counter = 0;
            for (int i = 0; i < seed.length; i++) {
                input[i % minNoSeedBytes()] ^= seed[i];
            }
            data = new byte[hashfunction.getOutputLength()];
            datapos = data.length;
        }
    }

    // Documented in com.verificatum.crypto.RandomSource.java.

    @Override
    public void getBytes(final byte[] array) {
        synchronized (this) {

            if (data == null) {
                throw new CryptoError("The PRG is not seeded yet!");
            }

            int index = 0;

            while (index < array.length) {

                final int len =
                    Math.min(array.length - index, data.length - datapos);

                if (len > 0) {

                    // Copy available output bytes.
                    System.arraycopy(data, datapos, array, index, len);
                    datapos += len;
                    index += len;

                } else {

                    // Plug in the current counter.
                    int i = minNoSeedBytes();
                    input[i++] = (byte) (counter >>> 24 & 0xff);
                    input[i++] = (byte) (counter >>> 16 & 0xff);
                    input[i++] = (byte) (counter >>> 8 & 0xff);
                    input[i++] = (byte) (counter & 0xff);

                    // Hash the updated input and update counters.
                    data = hashfunction.hash(input);

                    datapos = 0;
                    counter++;
                }
            }
        }
    }

    @Override
    public int minNoSeedBytes() {
        return hashfunction.getOutputLength() / 8;
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTreeBasic toByteTree() {
        return Marshalizer.marshal(hashfunction);
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose)
            + "(" + hashfunction.humanDescription(verbose) + ")";
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PRGHeuristic)) {
            return false;
        }
        return hashfunction.equals(((PRGHeuristic) obj).hashfunction);
    }
}
