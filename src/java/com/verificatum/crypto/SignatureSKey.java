
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
 * Interface representing a secret signature key.
 *
 * @author Douglas Wikstrom
 */
public interface SignatureSKey extends Marshalizable {

    /**
     * Sign the input and output the resulting signature.
     *
     * @param message Data to be signed.
     * @param randomSource Source of randomness used to create the
     * signature.
     * @return Signature of the message.
     */
    byte[] sign(RandomSource randomSource, byte[]... message);

    /**
     * Sign the input hashdigest and output the resulting signature.
     * Unless a hashdigest {@link #getDigest()} is used to compute the
     * digest the result is undefined.
     *
     * @param d Hash digest to be signed.
     * @param randomSource Source of randomness used to create the
     * signature.
     * @return Signature of the digest.
     */
    byte[] signDigest(RandomSource randomSource, byte[] d);

    /**
     * Returns an updateable digest.
     *
     * @return Updateable digest.
     */
    Hashdigest getDigest();
}
