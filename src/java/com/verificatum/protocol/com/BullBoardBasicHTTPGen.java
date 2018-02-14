
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
