
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

package com.verificatum.protocol;

import com.verificatum.ui.UI;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;


/**
 * Adds a threshold parameter to a protocol with a bulletin board
 * {@link ProtocolBB}.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolBBT extends ProtocolBB {

    /**
     * Name of threshold tag.
     */
    public static final String THRESHOLD = "thres";

    /**
     * Threshold number of parties needed to violate some security
     * property.
     */
    public final int threshold;

    /**
     * Creates a root protocol. This constructor should normally only
     * be called once in each application. All other protocols should
     * be constructed by calling a constructor of a subclass of this
     * class that makes a super call to
     * {@link Protocol#Protocol(String,Protocol)}.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     */
    public ProtocolBBT(final PrivateInfo privateInfo,
                       final ProtocolInfo protocolInfo,
                       final UI ui) {
        super(privateInfo, protocolInfo, ui);

        this.threshold = protocolInfo.getIntValue(THRESHOLD);
    }

    /**
     * Creates a child instance of <code>protocol</code> with session
     * identifier <code>sid</code>.
     *
     * @param sid Session identifier for this instance.
     * @param prot Protocol that invokes this protocol as a
     * subprotocol.
     */
    public ProtocolBBT(final String sid, final ProtocolBBT prot) {
        super(sid, prot);

        this.threshold = prot.threshold;
    }

    /**
     * Returns the lowest index such that the set of indices smaller
     * or equal of active parties is at least {@link #threshold}.
     *
     * @return Lowest index such that the set of indices smaller or
     * equal of active parties is at least {@link #threshold}.
     */
    public int getActiveThreshold() {
        final boolean[] active = getActives();

        int count = 0;
        int i;
        for (i = 1; count < threshold && i <= k; i++) {
            if (active[i]) {
                count++;
            }
        }

        return i - 1;
    }
}
