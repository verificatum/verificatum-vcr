
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
