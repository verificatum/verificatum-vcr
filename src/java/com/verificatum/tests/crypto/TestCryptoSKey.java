
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

import java.util.Arrays;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.crypto.CryptoKeyGen;
import com.verificatum.crypto.CryptoKeyPair;
import com.verificatum.crypto.CryptoSKey;
import com.verificatum.crypto.CryptoSKeyNaorYung;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.test.TestClass;
import com.verificatum.util.Timer;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link CryptoSKey}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestCryptoSKey extends TestClass {

    /**
     * Key generator used for testing.
     */
    final CryptoKeyGen keyGen;

    /**
     * Key pair used for testing.
     */
    final CryptoKeyPair keyPair;

    /**
     * Construct test.
     *
     * @param tp Test parameters.
     * @param keyGen Key generator.
     * @throws ArithmFormatException If the test cannot be constructed.
     */
    public TestCryptoSKey(final TestParameters tp,
                          final CryptoKeyGen keyGen)
        throws ArithmFormatException {
        super(tp);
        this.keyGen = keyGen;
        this.keyPair = keyGen.gen(rs, 10);
    }

    /**
     * Exercise toString.
     */
    public void excToString() {
        keyPair.getSKey().toString();
    }

    /**
     * Exercise humanDescription.
     */
    public void excHumanDescription() {
        keyPair.getSKey().humanDescription(true);
    }

    /**
     * Exercise hashCode.
     */
    public void excHashcode() {
        keyPair.getSKey().hashCode();
    }

    /**
     * Exercise encryption.
     */
    public void encryptionDecryption() {
        int size = 1;

        final Timer timer = new Timer(tp.milliSeconds);

        while (!timer.timeIsUp()) {

            final CryptoKeyPair keyPair = keyGen.gen(rs, 10);

            final byte[] message = rs.getBytes(size);
            final byte[] label = rs.getBytes(size);

            final byte[] ciphertext =
                keyPair.getPKey().encrypt(label, message, rs, 10);

            final byte[] plaintext =
                keyPair.getSKey().decrypt(label, ciphertext);

            assert Arrays.equals(message, plaintext)
                : "Encryption or decryption failed!";

            size++;
        }
    }

    /**
     * Equals.
     *
     * @throws EIOException If a test fails.
     */
    public void equality()
        throws EIOException {

        final CryptoSKey skey = keyPair.getSKey();

        final ByteTreeBasic bt = Marshalizer.marshal(skey);
        final ByteTreeReader btr = bt.getByteTreeReader();
        final CryptoSKeyNaorYung skeyCopy =
            (CryptoSKeyNaorYung)
            Marshalizer.unmarshalAux_CryptoSKey(btr, rs, 10);

        assert skey.equals(skey) : "Equality by reference failed!";
        assert skey.equals(skeyCopy) : "Equality by value failed!";

        final CryptoKeyPair keyPair2 = keyGen.gen(rs, 10);
        final CryptoSKey skey2 = keyPair2.getSKey();

        assert !skey.equals(skey2) : "Inequality by value failed!";

        assert !skey.equals(new Object())
            : "Inequality with instance of different class failed!";
    }
}
