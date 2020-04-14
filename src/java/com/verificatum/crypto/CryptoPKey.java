
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
 * Interface representing a public key of a cryptosystem with labels.
 *
 * <p>
 *
 * <b>If you implement this interface you MUST override
 * {@link Object#equals(Object)} as well. Unfortunately, it is not
 * possible to enforce this using interfaces in Java.</b>
 *
 * @author Douglas Wikstrom
 */
public interface CryptoPKey extends Marshalizable {

    /**
     * Encrypts the given message using randomness from the given
     * source.
     *
     * @param label Label used when encrypting.
     * @param message Message to be encrypted.
     * @param randomSource Source of randomness.
     * @param statDist Allowed statistical error from ideal
     * distribution.
     * @return Resulting ciphertext.
     */
    byte[] encrypt(byte[] label, byte[] message,
                   RandomSource randomSource, int statDist);
}
