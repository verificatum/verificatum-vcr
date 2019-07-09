
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
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.util.Functions;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;

/**
 * Public key of a Naor-Yung cryptosystem.
 *
 * @author Douglas Wikstrom
 */
public final class CryptoPKeyNaorYung implements CryptoPKey {

    /**
     * Underlying collision-resistant hashfunction.
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
     * Encryption element.
     */
    PGroupElement h;

    /**
     * Bit length of challenges in Fiat-Shamir proof.
     */
    int secpro;

    /**
     * Creates a public from the given parameters.
     *
     * @param roh Underlying collision-resistant hashfunction.
     * @param g1 First basis element.
     * @param g2 Second basis element.
     * @param h Encryption element.
     * @param secpro Bit length of challenges in Fiat-Shamir proof.
     */
    public CryptoPKeyNaorYung(final Hashfunction roh,
                              final PGroupElement g1,
                              final PGroupElement g2,
                              final PGroupElement h,
                              final int secpro) {
        this.roh = roh;
        this.g1 = g1;
        this.g2 = g2;
        this.h = h;
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
     * Encryption element.
     *
     * @return Encryption element.
     */
    @CoberturaIgnore
    public PGroupElement geth() {
        return h;
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
    public static CryptoPKeyNaorYung newInstance(final ByteTreeReader btr,
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
            final PGroupElement g1 = pGroup.toElement(btr.getNextChild());
            final PGroupElement g2 = pGroup.toElement(btr.getNextChild());
            final PGroupElement h = pGroup.toElement(btr.getNextChild());
            final int secpro = btr.getNextChild().readInt();

            return new CryptoPKeyNaorYung(roh, g1, g2, h, secpro);

        } catch (final ArithmFormatException afe) {
            throw new CryptoFormatException("Malformed key!", afe);
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed key!", eioe);
        }
    }

    // Documented in CryptoPKey.java

    @Override
    public byte[] encrypt(final byte[] label,
                          final byte[] message,
                          final RandomSource randomSource,
                          final int statDist) {

        // Convert into a list of group elements.
        final PGroupElement[] els =
            g1.getPGroup().encode(message, randomSource);

        // Map to product group.
        final PPGroup pPGroup = new PPGroup(g1.getPGroup(), els.length);

        final PGroupElement m = pPGroup.product(els);
        final PGroupElement pg1 = pPGroup.product(g1);
        final PGroupElement pg2 = pPGroup.product(g2);
        final PGroupElement ph = pPGroup.product(h);

        final PRing pPRing = pPGroup.getPRing();

        // Perform encryption in product group.
        final PRingElement r = pPRing.randomElement(randomSource, statDist);
        final PGroupElement u1 = pg1.exp(r);
        final PGroupElement u2 = pg2.exp(r);
        final PGroupElement e = ph.exp(r).mul(m);

        // Compute proof commitment.
        final PRingElement s = pPRing.randomElement(randomSource, statDist);
        final PGroupElement a1 = pg1.exp(s);
        final PGroupElement a2 = pg2.exp(s);

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
            pPRing.getPField().toElement(LargeInteger.toPositive(challenge));

        final PRingElement d = r.mul(pFieldChallenge).add(s);

        // Pack the result and return as byte[]
        final ByteTreeBasic btb =
            new ByteTreeContainer(ByteTree.intToByteTree(els.length),
                                  u1.toByteTree(),
                                  u2.toByteTree(),
                                  e.toByteTree(),
                                  a1.toByteTree(),
                                  a2.toByteTree(),
                                  d.toByteTree());
        return btb.toByteArray();
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public boolean equals(final Object pkey) {
        if (this == pkey) {
            return true;
        }
        if (!(pkey instanceof CryptoPKeyNaorYung)) {
            return false;
        }
        final CryptoPKeyNaorYung pkeycs = (CryptoPKeyNaorYung) pkey;

        return g1.equals(pkeycs.g1) && g2.equals(pkeycs.g2)
            && h.equals(pkeycs.h) && roh.equals(pkeycs.roh);
    }

    // Documented in ByteTreeConvertible.java

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(roh),
                                     Marshalizer.marshal(g1.getPGroup()),
                                     g1.toByteTree(),
                                     g2.toByteTree(),
                                     h.toByteTree(),
                                     ByteTree.intToByteTree(secpro));
    }

    // Documented in Marshalizable.java

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "("
            + roh.humanDescription(verbose) + ","
            + g1.getPGroup().humanDescription(verbose) + ")";
    }
}
