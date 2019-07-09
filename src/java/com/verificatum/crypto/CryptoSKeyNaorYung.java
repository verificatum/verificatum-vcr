
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

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PFieldElement;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PPGroup;
import com.verificatum.arithm.PPGroupElement;
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;
import com.verificatum.util.Functions;

/**
 * Secret key of a Naor-Yung cryptosystem.
 *
 * @author Douglas Wikstrom
 */
public final class CryptoSKeyNaorYung implements CryptoSKey {

    /**
     * Underlying group.
     */
    PGroup pGroup;

    /**
     * Underlying hash function used to implement random oracle.
     */
    Hashfunction roh;

    /**
     * First basis element.
     */
    PGroupElement g1;

    /**
     * Second basis element.
     */
    PGroupElement g2;

    /**
     * Encryption exponent.
     */
    PRingElement z;

    /**
     * Bit length of challenges in Fiat-Shamir proof.
     */
    int secpro;

    /**
     * Creates a secret with the given components.
     *
     * @param pGroup Underlying group.
     * @param roh Underlying hash function used to
     * implement random oracle.
     * @param g1 First basis element.
     * @param g2 Second basis element.
     * @param z Encryption exponent.
     * @param secpro Bit length of challenges in Fiat-Shamir proof.
     */
    public CryptoSKeyNaorYung(final PGroup pGroup,
                              final Hashfunction roh,
                              final PGroupElement g1,
                              final PGroupElement g2,
                              final PRingElement z,
                              final int secpro) {
        this.pGroup = pGroup;
        this.roh = roh;
        this.g1 = g1;
        this.g2 = g2;
        this.z = z;
        this.secpro = secpro;
    }

    /**
     * Underlying hash function.
     *
     * @return Underlying hash function.
     */
    @CoberturaIgnore
    public Hashfunction getRandomOracleHashfunction() {
        return roh;
    }

    /**
     * First basis element.
     *
     * @return First basis element.
     */
    @CoberturaIgnore
    public PGroupElement getg1() {
        return g1;
    }

    /**
     * Second basis element.
     *
     * @return Second basis element.
     */
    @CoberturaIgnore
    public PGroupElement getg2() {
        return g2;
    }

    /**
     * Bit length of challenges in Fiat-Shamir proof.
     *
     * @return Bit length of output of random oracle.
     */
    @CoberturaIgnore
    public int getRandomOracleBitLength() {
        return secpro;
    }

    /**
     * Secret exponent.
     *
     * @return Secret exponent.
     */
    @CoberturaIgnore
    public PRingElement getz() {
        return z;
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
     * @throws CryptoFormatException If the input does not represent
     *  valid instructions for creating an instance.
     */
    public static CryptoSKeyNaorYung newInstance(final ByteTreeReader btr,
                                                 final RandomSource rs,
                                                 final int certainty)
        throws CryptoFormatException {
        try {

            final Hashfunction roh =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs,
                                                      certainty);
            final PGroup pGroup =
                Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                rs,
                                                certainty);
            final PRing pRing = pGroup.getPRing();
            final PGroupElement g1 = pGroup.toElement(btr.getNextChild());
            final PGroupElement g2 = pGroup.toElement(btr.getNextChild());
            final PRingElement z = pRing.toElement(btr.getNextChild());
            final int secpro = btr.getNextChild().readInt();

            return new CryptoSKeyNaorYung(pGroup, roh, g1, g2, z, secpro);

        } catch (final ArithmFormatException afe) {
            throw new CryptoFormatException("Malformed key!", afe);
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed key!", eioe);
        }
    }

    // Documented in CryptoSKey.java

    @Override
    public byte[] decrypt(final byte[] label, final byte[] ciphertext) {

        if (ciphertext.length == 0) {
            return new byte[0];
        }

        try {
            final ByteTreeReader btr =
                new ByteTree(ciphertext, null).getByteTreeReader();

            final int width = btr.getNextChild().readInt();

            final PPGroup pPGroup = new PPGroup(pGroup, width);
            final PRing pPRing = pPGroup.getPRing();

            // Read ciphertext.
            final PGroupElement u1 = pPGroup.toElement(btr.getNextChild());
            final PGroupElement u2 = pPGroup.toElement(btr.getNextChild());
            final PGroupElement e = pPGroup.toElement(btr.getNextChild());

            // Read proof of knowledge.
            final PGroupElement a1 = pPGroup.toElement(btr.getNextChild());
            final PGroupElement a2 = pPGroup.toElement(btr.getNextChild());
            final PRingElement d = pPRing.toElement(btr.getNextChild());

            btr.close();

            // Compute challenge.
            final ByteTreeBasic data =
                new ByteTreeContainer(new ByteTree(label),
                                      u1.toByteTree(),
                                      u2.toByteTree(),
                                      e.toByteTree(),
                                      a1.toByteTree(),
                                      a2.toByteTree());

            final RandomOracle ro = new RandomOracle(roh, secpro);
            final byte[] challenge = ro.hash(data.toByteArray());
            final PFieldElement pFieldChallenge =
                pPRing.getPField().
                toElement(LargeInteger.toPositive(challenge));

            final PGroupElement pg1 = pPGroup.product(g1);
            final PGroupElement pg2 = pPGroup.product(g2);

            // Check proof.
            if (u1.exp(pFieldChallenge).mul(a1).equals(pg1.exp(d))
                && u2.exp(pFieldChallenge).mul(a2).equals(pg2.exp(d))) {

                final PPGroupElement mel =
                    (PPGroupElement) e.mul(u1.exp(z.neg()));
                final PGroupElement[] mels = mel.getFactors();

                return pGroup.decode(mels);

            } else {
                return null;
            }

        } catch (final ArithmFormatException afe) {
            return null;
        } catch (final EIOException eioe) {
            return null;
        }
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(roh),
                                     Marshalizer.marshal(pGroup),
                                     g1.toByteTree(),
                                     g2.toByteTree(),
                                     z.toByteTree(),
                                     ByteTree.intToByteTree(secpro));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "("
            + roh.humanDescription(verbose) + ","
            + pGroup.humanDescription(verbose) + ")";
    }

    // Documented in Object.

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public boolean equals(final Object skey) {
        if (this == skey) {
            return true;
        }
        if (!(skey instanceof CryptoSKeyNaorYung)) {
            return false;
        }
        final CryptoSKeyNaorYung skeycs = (CryptoSKeyNaorYung) skey;

        return g1.equals(skeycs.g1) && g2.equals(skeycs.g2)
            && z.equals(skeycs.z) && roh.equals(skeycs.roh);
    }
}
