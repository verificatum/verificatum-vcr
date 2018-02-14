
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
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

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
public final class SignaturePKeyHeuristic implements SignaturePKey {

    /**
     * Maximum number of bytes in public key.
     */
    public static final int MAX_PKEY_BYTELENGTH = 100 * 1024;

    /**
     * Underlying "signature algorithm".
     */
    public static final String ALGORITHM = "RSA";

    /**
     * Bit length of modulus.
     */
    int bitlength;

    /**
     * Encapsulated public key.
     */
    PublicKey pub;

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
     * @return Public signature key.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static SignaturePKeyHeuristic newInstance(final ByteTreeReader btr,
                                                     final RandomSource rs,
                                                     final int certainty)
        throws CryptoFormatException {
        try {

            final ByteTreeReader kbtr = btr.getNextChild();
            if (kbtr.getRemaining() > MAX_PKEY_BYTELENGTH) {
                throw new CryptoFormatException("Too long key!");
            }

            final byte[] keyBytes = kbtr.read();

            final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            final KeyFactory factory = KeyFactory.getInstance(ALGORITHM);

            final int bitlength = btr.getNextChild().readInt();

            return new SignaturePKeyHeuristic(factory.generatePublic(spec),
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
     * Creates an instance with the given parameters.
     *
     * @param pub Encapsulated public key.
     * @param bitlength Bit length of modulus of public key.
     */
    public SignaturePKeyHeuristic(final PublicKey pub, final int bitlength) {
        this.pub = pub;
        this.bitlength = bitlength;
    }

    // Documented in SignaturePKey.java

    @Override
    public boolean verify(final byte[] signature, final byte[]... message) {
        final Hashdigest hd = getDigest();

        for (int i = 0; i < message.length; i++) {
            hd.update(message[i], 0, message[i].length);
        }
        return verifyDigest(signature, hd.digest());
    }

    @Override
    public boolean verifyDigest(final byte[] signature, final byte[] d) {
        try {

            final Signature sig =
                Signature.getInstance("SHA256with" + ALGORITHM);
            sig.initVerify(pub);
            sig.update(d);

            return sig.verify(signature);

        } catch (final NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate public key!", nsae);
        } catch (final InvalidKeyException ike) {
            throw new CryptoError("Invalid public key!", ike);
        } catch (final SignatureException se) {
            return false;
        }
    }

    @Override
    public Hashdigest getDigest() {
        return new HashfunctionHeuristic("SHA-256").getDigest();
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        return new ByteTree(new ByteTree(pub.getEncoded()),
                            ByteTree.intToByteTree(bitlength));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(" + ALGORITHM + ", bitlength="
            + bitlength + ")";
    }
}
