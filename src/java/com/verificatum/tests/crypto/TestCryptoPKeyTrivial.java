
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

import com.verificatum.crypto.CryptoPKey;
import com.verificatum.crypto.CryptoPKeyTrivial;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link CryptoPKeyTrivial}.
 *
 * @author Douglas Wikstrom
 */
public final class TestCryptoPKeyTrivial extends TestClass {

    /**
     * Trivial public key for use.
     */
    private final CryptoPKey pKey;

    /**
     * Constructor needed to avoid that this class is instantiated.
     *
     * @param tp Test parameters.
     */
    public TestCryptoPKeyTrivial(final TestParameters tp) {
        super(tp);
        this.pKey = new CryptoPKeyTrivial();
    }

    /**
     * Exercise encryption.
     */
    public void excEncryption() {

        final byte[] label = new byte[1];
        final byte[] message = new byte[1];

        pKey.encrypt(label, message, rs, 10);
    }

    /**
     * Excercise byte tree.
     */
    public void excByteTree() {
        pKey.toByteTree();
    }

    /**
     * Exercise humanDescription.
     */
    public void excHumanDescription() {
        pKey.humanDescription(true);
    }
}
