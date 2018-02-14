
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

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.ui.Log;

/**
 * The trivial protocol where each party generates a key pair of a
 * cryptosystem and then shares it with the other parties.
 *
 * @author Douglas Wikstrom
 */
public final class Shutdown extends ProtocolBB {

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     */
    public Shutdown(final String sid, final ProtocolBB protocol) {
        super(sid, protocol);
    }

    /**
     * Shut down the protocol in a synchronized way.
     *
     * @param log Logging context.
     */
    public void execute(final Log log) {

        final String label = "shutdown";

        log.info("Waiting for mutual shutdown acknowledgements.");

        final Log tempLog = log.newChildLog();

        executeInner(tempLog, label + "_first_round");
        executeInner(tempLog, label + "_second_round");

        log.info("Allow download of our acknowledgment for another "
                 + (WAIT_FOR_OTHERS_TIME / 1000) + " seconds.");

        try {
            Thread.sleep(WAIT_FOR_OTHERS_TIME);
        } catch (final InterruptedException ie) {
            // User requested immediate shutdown. Do nothing.
        }
        bullBoard.stop(log);
    }

    /**
     * Execute one round of synchronized shut down.
     *
     * @param log Logging context.
     * @param label Unique label among invocations of this class.
     */
    public void executeInner(final Log log, final String label) {

        for (int i = 1; i <= k; i++) {

            if (getActive(i)) {

                if (i == j) {

                    log.info("Publish acknowledgement.");
                    bullBoard.publish(label, new ByteTree(), log);

                } else {

                    log.info("Wait for " + ui.getDescrString(i)
                             + " to acknowledge.");
                    final ByteTreeReader btr =
                        bullBoard.waitFor(i, label, log);
                    btr.close();
                }
            }
        }
    }
}
