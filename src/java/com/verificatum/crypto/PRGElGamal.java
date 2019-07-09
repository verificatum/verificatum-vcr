
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.LargeIntegerFixModPowTab;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.ui.Util;
import com.verificatum.util.Functions;


/**
 * A natural provably secure pseudo-random generator based on the
 * Decision Diffie-Hellman assumption over the group of squares modulo
 * a safe prime.
 *
 * <p>
 *
 * It interprets the seed as a list
 * (<i>g<sub>0</sub>,...,g<sub>k</sub>,r</i>) of generators
 * <i>g<sub>i</sub></i> and <i>r</i> containing <code>statDist</code>
 * more bits than the safe prime modulus. Here <code>statDist</code>
 * decides the statistical error when the random bits are turned into
 * these elements. In fact, it first recursively calls itself with the
 * seed for <i>k=1</i>. Then it uses the recursive PRG to generate
 * sufficient randomness for the given <i>k</i>.
 *
 * <p>
 *
 * In each iteration it computes a tuple of the form:
 *
 * <p>
 *
 * (<i>u,v<sub>1</sub>,..,v<sub>k</sub>)= (g<sub>0</sub><sup>r</sup>,
 * g<sub>1</sub><sup>r</sup>,..., g<sub>k</sub><sup>r</sup></i>).
 *
 * <p>
 *
 * Using the observation that
 * <i>min(g<sub>i</sub>,q-g<sub>i</sub>)</i> is randomly distributed
 * in <i>Z<sub>q</sub></i> if <i>g<sub>i</sub></i> is a random square,
 * we may view the above as a pseudo-random vector over
 * <i>Z<sub>q</sub><sup>k+1</sup></i>, where <i>q</i> is the order of
 * the group of squares. It is then easy to extract (almost) uniformly
 * distributed bit strings by ignoring the most significant bits of
 * each element.
 *
 * <p>
 *
 * In each iteration <i>g<sub>0</sub><sup>r</sup></i> is stored (and
 * kept secret) to be used as the exponent <i>r</i> in the next
 * iteration.
 *
 * @author Douglas Wikstrom
 */
public final class PRGElGamal extends PRG {

    /**
     * Upper bound for any modulus.
     */
    static final int MAX_MODULUS_BYTELENGTH = 50 * 1024;

    /**
     * Number of random generators used.
     */
    public static final int DEFAULT_WIDTH = 10;

    /**
     * Default statistical distance parameter.
     */
    public static final int DEFAULT_STATDIST = 100;

    /**
     * Safe prime modulus.
     */
    LargeInteger modulus;

    /**
     * Order of group of squares modulo the safe prime modulus.
     */
    LargeInteger groupOrder;

    /**
     * Standard generator is stored at index 0 and on the remaining
     * indices random generators are stored, but they are stored in a
     * form that enables fixed base exponentiation.
     */
    LargeIntegerFixModPowTab[] g;

    /**
     * Buffer containing randomness generated so far that may be used
     * by applications.
     */
    List<byte[]> buffer;

    /**
     * Pseudo random exponent used in the next iteration of the random
     * generator.
     */
    LargeInteger r;

    /**
     * Decides the statistical error.
     */
    int statDist;

    /**
     * Constructs an instance corresponding to the input
     * representation.
     *
     * @param btr Representation of an instance.
     * @return Instance corresponding to the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static PRGElGamal newInstance(final ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            final LargeInteger modulus =
                new LargeInteger(MAX_MODULUS_BYTELENGTH, btr.getNextChild());
            final int width = btr.getNextChild().readInt();
            final int statDist = btr.getNextChild().readInt();

            return new PRGElGamal(modulus, width, statDist);
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (final CryptoError ce) {
            throw new CryptoFormatException("Can not interpret!", ce);
        } catch (final ArithmFormatException afe) {
            throw new CryptoFormatException("Can not interpret!", afe);
        }
    }

    /**
     * Creates a new instance with the default width.
     *
     * @param modulus Safe prime modulus of the group.
     * @param statDist Statistical error.
     */
    public PRGElGamal(final LargeInteger modulus, final int statDist) {
        this(modulus, DEFAULT_WIDTH, statDist);
    }

    /**
     * Creates a new instance with the given width. It is the
     * responsibility of the programmer to ensure that the modulus is
     * a safe prime.
     *
     * @param modulus Safe prime modulus of the group.
     * @param width Number of generators used.
     * @param statDist Decides the statistical error.
     */
    public PRGElGamal(final LargeInteger modulus,
                      final int width,
                      final int statDist) {

        if (LargeInteger.ZERO.compareTo(modulus) >= 0) {
            throw new CryptoError("Non-positive modulus!");
        }

        if (!modulus.isSafePrime(new PRGHeuristic())) {
            throw new CryptoError("Modulus is not a safe prime!");
        }

        this.modulus = modulus;

        try {
            this.groupOrder =
                modulus.sub(LargeInteger.ONE).divide(LargeInteger.TWO);

        // UNCOVERABLE (Always possible given the above.)
        } catch (final ArithmException ae) {
            throw new CryptoError("Invalid modulus!", ae);
        }

        if (width < 2) {
            throw new CryptoError("Invalid width! (must be at least 2)");
        }

        // Make room for generators.
        this.g = new LargeIntegerFixModPowTab[width];
        Arrays.fill(g, null);

        this.statDist = statDist;

        // null is used as a flag to indicate if a seed has been set
        // or not.
        this.r = null;
    }

    /**
     * Returns the minimal number of seed bytes needed for the given
     * initialization parameters.
     *
     * @param modulus Safe prime modulus.
     * @param statDist Decides the statistical distance from uniform.
     * @return Number of seed bytes needed for the given
     *         initialization parameters.
     */
    public static int minNoSeedBytes(final LargeInteger modulus,
                                     final int statDist) {
        return minExponentBytes(modulus, statDist) // random exponent
                                                   // r
            + 2 * minGeneratorBytes(modulus, statDist); // indep.
        // generators
    }

    /**
     * Returns the minimal number of bytes needed for one random
     * exponent.
     *
     * @param modulus Safe prime modulus.
     * @param statDist Decides the statistical distance from uniform.
     * @return Minimal number of bytes needed for one random exponent.
     */
    public static int minExponentBytes(final LargeInteger modulus,
                                       final int statDist) {
        return (modulus.bitLength() + statDist + 7) / 8;
    }

    /**
     * Returns the minimal number of bytes needed for one random
     * generator.
     *
     * @param modulus Safe prime modulus.
     * @param statDist Decides the statistical distance from uniform.
     * @return Minimal number of bytes needed for one random
     *         generator.
     */
    public static int minGeneratorBytes(final LargeInteger modulus,
                                        final int statDist) {
        return (modulus.bitLength() + statDist + 7) / 8;
    }

    /**
     * Returns the minimal number of seed bytes needed for this PRG.
     *
     * @return Number of seed bytes needed for the given
     *         initialization parameters.
     */
    @Override
    public int minNoSeedBytes() {
        return minNoSeedBytes(modulus, statDist);
    }

    // This is documented in PRG.java
    @Override
    public void setSeed(final byte[] seedBytes) {
        synchronized (this) {

            // If the seed does not have enough bits, then we give up.
            if (minNoSeedBytes() > seedBytes.length) {
                throw new CryptoError("Seed is too short!");
            }

            // Create buffer.
            this.buffer = new ArrayList<byte[]>();

            free();

            final int fixModPowWidth = 15;

            // If we have width 2, then we can use the seed directly.
            if (g.length == 2) {

                int offset = 0;
                int len;

                // Extract random exponent.
                len = minExponentBytes(modulus, statDist);
                final byte[] exponentBytes = new byte[1 + len];
                System.arraycopy(seedBytes, offset, exponentBytes, 1, len);
                exponentBytes[0] = 0;
                this.r = new LargeInteger(exponentBytes);
                offset += len;

                // Extract random generators.
                len = minGeneratorBytes(modulus, statDist);
                final byte[] generatorBytes = new byte[1 + len];
                for (int i = 0; i < 2; i++) {
                    System.arraycopy(seedBytes, offset, generatorBytes, 1, len);
                    generatorBytes[0] = 0;
                    final LargeInteger gi = new LargeInteger(generatorBytes);

                    this.g[i] =
                        new LargeIntegerFixModPowTab(gi,
                                                     modulus.bitLength(),
                                                     fixModPowWidth, modulus);
                    offset += len;
                }

                // If we have width > 2, then we boot strap
                // ourselves. We create a new instance with width 2,
                // and use this instance as a source of the longer
                // seed we need due to our greater width.
            } else {

                final PRG prg = new PRGElGamal(modulus, 2, statDist);
                prg.setSeed(seedBytes);

                // Extract random exponent.
                this.r =
                    new LargeInteger(groupOrder.bitLength() + statDist, prg);

                // Extract random generators.
                for (int i = 0; i < g.length; i++) {
                    LargeInteger gi = new LargeInteger(modulus.bitLength()
                                                       + statDist, prg);
                    gi = gi.modPow(LargeInteger.TWO, modulus);
                    this.g[i] =
                        new LargeIntegerFixModPowTab(gi,
                                                     modulus.bitLength(),
                                                     fixModPowWidth,
                                                     modulus);
                }

                // Explicit deallocation of potential native table.
                ((PRGElGamal) prg).free();
            }
        }
    }

    /**
     * Frees resources allocated by the tables. It is the
     * responsibility of the programmer to not call this method on an
     * instance that will be used again.
     */
    public void free() {
        for (int i = 0; i < g.length; i++) {
            if (g[i] != null) {
                g[i].free();
            }
        }
    }

    /**
     * Fills the given array with pseudo-random bytes.
     *
     * @param array Destination of random bytes.
     */
    @Override
    public void getBytes(final byte[] array) {
        synchronized (this) {

            if (r == null) {
                throw new CryptoError("The PRG is not seeded yet!");
            }

            int length = array.length;
            int index = 0;

            for (;;) {

                // Buffer contains nothing.
                if (buffer.isEmpty()) {
                    iter();

                // Buffer contains data.
                } else {

                    // Get a byte[] from our buffer.
                    final byte[] partial = buffer.remove(0);

                    // If partial contains more bytes than needed, we
                    // put the remaining bytes back in the buffer.
                    if (length < partial.length) {

                        System.arraycopy(partial, 0, array, index, length);
                        buffer.add(0, Arrays.copyOfRange(partial, length,
                                                         partial.length));
                        return;

                    // Otherwise we copy the contents of partial into
                    // our destination array and update the index and
                    // length.
                    } else {

                        System.arraycopy(partial, 0,
                                         array, index,
                                         partial.length);
                        index += partial.length;
                        length -= partial.length;
                        if (length == 0) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Produces some more bits to the internal buffer. The randomness
     * to make another iteration is stored separately.
     */
    protected void iter() {

        synchronized (this) {

            for (int i = 0; i < g.length; i++) {

                // Exponentiation by r gives a pseudo-random square
                // modulo the safe prime modulus.
                LargeInteger li = g[i].modPow(r);

                // We map the square to an integer modulo the group order.
                if (li.compareTo(groupOrder) > 0) {
                    li = modulus.sub(li).mod(groupOrder);
                }

                // Make sure we can iterate again at a later time.
                if (i == 0) {

                    r = li;

                } else {

                    // The result is a pseudorandom integer between 0
                    // and groupOrder-1. To turn it into a
                    // pseudo-random array of bytes, the statDist most
                    // significant bits are ignored.
                    final byte[] liBytes = li.toByteArray();

                    final int origSkip = (statDist + 7) / 8;

                    // Space for storing the derived pseudo-random bits.
                    final byte[] bufferBytes =
                        new byte[modulus.bitLength() / 8 - origSkip];

                    final int liOffset = liBytes.length - bufferBytes.length;

                    // To understand this code, note that liBytes may be
                    // shorter than bufferBytes.
                    System.arraycopy(liBytes,
                                     Math.max(liOffset, 0), bufferBytes,
                                     Math.max(0, -liOffset),
                                     Math.min(bufferBytes.length,
                                              liBytes.length));

                    // If the most significant bits in li happens to
                    // be zero, then we need to set them explicitly,
                    // since when an integer is turned into a byte[]
                    // these zeros are eliminated.
                    Arrays.fill(bufferBytes,
                                0, Math.max(0, -liOffset),
                                (byte) 0);

                    buffer.add(bufferBytes);
                }
            }
        }
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        return new ByteTree(modulus.toByteTree(),
                            ByteTree.intToByteTree(g.length),
                            ByteTree.intToByteTree(statDist));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(modulo safe prime, bitLength="
            + modulus.bitLength() + ", width=" + g.length + ", statDist="
            + statDist + ")";
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
        if (!(obj instanceof PRGElGamal)) {
            return false;
        }
        final PRGElGamal prg = (PRGElGamal) obj;
        return modulus.equals(prg.modulus)
            && g.length == prg.g.length
            && statDist == prg.statDist;
    }
}
