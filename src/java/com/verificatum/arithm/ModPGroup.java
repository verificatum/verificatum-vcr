
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
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.crypto.RandomOracle;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.ui.Util;


/**
 * An implementation of a prime order subgroup of the multiplicative
 * group modulo a prime.
 *
 * @author Douglas Wikstrom
 */
public final class ModPGroup extends PGroup {

    /**
     * Maximal number of bytes that can be encoded when using random
     * encoding.
     */
    public static final int RO_MAX_NO_BYTES = 1;

    /**
     * Represents usage of random encoding of messages. A message
     * <code>message</code> is encoded by evaluating
     * <code>SHA-256({@link #g}<sup>i</sup>)</code> for
     * <i>i=1,2,3,...</i> until the first couple of bytes equals
     * <code>message</code>. Only short messages, with at most
     * {@link #RO_MAX_NO_BYTES} can be encoded in this way.
     */
    public static final int RO_ENCODING = 0;

    /**
     * Represents usage of safe prime encoding (this is only possible
     * when the modulus is a safe prime). A message is converted to an
     * integer and encoded by multiplying with <i>-1</i> if needed to
     * make the message a quadratic residue. An element <i>x</i> is
     * decoded by taking the minimum of <i>x</i> and {@link #modulus}
     * -<i>x</i>.
     */
    public static final int SAFEPRIME_ENCODING = 1;

    /**
     * Represents that subgroup encoding is used. To encode a message,
     * padding bits are increased until the resulting integer is
     * contained in the subgroup. Decoding is done by truncating.
     */
    public static final int SUBGROUP_ENCODING = 2;

    /**
     * Bounds the size of {@link #coOrder} that can be used with
     * subgroup encoding. This (heuristically) implies a bound of the
     * number of attempts needed for encoding.
     */
    public static final int MAXIMAL_COGROUP_BITLEN = 10;

    /**
     * Bounds the byte length of a modulus.
     */
    public static final int MAX_MODULUS_BYTELENGTH = 5000;

    /**
     * Modulus of the multiplicative group.
     */
    LargeInteger modulus;

    /**
     * Bit length of the modulus.
     */
    int bitLength;

    /**
     * Number of bytes needed to represent the modulus.
     */
    int modulusByteLength;

    /**
     * Number of bytes needed to injectively map a group element to a
     * byte[].
     */
    int byteLength;

    /**
     * Standard generator of the group.
     */
    PGroupElement g;

    /**
     * Unit element in the group.
     */
    PGroupElement ONE; // NOPMD

    /**
     * Maximum number of bytes that may be encoded into a group
     * element.
     */
    int encodeLength;

    /**
     * Integer, 2<sup>{@link #encodeLength}</sup>, used to encode
     * <code>byte[]</code> as group elements when using
     * {@link #SUBGROUP_ENCODING} encoding.
     */
    LargeInteger addNum;

    /**
     * Order of full multiplicative group divided by the order of the
     * group, i.e., the index of group.
     */
    LargeInteger coOrder;

    /**
     * Encoding used.
     */
    int encoding;

    /**
     * Determines the maximal number of encoding attempts.
     */
    int encodingAttempts;

    /**
     * Returns the encoding of this group.
     *
     * @return Encoding scheme of this group.
     */
    @CoberturaIgnore
    public int getEncoding() {
        return encoding;
    }

    /**
     * Constructs an instance corresponding to the input
     * representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Group represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * valid instructions for creating an instance.
     */
    public static PGroup newInstance(final ByteTreeReader btr,
                                     final RandomSource rs,
                                     final int certainty)
        throws ArithmFormatException {
        try {

            final LargeInteger modulus =
                new LargeInteger(MAX_MODULUS_BYTELENGTH, btr.getNextChild());
            final LargeInteger order =
                new LargeInteger(MAX_MODULUS_BYTELENGTH, btr.getNextChild());
            final LargeInteger gli =
                new LargeInteger(MAX_MODULUS_BYTELENGTH, btr.getNextChild());

            final int encoding = btr.getNextChild().readInt();

            return new ModPGroup(modulus, order, gli, encoding, rs, certainty);

        } catch (final EIOException eioe) {
            throw new ArithmFormatException("Invalid modulus!", eioe);
        }
    }

    /**
     * Initializes the group. It is the responsibility of the
     * programmer to make sure that the modulus is a safe prime.
     *
     * @param modulus Safe prime modulus.
     * @param gli Generator of group.
     * @param encoding Encoding used with this group.
     *
     * @throws ArithmFormatException If the size of the modulus is too
     * small.
     */
    protected void unsafeInit(final LargeInteger modulus,
                              final LargeInteger gli,
                              final int encoding)
        throws ArithmFormatException {

        this.modulus = modulus;

        this.bitLength = modulus.bitLength();

        // The identity element of the group of squares.
        this.ONE = new ModPGroupElement(this, LargeInteger.ONE);
        this.g = new ModPGroupElement(this, gli);

        this.modulusByteLength = modulus.toByteArray().length;
        this.byteLength = ONE.toByteArray().length;

        this.encoding = encoding;

        switch (encoding) {
        case RO_ENCODING:

            // It seems unreasonable to try to encode more bytes by
            // brute force.
            encodeLength = RO_MAX_NO_BYTES;
            addNum = null;
            break;

        case SAFEPRIME_ENCODING:

            // Make room for an int storing the length of the data and
            // make sure that the data fits using safe-prime encoding.
            encodeLength = (bitLength - 2) / 8 - 4;
            addNum = null;
            break;

        case SUBGROUP_ENCODING:

            // The number of padding bytes needed to encode elements
            // in the group. We use one additional byte to ensure
            // successful encoding with high probability.
            final int paddingBytes = (coOrder.bitLength() + 7) / 8 + 1;

            // We keep four additional bytes for storing the actual
            // length of an encoded byte[] (this could be made more
            // tight).
            encodeLength = bitLength / 8 - paddingBytes - 4;

            if (encodeLength <= 0) {
                throw new ArithmFormatException("Too small modulus!");
            }

            // Used to encode a byte[] as a group element.
            addNum = LargeInteger.ONE.shiftLeft((encodeLength + 4) * 8);

            encodingAttempts = (1 << (8 * paddingBytes)) - 1;

            break;

        default:
            throw new ArithmFormatException("Unknown encoding!");
        }
    }

    /**
     * Derive a "heuristically random" generator in the group defined
     * by the given modulus. This only works for safe-prime moduli.
     *
     * @param modulus Modulus defining the group.
     * @return Derived generator.
     */
    LargeInteger deriveGenerator(final LargeInteger modulus) {

        // Generate unpredictable bits from the modulus using SHA-256.
        final Hashfunction roHashfunction =
            new HashfunctionHeuristic("SHA-256");

        // Generate more bits than needed to get a "randomly"
        // distributed generator
        final int outputLength = 2 * modulus.bitLength();
        final RandomOracle ro = new RandomOracle(roHashfunction, outputLength);
        final byte[] input = ro.hash(modulus.toByteArray());
        input[0] = 0;

        // Make sure we get a generator.
        return new LargeInteger(input).modPow(LargeInteger.TWO, modulus);
    }

    /**
     * Constructs an instance where the modulus has the given
     * bit-length and the safe prime is taken from a fixed table. The
     * lower and upper bounds of the bitsize are {@link
     * SafePrimeTable#MIN_BIT_LENGTH} and {@link
     * SafePrimeTable#MAX_BIT_LENGTH}.
     *
     * @param bitLen Bit length of modulus.
     *
     * @throws ArithmFormatException If the input bit length falls
     * outside the range of bit lengths for which primes are
     * tabulated.
     */
    public ModPGroup(final int bitLen) throws ArithmFormatException {
        final LargeInteger safePrime = SafePrimeTable.safePrime(bitLen);
        final PField pField =
            new PField(safePrime.sub(LargeInteger.ONE).shiftRight(1));
        super.init(pField);

        final LargeInteger gli = deriveGenerator(safePrime);
        unsafeInit(safePrime, gli, SAFEPRIME_ENCODING);
        coOrder = LargeInteger.TWO;
    }

    /**
     * Constructs a group with safe prime modulus and associated
     * generator of the given size derived from the given bytes. The
     * modulus is derived as the first safe prime greater than an
     * integer of the proper bit length derived from the given bytes.
     * The generator is taken to be the modular square of an integer
     * is somewhat larger than the modulus. This constructor relies on
     * the input being random.
     *
     * @param bitLen Bit size of the order of the group.
     * @param randomBytes "Random" bytes from which the modulus and
     * generator are derived. This must be at least 2 * (bitLen + 7) /
     * 8 + 10 bytes.
     * @param rs Random source used to probabilistically check
     * primality of the modulus.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     *
     * @throws ArithmFormatException If the input bytes can not be
     *  converted to an instance.
     */
    public ModPGroup(final int bitLen,
                     final byte[] randomBytes,
                     final RandomSource rs,
                     final int certainty)
        throws ArithmFormatException {
        final int byteLen = (bitLen + 7) / 8;
        final int minTotal = 2 * byteLen + 10;

        if (randomBytes.length < minTotal) {
            throw new ArithmError("Too few random bytes! ("
                                  + randomBytes.length + " < "
                                  + minTotal + ")");
        }

        // Derive safe prime.
        final byte[] safePrimeBytes =
            Arrays.copyOfRange(randomBytes, 0, byteLen);
        LargeInteger safePrime = LargeInteger.toPositive(safePrimeBytes);
        safePrime = safePrime.mod(LargeInteger.TWO.shiftLeft(bitLen + 1));
        safePrime = safePrime.setBit(bitLen); // bitLen-1 bits in modulus.

        // The probability that the next safe prime is too far away to
        // find is negligible, so this is safe.
        safePrime = safePrime.nextSafePrime(rs, certainty);

        // Derive generator.
        final byte[] gliBytes =
            Arrays.copyOfRange(randomBytes, byteLen, randomBytes.length);
        LargeInteger gli = LargeInteger.toPositive(gliBytes);
        gli = gli.modPow(LargeInteger.TWO, safePrime);

        // Set associated field.
        final PField pField =
            new PField(safePrime.shiftRight(1), rs, certainty);
        super.init(pField);

        unsafeInit(safePrime, gli, SAFEPRIME_ENCODING);
    }

    /**
     * Verify that the bit lengths and encoding are compatible.
     *
     * @param bitLen Bit length of underlying modulus.
     * @param obitLen Binary logarithm of the order of the group - 1.
     * @param encoding Encoding used to encode messages into group
     * elements.
     * @throws ArithmFormatException If the parameters are invalid.
     */
    void sanityCheck(final int bitLen, final int obitLen, final int encoding)
        throws ArithmFormatException {

        if (bitLen <= obitLen) {
            throw new ArithmFormatException("Bitlength of modulus must be "
                                            + "larger than bitlength of "
                                            + "order!");
        }

        if (encoding == SAFEPRIME_ENCODING) {
            throw new ArithmFormatException("Safe prime encoding can not be "
                                            + "used for non-safe primes!");
        }

        if (encoding == SUBGROUP_ENCODING
            && bitLen - obitLen > MAXIMAL_COGROUP_BITLEN) {

            final String e =
                "The order is too small compared to the modulus to "
                + "allow encoding messages in the subgroup. Choose random "
                + "encoding or change the bitlengths!";
            throw new ArithmFormatException(e);
        }
    }

    /**
     * Creates a group with a <code>bitLen</code>-bit modulus and a
     * group of order between 2<sup><code>obitLen</code></sup> and
     * 2<sup> <code>obitLen</code>+1</sup>, using the given encoding.
     *
     * @param bitLen Bit length of underlying modulus.
     * @param obitLen Binary logarithm of the order of the group - 1.
     * @param encoding Encoding used to encode messages into group
     * elements.
     * @param rs Random source used to probabilistically check
     * primality of the modulus.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     *
     * @throws ArithmFormatException If the given parameters are
     * inconsistent, e.g., using a non-safe prime and safe-prime
     * encoding.
     */
    public ModPGroup(final int bitLen,
                     final int obitLen,
                     final int encoding,
                     final RandomSource rs,
                     final int certainty)
        throws ArithmFormatException {

        sanityCheck(bitLen, obitLen, encoding);

        LargeInteger order = null;

        LargeInteger theModulus = null;
        while (theModulus == null) {

            // Generate random prime of right size.
            do {

                order = new LargeInteger(obitLen, rs);
                order = order.setBit(obitLen - 1);
                order = order.nextPrime(rs, certainty);

            // UNCOVERABLE (Defensive programming. Extremely unlikely.)
            } while (order.bitLength() > obitLen);

            // We add twice the order in each iteration below.
            final LargeInteger order2 = order.shiftLeft(1);

            // Starting point of our search. We pick a random number
            // if the order is small compared to the modulus.
            if (bitLen - obitLen > MAXIMAL_COGROUP_BITLEN) {

                // Pick a random even number of suitable bitlength
                coOrder = new LargeInteger(bitLen - obitLen + 1, rs);
                coOrder = coOrder.clearBit(0);

                // Compute product and add one.
                theModulus = order.mul(coOrder).add(LargeInteger.ONE);

            } else {

                theModulus =
                    order.shiftLeft(bitLen - obitLen).add(LargeInteger.ONE);
            }

            // Repeatedly add twice the order and check for primality,
            // until we find a prime or the result has too many bits.
            for (;;) {

                if (theModulus.bitLength() > bitLen) {
                    theModulus = null;
                    break;
                }

                if (theModulus.isProbablePrime(rs, certainty)) {
                    break;
                }
                theModulus = theModulus.add(order2);
            }

        }

        coOrder = null;
        try {
            coOrder = theModulus.sub(LargeInteger.ONE).divide(order);

        // UNCOVERABLE (Never division by zero.)
        } catch (final ArithmException ae) {
            throw new ArithmError("This is a bug!", ae);
        }
        LargeInteger gli = null;

        // Generate random generator.
        do {

            gli = new LargeInteger(2 * bitLen, rs);
            gli = gli.modPow(coOrder, theModulus);

        // UNCOVERABLE (Defensive programming. Extremely unlikely.)
        } while (gli.equals(LargeInteger.ZERO) || gli.equals(LargeInteger.ONE));

        final PField pField = new PField(order, rs, certainty);
        super.init(pField);

        unsafeInit(theModulus, gli, encoding);
    }

    /**
     * Constructs a group with safe prime modulus and associated
     * generator of the given size derived from the given random
     * source. The modulus is derived as the first safe prime greater
     * than an integer of the proper bit length derived from the
     * random source. The generator is taken to be the modular square
     * of an integer is somewhat larger than the modulus.
     *
     * @param bitLen Bit length of underlying modulus.
     * @param rs Source of randomness.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     *
     * @throws ArithmFormatException If the input bit length falls
     *  outside the range of bit lengths for which primes
     *  are tabulated.
     */
    public ModPGroup(final int bitLen,
                     final RandomSource rs,
                     final int certainty)
        throws ArithmFormatException {
        this(bitLen, rs.getBytes(3 * ((bitLen + 7) / 8)), rs, certainty);
    }

    /**
     * Verify the parameters of the group.
     *
     * @param modulus Modulus.
     * @param order Order of group.
     * @param coOrder Co-order of group.
     * @param gli Integer representative of generator of group.
     * @param rs Random source used to probabilistically check
     * primality of the modulus.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     *
     * @throws ArithmFormatException If the given parameters are
     *  inconsistent.
     */
    private void sanityCheck(final LargeInteger modulus,
                             final LargeInteger order,
                             final LargeInteger coOrder,
                             final LargeInteger gli,
                             final RandomSource rs,
                             final int certainty)
        throws ArithmFormatException {

        if (modulus.compareTo(LargeInteger.ONE) <= 0) {
            throw new ArithmFormatException("The modulus is not positive!");
        }

        // Verify that the modulus is prime.
        if (!modulus.isProbablePrime(rs, certainty)) {
            throw new ArithmFormatException("The modulus is not prime!");
        }

        // Verify that the modulus and the order match.
        if (!order.mul(coOrder).add(LargeInteger.ONE).equals(modulus)) {
            throw new ArithmFormatException("Incompatible module and order!");
        }

        // Make sure that safe prime encoding is only used with an
        // integer that *could* be a safe prime.
        if (encoding == SAFEPRIME_ENCODING
            && !coOrder.equals(LargeInteger.TWO)) {
            final String e =
                "Attempting to use safe prime encoding using a non-safe prime!";
            throw new ArithmFormatException(e);
        }

        // Check that the generator candidate is canonically reduced
        // and non-zero and non-one.
        if (gli.compareTo(LargeInteger.TWO) < 0
            || modulus.compareTo(gli) <= 0) {
            final String s = "Generator is not reduced canonically!";
            throw new ArithmFormatException(s);
        }
    }

    /**
     * Verify that the encoding is compatible with the parameters of
     * the group.
     *
     * @param modulus Modulus.
     * @param order Order of group.
     * @param gli Integer representative of generator of group.
     * @param encoding Encoding used to encode messages into group
     * elements.
     * @throws ArithmFormatException If the encoding is not compatible
     * with the parameters of the group.
     */
    void sanityCheck(final LargeInteger modulus,
                     final LargeInteger order,
                     final LargeInteger gli,
                     final int encoding)
        throws ArithmFormatException {

        // Make sure that subgroup encoding is not used with to large
        // co-groups.
        if (encoding == SUBGROUP_ENCODING
            && modulus.bitLength() - order.bitLength()
            > MAXIMAL_COGROUP_BITLEN) {

            final String e =
                "A co-group of order with bit length more than "
                + MAXIMAL_COGROUP_BITLEN
                + " is incompatible with encoding arbitrary messages in the "
                + "subgroup!";
            throw new ArithmFormatException(e);
        }

        // Check that the generator is contained in this group.
        if (encoding == SAFEPRIME_ENCODING && gli.legendre(modulus) != 1) {

            throw new ArithmFormatException("Generator is not a square!");

        } else if (encoding == SUBGROUP_ENCODING
                   && !gli.modPow(order, modulus).equals(LargeInteger.ONE)) {

            final String e = "Generator is not contained in group!";
            throw new ArithmFormatException(e);
        }
    }

    /**
     * Initializes the group.
     *
     * @param modulus Modulus.
     * @param order Order of group.
     * @param gli Integer representative of generator of group.
     * @param encoding Encoding used to encode messages into group
     * elements.
     * @param rs Random source used to probabilistically check
     * primality of the modulus.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     *
     * @throws ArithmFormatException If the given parameters are
     *  inconsistent.
     */
    public ModPGroup(final LargeInteger modulus,
                     final LargeInteger order,
                     final LargeInteger gli,
                     final int encoding,
                     final RandomSource rs,
                     final int certainty)
        throws ArithmFormatException {

        try {
            coOrder = modulus.sub(LargeInteger.ONE).divide(order);

        } catch (final ArithmException ae) {
            throw new ArithmError("This is a bug!", ae);
        }

        // Set associated field. This checks that the order is prime.
        final PField pField = new PField(order, rs, certainty);
        super.init(pField);

        sanityCheck(modulus, order, coOrder, gli, rs, certainty);
        sanityCheck(modulus, order, gli, encoding);

        unsafeInit(modulus, gli, encoding);
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the given integer represents a group element or not.
     *
     * @param value Representative of candidate group element.
     * @return Result of test.
     */
    public boolean contains(final LargeInteger value) {
        if (value.compareTo(LargeInteger.ZERO) <= 0
            || modulus.compareTo(value) <= 0) {
            return false;
        }
        if (encoding == SAFEPRIME_ENCODING) {
            return value.legendre(modulus) == 1;
        } else {
            return value.modPow(getElementOrder(), modulus)
                .equals(LargeInteger.ONE);
        }
    }

    /**
     * Returns the modulus of this instance.
     *
     * @return Modulus of this instance.
     */
    @CoberturaIgnore
    public LargeInteger getModulus() {
        return modulus;
    }

    /**
     * Creates a group element corresponding to the given integer. It
     * is the responsibility of the programmer to ensure that the
     * input is a canonically reduced integer representative of a
     * group element.
     *
     * @param value Underlying integer representative.
     * @return The group element of which the input is a
     * representative.
     */
    public PGroupElement toElement(final LargeInteger value) {
        return new ModPGroupElement(this, value);
    }

    /**
     * Converts an array of integers to the corresponding list of
     * group elements. It is the responsibility of the programmer to
     * ensure that each integer in the input is a canonically reduced
     * integer representative of a group element.
     *
     * @param lia Integers to be converted.
     * @return Corresponding group elements.
     */
    protected PGroupElement[] toElements(final LargeInteger[] lia) {
        final PGroupElement[] result = new PGroupElement[lia.length];
        for (int i = 0; i < lia.length; i++) {
            result[i] = new ModPGroupElement(this, lia[i]);
        }
        return result;
    }

    /**
     * Converts the input elements to their integer representatives.
     *
     * @param elements Elements to be converted.
     * @return Integers representing the input elements.
     */
    protected LargeInteger[] toLargeIntegers(final PGroupElement[] elements) {
        final LargeInteger[] integers = new LargeInteger[elements.length];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = ((ModPGroupElement) elements[i]).value;
        }
        return integers;
    }

    // Documented in PGroup.java.

    @Override
    public PGroupElementArray randomElementArray(final int size,
                                                 final RandomSource rs,
                                                 final int statDist) {
        return new ModPGroupElementArray(this, size, rs, statDist);
    }

    @CoberturaIgnore
    @Override
    public int getByteLength() {
        return byteLength;
    }

    @Override
    public PGroupElement toElement(final ByteTreeReader btr)
        throws ArithmFormatException {
        return new ModPGroupElement(this, btr, true);
    }

    @Override
    public PGroupElement unsafeToElement(final ByteTreeReader btr) {
        try {
            return new ModPGroupElement(this, btr, false);
        } catch (final ArithmFormatException afe) {
            throw new ArithmError("Not a group element!", afe);
        }
    }

    @Override
    public PGroupElement encode(final byte[] byteArray,
                                final int startIndex,
                                final int length) {
        return new ModPGroupElement(this, byteArray, startIndex, length);
    }

    @Override
    public PGroupElement randomElement(final RandomSource rs,
                                       final int statDist) {

        LargeInteger li = new LargeInteger(modulus, statDist, rs);
        li = li.modPow(coOrder, modulus);

        return new ModPGroupElement(this, li);
    }

    @CoberturaIgnore
    @Override
    public PGroupElement getg() {
        return g;
    }

    @CoberturaIgnore
    @Override
    public PGroupElement getONE() {
        return ONE;
    }

    @CoberturaIgnore
    @Override
    public int getEncodeLength() {
        return encodeLength;
    }

    @CoberturaIgnore
    @Override
    public String toString() {
        return modulus.toString(16) + ":" + getElementOrder().toString(16)
            + ":encoding(" + encoding + ")";
    }

    @Override
    public PGroupElement expProd(final PGroupElement[] bases,
                                 final PRingElement[] exponents) {
        final LargeInteger[] liExponents =
            ((PField) pRing).toLargeIntegers(exponents);

        final LargeInteger li = LargeInteger.modPowProd(toLargeIntegers(bases),
                                                        liExponents,
                                                        modulus);
        return new ModPGroupElement(this, li);
    }

    @Override
    public PGroupElementArray
        toElementArray(final PGroupElement[] elements) {
        return new ModPGroupElementArray(this, elements);
    }

    @Override
    public PGroupElementArray
        toElementArray(final PGroupElementArray... arrays) {
        return new ModPGroupElementArray(this, arrays);
    }

    @Override
    public PGroupElementArray toElementArray(final int size,
                                             final PGroupElement element) {
        return new ModPGroupElementArray(this, size, element);
    }

    @Override
    public PGroupElementArray toElementArray(final int size,
                                             final ByteTreeReader btr)
        throws ArithmFormatException {
        return new ModPGroupElementArray(this, size, btr);
    }

    @Override
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final PRingElement[] exponents) {
        final LargeInteger[] liExponents =
            ((PField) pRing).toLargeIntegers(exponents);
        return toElements(LargeInteger.modPow(toLargeIntegers(bases),
                                              liExponents,
                                              modulus));
    }

    @Override
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final PRingElement exponent) {
        return toElements(LargeInteger.modPow(toLargeIntegers(bases),
                                              ((PFieldElement) exponent).value,
                                              modulus));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ModPGroup)) {
            return false;
        }
        final ModPGroup mpg = (ModPGroup) obj;
        return modulus.equals(mpg.modulus) && encoding == mpg.encoding;
    }

    // Documented in Marshalizable.java

    @Override
    public String humanDescription(final boolean verbose) {

        final StringBuilder descr = new StringBuilder();

        switch (encoding) {
        case SAFEPRIME_ENCODING:
            descr.append("safe-prime modulus=2*order+1. order bit-length = ");
            descr.append(getElementOrder().bitLength());
            break;
        case RO_ENCODING:
            descr.append("random encoding, ");
            break;
        case SUBGROUP_ENCODING:
            descr.append("modulus=k*order+1. modulus bit-length = "
                         + modulus.bitLength() + ", order bit-length = "
                         + getElementOrder().bitLength());
            break;
        default:
            throw new ArithmError("Unknown encoding!");
        }

        return Util.className(this, verbose) + "(" + descr.toString() + ")";
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(modulus.toByteTree(),
                                     getElementOrder().toByteTree(),
                                     g.toByteTree(),
                                     ByteTree.intToByteTree(encoding));
    }
}
