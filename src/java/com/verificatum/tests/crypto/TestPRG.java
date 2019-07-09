
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.crypto.CryptoError;
import com.verificatum.crypto.CryptoException;
import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.PRG;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.TempFile;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.test.TestClass;
import com.verificatum.util.Timer;


/**
 * Tests {@link PRG}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public abstract class TestPRG extends TestClass {

    /**
     * Pseudo-random generators.
     */
    protected PRG[] prgs;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     * @throws CryptoFormatException If construction of the test
     * failed.
     */
    protected TestPRG(final TestParameters tp)
        throws ArithmFormatException, CryptoFormatException {
        super(tp);
        this.prgs = prgs();
    }

    /**
     * Generates variations of the PRG with different parameters.
     *
     * @return Variations of the PRG.
     * @throws ArithmFormatException If a test fails.
     * @throws CryptoFormatException If a test fails.
     */
    protected abstract PRG[] prgs()
        throws ArithmFormatException, CryptoFormatException;

    /**
     * Generate.
     *
     * @throws Exception If a test fails.
     */
    public void generate() throws Exception {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            for (int i = 0; i < prgs.length; i++) {

                final byte[] seedBytes = rs.getBytes(prgs[i].minNoSeedBytes());
                prgs[i].setSeed(seedBytes);
                final byte[] seed = prgs[i].getBytes(size);
            }
            size++;
        }

        boolean invalid = false;
        try {
            final byte[] seedBytes = rs.getBytes(prgs[0].minNoSeedBytes() - 1);
            prgs[0].setSeed(seedBytes);
        } catch (final CryptoError ce) {
            invalid = true;
        }
        assert invalid : "Failed to fail on too short seed!";
    }

    /**
     * Verify conversion to byte tree.
     *
     * @throws Exception If a test fails.
     */
    public void marshal() throws Exception {

        for (int i = 0; i < prgs.length; i++) {

            final ByteTreeBasic byteTree = Marshalizer.marshal(prgs[i]);
            final PRG prg =
                Marshalizer.unmarshalAux_PRG(byteTree.getByteTreeReader(),
                                             rs, 10);

            final byte[] seedBytes = rs.getBytes(prgs[i].minNoSeedBytes());

            final int size = 10;
            prgs[i].setSeed(seedBytes);
            final byte[] r1 = prgs[i].getBytes(size);

            prg.setSeed(seedBytes);
            final byte[] r2 = prg.getBytes(size);

            assert Arrays.equals(r1, r2) : "Marshalling failed!";
        }
    }

    /**
     * Exercise read and store seed on file.
     *
     * @throws CryptoException If a test fails.
     * @throws IOException If a test fails.
     */
    public void excSetSeedReplaceStored()
        throws CryptoException, IOException {
        final File seedFile = TempFile.getFile();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("00000000000000000000");
        }

        // We simulate waiting for the seedfile to appear from an
        // other thread.
        final String[] failed = new String[1];
        final Thread thread = new Thread() {
                public void run() {
                    try {
                        prgs[0].setSeedReplaceStored(seedFile,
                                                     TempFile.getFile());
                    } catch (final CryptoException ce) {
                        failed[0] = ce.getMessage();
                    }
                }
            };
        thread.start();
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ie) {
        }
        ExtIO.atomicWriteString(TempFile.getFile(), seedFile, sb.toString());
        try {
            thread.join();
        } catch (final InterruptedException ie) {
        }
        if (failed[0] != null) {
            throw new CryptoException(failed[0]);
        }
    }

    /**
     * Exercise human description.
     */
    public void excHumanDescription() {
        prgs[0].humanDescription(true);
    }
}
