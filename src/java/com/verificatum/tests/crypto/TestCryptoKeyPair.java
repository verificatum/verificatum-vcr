
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

package com.verificatum.tests.crypto;

import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.CryptoKeyPair;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestParameters;
import com.verificatum.test.TestClass;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link CryptoKeyPair}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestCryptoKeyPair extends TestClass {

    /**
     * Crypto key pair.
     */
    final CryptoKeyPair keyPair;

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @param keyPair Key pair.
     */
    public TestCryptoKeyPair(final TestParameters tp,
                             final CryptoKeyPair keyPair) {
        super(tp);
        this.keyPair = keyPair;
    }

    /**
     * Exercise getPKey.
     */
    public void excGetPKey() {
        keyPair.getPKey();
    }

    /**
     * Exercise getSKey.
     */
    public void excGetSKey() {
        keyPair.getSKey();
    }

    /**
     * Exercise humanDescription.
     */
    public void excHumanDescription() {
        keyPair.humanDescription(true);
    }

    /**
     * Byte tree.
     *
     * @throws CryptoFormatException If the a test fails.
     */
    public void byteTree()
        throws CryptoFormatException {

        final ByteTreeBasic bt = keyPair.toByteTree();

        final ByteTreeReader btr = bt.getByteTreeReader();
        final CryptoKeyPair keyPair2 = CryptoKeyPair.newInstance(btr, rs, 10);

        assert keyPair.getPKey().equals(keyPair2.getPKey())
            : "Public keys are distinct!";
        assert keyPair.getSKey().equals(keyPair2.getSKey())
            : "Secret keys are distinct!";

        final ByteTree btb = new ByteTree(new byte[1]);
        final ByteTreeReader btrb = btb.getByteTreeReader();
        boolean invalid = false;
        try {
            CryptoKeyPair.newInstance(btrb, rs, 10);
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }
}
