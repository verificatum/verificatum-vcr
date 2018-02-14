
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

package com.verificatum.crypto;

import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
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
 * Interface representing a key generation algorithm of a Naor-Yung
 * cryptosystem over a given group and using a given hashfunction.
 *
 * @author Douglas Wikstrom
 */
public final class CryptoKeyGenNaorYung implements CryptoKeyGen {

    /**
     * Underlying group.
     */
    PGroup pGroup;

    /**
     * Underlying hashfunction.
     */
    Hashfunction roh;

    /**
     * Bit length of challenges in Fiat-Shamir proof.
     */
    int secpro;

    /**
     * Constructs an instance corresponding to the input
     * representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>- <code>certainty</code> </sup>.
     * @return Key generator represented by the input.
     *
     * @throws CryptoFormatException If the input does not represent
     * valid instructions for creating an instance.
     */
    public static CryptoKeyGenNaorYung newInstance(final ByteTreeReader btr,
                                                   final RandomSource rs,
                                                   final int certainty)
        throws CryptoFormatException {
        try {

            final PGroup pGroup =
                Marshalizer.unmarshalAux_PGroup(btr.getNextChild(),
                                                rs,
                                                certainty);
            final Hashfunction roh =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs,
                                                      certainty);
            final int secpro = btr.getNextChild().readInt();
            return new CryptoKeyGenNaorYung(pGroup, roh, secpro);

        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed key!", eioe);
        }
    }

    /**
     * Creates an instance with the given underlying group and
     * hashfunction.
     *
     * @param pGroup Underlying group.
     * @param roh Underlying collision-resistant hashfunction.
     * @param secpro Bit length of challenges in Fiat-Shamir proof.
     */
    public CryptoKeyGenNaorYung(final PGroup pGroup,
                                final Hashfunction roh,
                                final int secpro) {
        this.pGroup = pGroup;
        this.roh = roh;
        this.secpro = secpro;
    }

    /**
     * Underlying group.
     *
     * @return Underlying group.
     */
    public PGroup getPGroup() {
        return pGroup;
    }

    /**
     * Hash function used to implement the random oracle.
     *
     * @return Hash function used to implement the random oracle.
     */
    public Hashfunction getRandomOracleHashfunction() {
        return roh;
    }

    /**
     * Prints a string representation of this instance. This should
     * only be used for debugging.
     *
     * @return String representation of this instance.
     */
    @Override
    public String toString() {
        return pGroup.toString() + ":" + roh.toString();
    }

    // Documented in CryptoKeyGen.java

    @Override
    public CryptoKeyPair gen(final RandomSource randomSource,
                             final int statDist) {
        final PRing pRing = pGroup.getPRing();
        final PRingElement z = pRing.randomElement(randomSource, statDist);

        final PGroupElement g1 = pGroup.getg();
        final PRingElement r = pRing.randomElement(randomSource, statDist);
        final PGroupElement g2 = pGroup.getg().exp(r);

        final PGroupElement h = g1.exp(z);

        final CryptoPKey pkey =
            new CryptoPKeyNaorYung(roh, g1, g2, h, secpro);
        final CryptoSKey skey =
            new CryptoSKeyNaorYung(pGroup, roh, g1, g2, z, secpro);

        return new CryptoKeyPair(pkey, skey);
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(pGroup),
                                     Marshalizer.marshal(roh),
                                     ByteTree.intToByteTree(secpro));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose)
            + "(" + pGroup.humanDescription(verbose) + ","
            + roh.humanDescription(verbose) + ")";
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
        if (!(obj instanceof CryptoKeyGenNaorYung)) {
            return false;
        }
        final CryptoKeyGenNaorYung keyGen = (CryptoKeyGenNaorYung) obj;

        return keyGen.pGroup.equals(pGroup)
            && keyGen.roh.equals(roh)
            && keyGen.secpro == secpro;
    }
}
