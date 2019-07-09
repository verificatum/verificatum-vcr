
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
