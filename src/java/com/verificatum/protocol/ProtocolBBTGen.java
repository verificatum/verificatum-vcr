
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
