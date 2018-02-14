
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
