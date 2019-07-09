
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
