
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

import com.verificatum.protocol.com.BullBoardBasicGen;
import com.verificatum.ui.info.InfoException;
import com.verificatum.ui.info.IntField;
import com.verificatum.ui.info.ProtocolInfo;

/**
 * Defines additional fields and default values used by
 * {@link ProtocolBB}.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolBBTGen extends ProtocolBBGen {

    /**
     * Creates an instance for a given implementation of a bulletin
     * board.
     *
     * @param bbbg Adds the values needed by the particular
     * instantiation of bulletin board used.
     */
    public ProtocolBBTGen(final BullBoardBasicGen bbbg) {
        super(bbbg);
    }

    /**
     * Creates an instance for the default bulletin board.
     */
    public ProtocolBBTGen() {
        super();
    }

    /**
     * Threshold number of parties needed to violate privacy, i.e.,
     * this is the number of parties needed to decrypt.
     */
    public static final String THRESHOLD_DESCRIPTION =
        "Threshold number of parties needed to violate the privacy of the "
        + "protocol, i.e., this is the number of parties needed to decrypt. "
        + "This must be positive, but at most equal to the number of parties.";

    @Override
    public void addProtocolInfo(final ProtocolInfo pri) {
        super.addProtocolInfo(pri);

        final IntField thresField =
            new IntField(ProtocolBBT.THRESHOLD,
                         THRESHOLD_DESCRIPTION, 1, 1, 1,
                         ProtocolGen.MAX_NOPARTIES);
        pri.addInfoFields(thresField);
    }

    // There is no default number for the threshold.

    @Override
    public void validateLocal(final ProtocolInfo pri) throws InfoException {
        super.validateLocal(pri);
        final int noParties = pri.getIntValue(Protocol.NOPARTIES);
        final int threshold = pri.getIntValue(ProtocolBBT.THRESHOLD);
        if (threshold > noParties) {
            throw new InfoException("Threshold is greater than number of "
                                    + "parties! (" + threshold + " > "
                                    + noParties + ")");
        }
    }
}
