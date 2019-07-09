
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

import com.verificatum.crypto.CryptoException;
import com.verificatum.crypto.RandomSource;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.SignatureKeyGen;
import com.verificatum.crypto.SignatureKeyGenHeuristic;
import com.verificatum.crypto.SignatureKeyPair;
import com.verificatum.crypto.SignaturePKey;
import com.verificatum.crypto.SignatureSKey;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link TestSignatureKeyGen}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public final class TestSignatureKeyGen {

    /**
     * Lock object.
     */
    private static Object lock = new Object();

    /**
     * Source of randomness.
     */
    static RandomSource rs;

    /**
     * Number of different signature schemes.
     */
    static final int NO_KEYGEN = 1;

    /**
     * Signature key generator.
     */
    static SignatureKeyGen keygen;

    /**
     * Constructor needed to avoid that this class is instantiated.
     */
    private TestSignatureKeyGen() {
    }

    /**
     * Instantiate a random source to be used by the other methods.
     *
     * @param tp Test parameters configuration of the servers.
     * @param i Index of signature scheme tested.
     * @throws Exception If a test fails.
     */
    protected static void setupKeyGen(final TestParameters tp, final int i)
        throws Exception {

        synchronized (lock) {
            if (rs == null) {
                rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
            }

            switch (i) {
            case 0:
                tp.ps.print("SignatureKeyGenHeuristic ");
                keygen = new SignatureKeyGenHeuristic(512);
                break;
            case 1:
                // Add additional schemes here.
            default:
                throw new CryptoException("Bad signature type index!");
            }
        }
    }

    /**
     * Verifies signing and verifying signatures.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception If a test fails.
     */
    public static void signAndVerify(final TestParameters tp) throws Exception {

        for (int i = 0; i < NO_KEYGEN; i++) {

            setupKeyGen(tp, i);

            final SignatureKeyPair keyPair = keygen.gen(rs);

            final Timer timer = new Timer(tp.milliSeconds);

            int size = 1;

            while (!timer.timeIsUp()) {

                final byte[] message = rs.getBytes(size);
                final byte[] signature = keyPair.getSKey().sign(rs, message);

                assert keyPair.getPKey().verify(signature, message)
                    : "Failed to sign and verify!";

                size++;
            }
        }
    }

    /**
     * Verify conversion to and from byte tree.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception If a test fails.
     */
    public static void marshal(final TestParameters tp) throws Exception {

        for (int i = 0; i < NO_KEYGEN; i++) {

            setupKeyGen(tp, i);

            final Timer timer = new Timer(tp.milliSeconds);

            int size = 1;

            while (!timer.timeIsUp()) {

                final byte[] message = rs.getBytes(size);

                final SignatureKeyPair keyPair = keygen.gen(rs);

                final String skeyString =
                    Marshalizer.marshalToHexHuman(keyPair.getSKey(), true);
                final SignatureSKey skey =
                    Marshalizer.unmarshalHexAux_SignatureSKey(skeyString,
                                                              rs,
                                                              10);

                final String pkeyString =
                    Marshalizer.marshalToHexHuman(keyPair.getPKey(), true);
                final SignaturePKey pkey =
                    Marshalizer.unmarshalHexAux_SignaturePKey(pkeyString,
                                                              rs,
                                                              10);

                final byte[] signature = skey.sign(rs, message);

                assert pkey.verify(signature, message)
                    : "Failed to marshal signature keys!";

                size++;
            }
        }
    }
}
