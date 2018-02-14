
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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.ui.Util;


/**
 * Wrapper of a standard signature schemes. Currently, this means RSA
 * full domain hash based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public final class SignatureSKeyHeuristic implements SignatureSKey {

    /**
     * Maximum number of bytes in public key.
     */
    public static final int MAX_SKEY_BYTELENGTH = 100 * 1024;

    /**
     * Underlying "signature algorithm".
     */
    public static final String ALGORITHM = "RSA";

    /**
     * Bit length of the modulus in the corresponding public key.
     */
    int bitlength;

    /**
     * Encapsulated secret signature key.
     */
    PrivateKey priv;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Instance corresponding to the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static SignatureSKeyHeuristic newInstance(final ByteTreeReader btr,
                                                     final RandomSource rs,
                                                     final int certainty)
        throws CryptoFormatException {
        try {

            final ByteTreeReader kbtr = btr.getNextChild();
            if (kbtr.getRemaining() > MAX_SKEY_BYTELENGTH) {
                throw new CryptoFormatException("Too long key!");
            }

            final byte[] keyBytes = kbtr.read();

            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            final KeyFactory factory = KeyFactory.getInstance(ALGORITHM);

            final int bitlength = btr.getNextChild().readInt();
            return new SignatureSKeyHeuristic(factory.generatePrivate(spec),
                                              bitlength);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate public key!", nsae);
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (final InvalidKeySpecException ikse) {
            throw new CryptoFormatException("Malformed public key spec!", ikse);
        }
    }

    /**
     * Create instance from the given parameters.
     *
     * @param priv Encapsulated secret key.
     * @param bitlength Bit length of the modulus in the corresponding
     * public key.
     */
    public SignatureSKeyHeuristic(final PrivateKey priv, final int bitlength) {
        this.priv = priv;
        this.bitlength = bitlength;
    }

    // Documented SignatureSKey.java

    @Override
    public byte[] sign(final RandomSource randomSource,
                       final byte[]... message) {
        final Hashdigest hd = getDigest();

        for (int i = 0; i < message.length; i++) {
            hd.update(message[i], 0, message[i].length);
        }
        return signDigest(randomSource, hd.digest());
    }

    @Override
    public byte[] signDigest(final RandomSource randomSource,
                             final byte[] d) {
        try {

            final Signature sig =
                Signature.getInstance("SHA256with" + ALGORITHM);
            sig.initSign(priv);
            sig.update(d);

            return sig.sign();

        } catch (final NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate private key!", nsae);
        } catch (final InvalidKeyException ike) {
            throw new CryptoError("Invalid private key!", ike);
        } catch (final SignatureException se) {
            throw new CryptoError("Failed to compute signature!", se);
        }
    }

    @Override
    public Hashdigest getDigest() {
        return new HashfunctionHeuristic("SHA-256").getDigest();
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        return new ByteTree(new ByteTree(priv.getEncoded()),
                            ByteTree.intToByteTree(bitlength));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(" + ALGORITHM + ", bitlength="
            + bitlength + ")";
    }
}
