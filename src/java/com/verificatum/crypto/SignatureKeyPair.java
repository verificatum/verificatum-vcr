
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
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizable;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;

/**
 * Container class of a signature public key and secret key.
 *
 * @author Douglas Wikstrom
 */
public final class SignatureKeyPair implements Marshalizable {

    /**
     * Secret key stored in this instance.
     */
    SignatureSKey skey;

    /**
     * Public key stored in this instance.
     */
    SignaturePKey pkey;

    /**
     * Create instance containing the given keys.
     *
     * @param pkey Public key.
     * @param skey Secret key.
     */
    public SignatureKeyPair(final SignaturePKey pkey,
                            final SignatureSKey skey) {
        this.pkey = pkey;
        this.skey = skey;
    }

    /**
     * Create instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code>
     * </sup>.
     * @return Instance corresponding to the input.
     *
     * @throws CryptoFormatException If the input does not represent
     *  an input.
     */
    public static SignatureKeyPair newInstance(final ByteTreeReader btr,
                                               final RandomSource rs,
                                               final int certainty)
        throws CryptoFormatException {
        try {
            final SignaturePKey pkey =
                Marshalizer.unmarshalAux_SignaturePKey(btr.getNextChild(),
                                                       rs,
                                                       certainty);
            final SignatureSKey skey =
                Marshalizer.unmarshalAux_SignatureSKey(btr.getNextChild(),
                                                       rs,
                                                       certainty);

            return new SignatureKeyPair(pkey, skey);

        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Return the public key of this instance.
     *
     * @return Public key.
     */
    public SignaturePKey getPKey() {
        return pkey;
    }

    /**
     * Return the secret key of this instance.
     *
     * @return Secret key.
     */
    public SignatureSKey getSKey() {
        return skey;
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(pkey),
                                     Marshalizer.marshal(skey));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "("
            + pkey.humanDescription(verbose) + ","
            + skey.humanDescription(verbose) + ")";
    }
}
