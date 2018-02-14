
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

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;

/**
 * Implementation of the Merkle-Damgaard construction of an
 * arbitrary-length collision-resistant hash function from a fixed
 * length collision-resistant hash function.
 *
 * @author Douglas Wikstrom
 */
public final class HashfunctionMerkleDamgaard implements Hashfunction {

    /**
     * Underlying fixed length collision-resistant hash function.
     */
    HashfunctionFixedLength hffl;

    /**
     * Constructs an instance following the instructions in the input
     * <code>ByteTree</code>.
     *
     * @param btr Representation of instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Hashfunction represented by the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static HashfunctionMerkleDamgaard
        newInstance(final ByteTreeReader btr,
                    final RandomSource rs,
                    final int certainty)
        throws CryptoFormatException {
        try {
            final HashfunctionFixedLength hffl =
                Marshalizer.unmarshalAux_HashfunctionFixedLength(btr,
                                                                 rs,
                                                                 certainty);
            return new HashfunctionMerkleDamgaard(hffl);
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Unable to interpret!", eioe);
        }
    }

    /**
     * Creates an instance from the given fixed length hash function.
     *
     * @param hffl Fixed length collision-resistant hash function.
     */
    public HashfunctionMerkleDamgaard(final HashfunctionFixedLength hffl) {
        this.hffl = hffl;
    }

    @Override
    public String toString() {
        return hffl.toString();
    }

    // Documented in Hashfunction.java

    @Override
    public byte[] hash(final byte[]... datas) {
        final Hashdigest hd = getDigest();

        for (int i = 0; i < datas.length; i++) {
            hd.update(datas[i]);
        }
        return hd.digest();
    }

    @Override
    public Hashdigest getDigest() {
        return new HashdigestMerkleDamgaard(hffl);
    }

    @Override
    public int getOutputLength() {
        return hffl.getOutputLength();
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return Marshalizer.marshal(hffl);
    }

    // Documented in HumanDescription.java

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "("
            + hffl.humanDescription(verbose) + ")";
    }

    // Documented in Object.java

    @Override
    public int hashCode() {
        final int hfflcode = hffl.hashCode();
        return hfflcode * hfflcode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashfunctionMerkleDamgaard)) {
            return false;
        }
        return hffl.equals(((HashfunctionMerkleDamgaard) obj).hffl);
    }
}
