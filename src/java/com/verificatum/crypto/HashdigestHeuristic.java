
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

import java.security.MessageDigest;

/**
 * Interface for a digest of a collision-free hash function.
 *
 * @author Douglas Wikstrom
 */
public final class HashdigestHeuristic implements Hashdigest {

    /**
     * Message digest wrapped by this instance.
     */
    MessageDigest md;

    /**
     * Constructs a wrapper for the given message digest.
     *
     * @param md Message digest wrapped by this instance.
     */
    public HashdigestHeuristic(final MessageDigest md) {
        this.md = md;
    }

    // Documented in Hashdigest.java

    @Override
    public void update(final byte[]... data) {
        for (int i = 0; i < data.length; i++) {
            md.update(data[i]);
        }
    }

    @Override
    public void update(final byte[] data, final int offset, final int length) {
        md.update(data, offset, length);
    }

    @Override
    public byte[] digest() {
        return md.digest();
    }
}
