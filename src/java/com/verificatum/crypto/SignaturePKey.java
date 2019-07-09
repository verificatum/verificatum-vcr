
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

import com.verificatum.eio.Marshalizable;

/**
 * Interface representing a public signature key.
 *
 * @author Douglas Wikstrom
 */
public interface SignaturePKey extends Marshalizable {

    /**
     * Verify the given signature and message using this public key.
     *
     * @param signature Candidate signature.
     * @param message Data that supposedly is signed.
     * @return Verdict for the signature and message pair.
     */
    boolean verify(byte[] signature, byte[]... message);

    /**
     * Verify the given signature and digest using this public key.
     * The result is undefined unless {@link #getDigest()} was used to
     * compute the digest.
     *
     * @param signature Candidate signature.
     * @param d Digest that supposedly is signed.
     * @return Verdict for the signature and message pair.
     */
    boolean verifyDigest(byte[] signature, byte[] d);

    /**
     * Returns an updateable digest that can later be given as input
     * to {@link #verify(byte[],byte[][])}.
     *
     * @return Updateable digest.
     */
    Hashdigest getDigest();
}
