
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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizable;
import com.verificatum.ui.Util;


/**
 * Wrapper of standardized signatures. Currently, this means RSA full
 * domain hash based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public final class SignatureKeyGenHeuristic
    implements SignatureKeyGen, Marshalizable {

    /**
     * Underlying "signature algorithm".
     */
    public static final String ALGORITHM = "RSA";

    /**
     * Underlying key generation algorithm.
     */
    KeyPairGenerator keyGen;

    /**
     * Bit length of modulus of public key.
     */
    int bitlength;

    /**
     * Returns a new instance as defined by the input.
     *
     * @param btr Representation of key generator.
     * @return Instance of key generator.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static SignatureKeyGenHeuristic newInstance(final ByteTreeReader btr)
        throws CryptoFormatException {
        try {
            return new SignatureKeyGenHeuristic(btr.readInt());
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Create a key generator for the given bit length.
     *
     * @param bitlength Bit length of modulus of generated public
     * keys.
     */
    public SignatureKeyGenHeuristic(final int bitlength) {
        try {
            keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(bitlength);
            this.bitlength = bitlength;
        } catch (final NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate key generator!", nsae);
        }
    }

    /**
     * Generates a signature key pair. WARNING! This method ignores
     * the random source parameter and uses the builtin standard
     * {@link java.security.SecureRandom} instance of the virtual
     * machine.
     *
     * @param randomSource Source of randomness used by generator.
     * @return Signature key pair.
     */
    @Override
    public SignatureKeyPair gen(final RandomSource randomSource) {

        final KeyPair pair = keyGen.generateKeyPair();
        final PrivateKey priv = pair.getPrivate();
        final PublicKey pub = pair.getPublic();

        return new SignatureKeyPair(new SignaturePKeyHeuristic(pub,
                                                               bitlength),
                                    new SignatureSKeyHeuristic(priv,
                                                               bitlength));
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        return ByteTree.intToByteTree(bitlength);
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(" + ALGORITHM + ", bitlength="
            + bitlength + ")";
    }
}
