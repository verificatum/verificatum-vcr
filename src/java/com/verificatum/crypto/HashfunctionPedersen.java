
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

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PFieldElement;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;
import com.verificatum.util.Functions;


/**
 * Implementation of Pedersen's fixed length hash function. This is
 * collision-free under the discrete logarithm assumption in the
 * underlying group.
 *
 * @author Douglas Wikstrom
 */
public final class HashfunctionPedersen implements HashfunctionFixedLength {

    /**
     * Maximal number of bases used.
     */
    public static final int MAX_WIDTH = 10;

    /**
     * Number of bits that can be input to the function.
     */
    int inputLength;

    /**
     * Number of bits output by the function.
     */
    int outputLength;

    /**
     * Maximal number of bytes that can be converted injectively into
     * an element of the field associated with the group over which we
     * compute.
     */
    int expLength;

    /**
     * Independent generators.
     */
    PGroupElement[] generators;

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Hashfunction represented by the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static HashfunctionPedersen newInstance(final ByteTreeReader btr,
                                                   final RandomSource rs,
                                                   final int certainty)
        throws CryptoFormatException {
        try {

            final PGroup pGroup =
                Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                rs,
                                                certainty);
            final ByteTreeReader gbtr = btr.getNextChild();
            final int width = Math.min(MAX_WIDTH, gbtr.getRemaining());
            final PGroupElement[] generators = pGroup.toElements(width, gbtr);

            return new HashfunctionPedersen(generators);

        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (final ArithmFormatException afe) {
            throw new CryptoFormatException("Can not interpret!", afe);
        }
    }

    /**
     * Creates an instance defined by the generators given as input.
     * This does not copy the input.
     *
     * @param generators Generators that define this instance.
     */
    public HashfunctionPedersen(final PGroupElement... generators) {
        this.generators = generators;
        init();
    }

    /**
     * Creates a random instance defined over the given group.
     *
     * @param pGroup Group over which the function is defined.
     * @param width Number of generators.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public HashfunctionPedersen(final PGroup pGroup,
                                final int width,
                                final RandomSource rs,
                                final int statDist) {

        // Generate independent group elements.
        generators = new PGroupElement[width];
        for (int i = 0; i < width; i++) {
            generators[i] = pGroup.randomElement(rs, statDist);
        }

        init();
    }

    /**
     * Completes the initialization of this instance.
     */
    protected void init() {
        // Compute input and output byte lengths.
        final PGroup pGroup = generators[0].getPGroup();
        expLength = pGroup.getPRing().getPField().getEncodeLength();
        inputLength = 8 * generators.length * expLength;
        outputLength = 8 * pGroup.getByteLength();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < generators.length; i++) {
            sb.append(generators[i].toString());
            if (i > 0) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    // Documented in FixedLengthHashfunction.java.

    @Override
    public byte[] hash(final byte[] input) {
        if (input.length > inputLength / 8) {
            throw new CryptoError("Input has wrong length!");
        }

        final PFieldElement[] exponents = new PFieldElement[generators.length];
        final PField pField = generators[0].getPGroup().getPRing().getPField();
        int offset = 0;
        for (int i = 0; i < exponents.length; i++) {
            final LargeInteger li =
                LargeInteger.toPositive(input, offset, expLength);
            exponents[i] = pField.toElement(li);
            offset += expLength;
        }

        return generators[0].getPGroup().expProd(generators, exponents)
            .toByteArray();
    }

    @Override
    public int getInputLength() {
        return inputLength;
    }

    @Override
    public int getOutputLength() {
        return outputLength;
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTreeBasic toByteTree() {

        final PGroup pGroup = generators[0].getPGroup();
        final ByteTreeBasic pGroupByteTree = Marshalizer.marshal(pGroup);
        final ByteTreeBasic generatorsByteTree = pGroup.toByteTree(generators);

        return new ByteTreeContainer(pGroupByteTree, generatorsByteTree);
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "("
            + generators[0].getPGroup().humanDescription(verbose)
            + ", width=" + generators.length + ")";
    }

    // Documented in Object.java

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashfunctionPedersen)) {
            return false;
        }
        return Arrays.equals(generators,
                             ((HashfunctionPedersen) obj).generators);
    }
}
