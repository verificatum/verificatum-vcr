
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
