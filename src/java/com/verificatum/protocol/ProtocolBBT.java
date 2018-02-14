
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
