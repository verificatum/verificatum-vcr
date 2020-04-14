
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
