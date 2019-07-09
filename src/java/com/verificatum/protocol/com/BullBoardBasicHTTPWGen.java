
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
