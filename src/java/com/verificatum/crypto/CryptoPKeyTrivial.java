
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

import java.util.Arrays;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.ui.Util;


/**
 * A trivial encryption key that maps a message to itself. This is
 * useful as a fallback to simplify the logics of protocols.
 *
 * @author Douglas Wikstrom
 */
public final class CryptoPKeyTrivial implements CryptoPKey {

    /**
     * Constructs an instance corresponding to the input
     * representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Public key represented by the input.
     */
    public static CryptoPKeyTrivial newInstance(final ByteTreeReader btr,
                                                final RandomSource rs,
                                                final int certainty) {
        return new CryptoPKeyTrivial();
    }

    // Documented in CryptoPKey.java

    @Override
    public byte[] encrypt(final byte[] label,
                          final byte[] message,
                          final RandomSource randomSource,
                          final int statDist) {
        return Arrays.copyOfRange(message, 0, message.length);
    }

    // Documented in ByteTreeConvertible.java

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTree();
    }

    // Documented in Marshalizable.java

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose);
    }
}
