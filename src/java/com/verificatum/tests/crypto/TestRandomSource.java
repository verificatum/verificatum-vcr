
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

import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Hex;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.TempFile;
import com.verificatum.test.TestParameters;
import com.verificatum.test.TestClass;
import com.verificatum.crypto.CryptoException;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomSource;


/**
 * Tests {@link RandomSource}.
 *
 * @author Douglas Wikstrom
 */
public final class TestRandomSource extends TestClass {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestRandomSource(final TestParameters tp) {
        super(tp);
    }

    /**
     * Verify that generation works.
     *
     * @throws IOException If a test fails.
     * @throws CryptoException If a test fails.
     */
    public void randomSource() throws IOException, CryptoException {

        final File rsFile = TempFile.getFile();
        final File seedFile = TempFile.getFile();
        final File tmpSeedFile = TempFile.getFile();

        final RandomSource rs = new RandomDevice();
        ExtIO.writeString(rsFile, Marshalizer.marshalToHex(rs));
        final RandomSource rs2 = RandomSource.randomSource(rsFile, null, null);
        assert rs.equals(rs2) : "Failed to create random device from file!";

        final PRGHeuristic prg = new PRGHeuristic();
        ExtIO.writeString(rsFile, Marshalizer.marshalToHex(prg));
        final byte[] byteSeed = new byte[10000];
        final String seed = Hex.toHexString(byteSeed);
        ExtIO.writeString(seedFile, seed);
        final RandomSource prg2 =
            RandomSource.randomSource(rsFile, seedFile, tmpSeedFile);
        assert prg.equals(prg2) : "Failed to create seeded PRG from file!";

        boolean invalid = false;
        try {
            final File nonExistant = new File("");
            RandomSource.randomSource(nonExistant, seedFile, tmpSeedFile);
        } catch (final CryptoException ce) {
            invalid = true;
        }
        assert invalid : "Failed to fail on unreadable file!";

        invalid = false;
        try {
            RandomSource.randomSource(seedFile, rsFile, tmpSeedFile);
        } catch (final CryptoException ce) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad data!";
    }
}
