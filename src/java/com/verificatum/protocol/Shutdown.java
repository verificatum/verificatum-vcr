
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
