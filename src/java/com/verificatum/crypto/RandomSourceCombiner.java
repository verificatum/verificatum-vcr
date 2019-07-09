
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

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;
import com.verificatum.util.Functions;

/**
 * Implements a combiner of random sources. If the sources are
 * independent, then the output is at least as hard to distinguish
 * from random as any of its underlying sources. The combiner simply
 * takes the xor of the random sources it combines.
 *
 * @author Douglas Wikstrom
 */
public final class RandomSourceCombiner extends RandomSource {

    /**
     * Maximal number of sources that can be combined.
     */
    public static final int MAX_RND_SOURCES = 50;

    /**
     * Instances wrapped by this one.
     */
    RandomSource[] randomSources;

    /**
     * Creates an instance.
     *
     * @param randomSources Instances wrapped by this one.
     */
    public RandomSourceCombiner(final RandomSource... randomSources) {
        this.randomSources = randomSources;
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public RandomSourceCombiner(final ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            final int width = btr.getRemaining();
            if (width > MAX_RND_SOURCES) {
                throw new CryptoFormatException("Too many random sources!");
            }

            randomSources = new RandomSource[width];
            for (int i = 0; i < width; i++) {
                randomSources[i] =
                    Marshalizer.unmarshal_RandomSource(btr.getNextChild());
            }

        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @return Instance represented by the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static RandomSourceCombiner newInstance(final ByteTreeReader btr)
        throws CryptoFormatException {
        return new RandomSourceCombiner(btr);
    }

    // Documented in RandomSource.java

    @Override
    public void getBytes(final byte[] array) {
        synchronized (this) {
            randomSources[0].getBytes(array);

            // We xor the outputs of all the random sources.
            final byte[] tempArray = new byte[array.length];
            for (int i = 1; i < randomSources.length; i++) {

                randomSources[i].getBytes(tempArray);

                for (int j = 0; j < array.length; j++) {
                    array[j] ^= tempArray[j];
                }
            }
        }
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTreeBasic toByteTree() {
        final ByteTreeBasic[] byteTrees =
            new ByteTreeBasic[randomSources.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = Marshalizer.marshal(randomSources[i]);
        }
        return new ByteTreeContainer(byteTrees);
    }

    @Override
    public String humanDescription(final boolean verbose) {
        final StringBuilder sb = new StringBuilder();

        sb.append(Util.className(this, verbose));
        sb.append('(');
        sb.append(randomSources[0].humanDescription(verbose));
        for (int i = 1; i < randomSources.length; i++) {
            sb.append(", ");
            sb.append(randomSources[i].humanDescription(verbose));
        }
        sb.append(')');
        return sb.toString();
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
        if (!(obj instanceof RandomSourceCombiner)) {
            return false;
        }

        final RandomSourceCombiner combiner = (RandomSourceCombiner) obj;
        if (randomSources.length != combiner.randomSources.length) {
            return false;
        }
        for (int i = 0; i < randomSources.length; i++) {
            if (!randomSources[i].equals(combiner.randomSources[i])) {
                return false;
            }
        }
        return true;
    }
}
