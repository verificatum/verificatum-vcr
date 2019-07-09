
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

package com.verificatum.protocol.com;

import com.verificatum.crypto.RandomSource;
import com.verificatum.crypto.SignatureKeyPair;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.protocol.ProtocolDefaults;
import com.verificatum.protocol.ProtocolError;
import com.verificatum.ui.info.InfoException;
import com.verificatum.ui.info.PartyInfo;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;
import com.verificatum.ui.info.StringField;
import com.verificatum.ui.info.URLPortField;
import com.verificatum.util.Lazy;

/**
 * Defines which information is stored in the protocol and private
 * info files, and also defines default values of some fields. For
 * each subclass of {@link BullBoardBasicHTTP}, there should be a
 * corresponding subclass of this class that allows adding the needed
 * fields and default values.
 *
 * @author Douglas Wikstrom
 */
public class BullBoardBasicHTTPGen extends BullBoardBasicGen {

    /**
     * Description of public key field.
     */
    public static final String PUB_KEY_DESCRIPTION =
        "Public signature key (instance of subclasses of "
        + "com.verificatum.crypto.SignaturePKey). WARNING! This field "
        + "is not validated syntactically.";

    /**
     * Description of private key field.
     */
    public static final String PRIV_KEY_DESCRIPTION =
        "Pair of public and private signature keys (instance of "
        + "com.verificatum.crypto.SignatureKeyPair). WARNING! This field "
        + "is not validated syntactically.";

    /**
     * Description of directory of http server field.
     */
    public static final String HTTPDIR_DESCRIPTION =
        "Root directory of HTTP server. WARNING! This field "
        + "is not validated syntactically.";

    /**
     * Description of http server type field.
     */
    public static final String HTTP_TYPE_DESCRIPTION =
        "Decides if an internal or external HTTP server is used. "
        + "Legal values are \"internal\" or \"external\".";

    /**
     * Description of the http-server field.
     */
    public static final String HTTP_DESCRIPTION =
        "URL to the HTTP server of this party.";

    /**
     * Description of listening http server type field.
     */
    public static final String HTTPL_DESCRIPTION =
        "URL where the HTTP-server of this party listens for connections, "
        + "which may be different from the HTTP address used to access "
        + "it, e.g., if it is behind a NAT.";

    @Override
    public void addProtocolInfo(final ProtocolInfo pri) {
        pri.getFactory()
            .addInfoFields(new StringField(BullBoardBasicHTTP.PUB_KEY,
                                           PUB_KEY_DESCRIPTION, 1, 1),
                           new URLPortField(BullBoardBasicHTTP.HTTP,
                                            HTTP_DESCRIPTION, 1, 1));
    }

    @Override
    public void addPrivateInfo(final PrivateInfo pi) {
        pi.addInfoField(new StringField(BullBoardBasicHTTP.PRIV_KEY,
                                        PRIV_KEY_DESCRIPTION, 1, 1));
        pi.addInfoField(new URLPortField(BullBoardBasicHTTP.HTTPL,
                                         HTTPL_DESCRIPTION, 1, 1));
        pi.addInfoField(new StringField(BullBoardBasicHTTP.HTTPDIR,
                                        HTTPDIR_DESCRIPTION, 1, 1));
        final StringField httptypeField =
            new StringField(BullBoardBasicHTTP.HTTP_TYPE,
                             HTTP_TYPE_DESCRIPTION, 1, 1).
            setPattern("internal|external");
        pi.addInfoField(httptypeField);
    }

    @Override
    public void addDefault(final PrivateInfo pi,
                           final ProtocolInfo pri,
                           final RandomSource rs) {
        try {
            pi.addValue(BullBoardBasicHTTP.PRIV_KEY,
                        ProtocolDefaults.LazySignatureSKey(rs));

            pi.addValue(BullBoardBasicHTTP.HTTPL, ProtocolDefaults.HTTPL());
            pi.addValue(BullBoardBasicHTTP.HTTPDIR, ProtocolDefaults.HTTPDIR());
            pi.addValue(BullBoardBasicHTTP.HTTP_TYPE,
                        ProtocolDefaults.HTTP_TYPE);
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
    }

    @Override
    public void addDefault(final PartyInfo pai,
                           final ProtocolInfo pri,
                           final PrivateInfo pi,
                           final RandomSource rs) {
        try {
            pai.addValue(BullBoardBasicHTTP.PUB_KEY, new Lazy() {
                @Override
                public String gen() {
                    try {
                        final Lazy lazyKeyPair =
                            (Lazy) pi.getValue(BullBoardBasicHTTP.PRIV_KEY);
                        final String keyPairString = lazyKeyPair.gen();
                        final SignatureKeyPair keyPair =
                            Marshalizer.
                            unmarshalHexAux_SignatureKeyPair(keyPairString,
                                                             rs,
                                                             50);

                        return Marshalizer.marshalToHexHuman(keyPair.getPKey(),
                                                             true);
                    } catch (final EIOException eioe) {
                        throw new ProtocolError("Failed to extract public key!",
                                                eioe);
                    }
                }
            });

            pai.addValue(BullBoardBasicHTTP.HTTP, ProtocolDefaults.HTTP());
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
    }
}
