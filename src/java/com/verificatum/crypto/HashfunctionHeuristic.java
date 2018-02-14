
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.EIOException;
import com.verificatum.ui.Util;


/**
 * Implements a wrapper for standardized hashfunctions. Currently,
 * this means the SHA-2 family of hashfunctions.
 *
 * @author Douglas Wikstrom
 */
public final class HashfunctionHeuristic implements Hashfunction {

    /**
     * Maximal byte length of an algorithm name.
     */
    public static final int MAX_ALGORITHM_BYTELENGTH = 100;

    /**
     * Name of underlying algorithm.
     */
    String algorithm;

    /**
     * Length of output.
     */
    int outputLength;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @return Hashfunction represented by the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static HashfunctionHeuristic newInstance(final ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            if (btr.getRemaining() > MAX_ALGORITHM_BYTELENGTH) {
                throw new CryptoFormatException("Algorithm name is too long!");
            }
            return new HashfunctionHeuristic(btr.readString());

        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (final CryptoError ce) {
            throw new CryptoFormatException("Unable to interpret!", ce);
        }
    }

    /**
     * Creates an instance of a given heuristic hashfunction. The
     * supported algorithms are <code>SHA-256</code>,
     * <code>SHA-384</code>, and <code>SHA-512</code>.
     *
     * @param algorithm Name of algorithm.
     */
    public HashfunctionHeuristic(final String algorithm) {
        this.algorithm = algorithm;

        if ("SHA-256".equals(algorithm)) {
            outputLength = 256;
        } else if ("SHA-384".equals(algorithm)) {
            outputLength = 384;
        } else if ("SHA-512".equals(algorithm)) {
            outputLength = 512;
        } else {
            throw new CryptoError("Unsupported algorithm!");
        }
    }

    // Documented in Hashfunction.java

    @Override
    public Hashdigest getDigest() {
        try {
            final MessageDigest innerMd = MessageDigest.getInstance(algorithm);
            return new HashdigestHeuristic(innerMd);

        // UNCOVERABLE (Verified above.)
        } catch (final NoSuchAlgorithmException nsae) {
            throw new CryptoError("Unsupported algorithm!", nsae);
        }
    }

    // Apparently Sun did not make a thread safe implementation of
    // SHA-2. Thus, we need to add some synchronization here.
    @Override
    public byte[] hash(final byte[]... datas) {
        try {
            final MessageDigest md = MessageDigest.getInstance(algorithm);
            for (int i = 0; i < datas.length; i++) {
                md.update(datas[i]);
            }
            return md.digest();

        // UNCOVERABLE (Verified above.)
        } catch (final NoSuchAlgorithmException nsae) {
            throw new CryptoError("Unsupported algorithm!", nsae);
        }
    }

    @Override
    public int getOutputLength() {
        return outputLength;
    }

    @Override
    public String toString() {
        return algorithm;
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        try {
            return new ByteTree(algorithm.getBytes("UTF-8"));

        // UNCOVERABLE (Verified above.)
        } catch (final UnsupportedEncodingException uee) {
            throw new CryptoError("This should never happen!", uee);
        }

    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(" + algorithm + ")";
    }

    // Documented in Object.java

    @Override
    public int hashCode() {
        final HashfunctionHeuristic hh = new HashfunctionHeuristic("SHA-256");
        final Hashdigest h = hh.getDigest();
        toByteTree().update(h);
        final byte[] d = h.digest();
        return ExtIO.readInt(d, 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashfunctionHeuristic)) {
            return false;
        }
        return algorithm.equals(((HashfunctionHeuristic) obj).algorithm);
    }
}
