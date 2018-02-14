
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

package com.verificatum.crypto;

import com.verificatum.eio.ExtIO;

/**
 * Digest for a random oracle.
 *
 * @author Douglas Wikstrom
 */
public final class HashdigestRandomOracle implements Hashdigest {

    /**
     * Underlying hashfunction.
     */
    Hashfunction hashfunction;

    /**
     * Message digest wrapped by this instance.
     */
    Hashdigest hd;

    /**
     * Output bit length.
     */
    int outputLength;

    /**
     * Creates an instance using the given hashfunction and with the
     * given output bit length.
     *
     * @param hashfunction Underlying hashfunction.
     * @param outputLength Output bit length.
     */
    public HashdigestRandomOracle(final Hashfunction hashfunction,
                                  final int outputLength) {

        this.hashfunction = hashfunction;
        this.outputLength = outputLength;

        final byte[] prefix = new byte[4];
        ExtIO.writeInt(prefix, 0, outputLength);

        this.hd = hashfunction.getDigest();
        hd.update(prefix);
    }

    // Documented in Hashdigest.java

    @Override
    public void update(final byte[]... data) {
        hd.update(data);
    }

    @Override
    public void update(final byte[] data,
                       final int offset,
                       final int length) {
        hd.update(data, offset, length);
    }

    @Override
    public byte[] digest() {

        final PRGHeuristic prg = new PRGHeuristic(hashfunction);

        final byte[] seed = hd.digest();

        prg.setSeed(seed);

        final int len = (outputLength + 7) / 8;

        final byte[] res = prg.getBytes(len);

        if (outputLength % 8 != 0) {
            res[0] = (byte) (res[0] & (0xFF >>> (8 - outputLength % 8)));
        }

        return res;
    }
}
