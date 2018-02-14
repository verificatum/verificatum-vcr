
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
import com.verificatum.protocol.ProtocolError;
import com.verificatum.protocol.ProtocolDefaults;
import com.verificatum.ui.info.InfoException;
import com.verificatum.ui.info.PartyInfo;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;
import com.verificatum.ui.info.InetPortField;

/**
 * Defines which information is stored in the protocol and private
 * info files, and also defines default values of some fields. For
 * each subclass of {@link BullBoardBasicHTTPW}, there should be a
 * corresponding subclass of this class that allows adding the needed
 * fields and default values.
 *
 * @author Douglas Wikstrom
 */
public final class BullBoardBasicHTTPWGen extends BullBoardBasicHTTPGen {

    /**
     * Description of listening hintl server type field.
     */
    public static final String HINTL_DESCRIPTION =
        "Socket address given as <hostname>:<port> or <ip address>:<port>, "
        + "where our hint server listens for connections, which may be "
        + "different from the address used to access it, e.g., if it is "
        + "behind a NAT.";

    /**
     * Description of listening hint server type field.
     */
    public static final String HINT_DESCRIPTION =
        "Socket address given as <hostname>:<port> or <ip address>:<port> "
        + "to our hint server. A hint server is a simple UDP server that "
        + "reduces latency and traffic on the HTTP servers.";

    @Override
    public void addProtocolInfo(final ProtocolInfo pri) {
        super.addProtocolInfo(pri);
        final InetPortField hintField =
            new InetPortField(BullBoardBasicHTTPW.HINT, HINT_DESCRIPTION, 1, 1);
        pri.getFactory().addInfoFields(hintField);
    }

    @Override
    public void addPrivateInfo(final PrivateInfo pi) {
        super.addPrivateInfo(pi);
        pi.addInfoField(new InetPortField(BullBoardBasicHTTPW.HINTL,
                                          HINTL_DESCRIPTION, 1, 1));
    }

    @Override
    public void addDefault(final PrivateInfo pi,
                           final ProtocolInfo pri,
                           final RandomSource rs) {
        super.addDefault(pi, pri, rs);
        try {
            pi.addValue(BullBoardBasicHTTPW.HINTL, ProtocolDefaults.HINTL());
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
    }

    @Override
    public void addDefault(final PartyInfo pai,
                           final ProtocolInfo pri,
                           final PrivateInfo pi,
                           final RandomSource rs) {
        super.addDefault(pai, pri, pi, rs);
        try {
            pai.addValue(BullBoardBasicHTTPW.HINT, ProtocolDefaults.HINT());
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
    }
}
