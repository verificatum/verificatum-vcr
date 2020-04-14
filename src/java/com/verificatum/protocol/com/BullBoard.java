
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

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.protocol.Protocol;
import com.verificatum.protocol.ProtocolBB;
import com.verificatum.ui.Log;

/**
 * This class allows several protocols to share a single instance of
 * {@link BullBoardBasic} in the natural hierarchical way using unique
 * instance identifiers. Essentially each instance of this class is
 * given its own "scope" of the bulletin board where messages can be
 * published or read.
 *
 * @author Douglas Wikstrom
 */
public class BullBoard extends Protocol {

    /**
     * Maximal amount of time waiting to download a message.
     */
    protected int defaultMaximalWaitTime;

    /**
     * Default maximal number of bytes of downloaded message.
     */
    protected long defaultMaximalByteLength;

    /**
     * Default maximal recursive depth of downloaded message.
     */
    protected int defaultMaximalRecursiveDepth;

    /**
     * Provides the underlying functionality.
     */
    protected BullBoardBasic bullBoardBasic;

    /**
     * Creates an instance.
     *
     * @param protocol Protocol which invokes this one.
     * @param bullBoardBasic Underlying bulletin board shared by all
     * instances of this class of a party.
     * @param defaultMaximalWaitTime Maximal amount of time waiting to
     * download a message.
     * @param defaultMaximalByteLength Default maximal number of bytes
     * of downloaded message.
     * @param defaultMaximalRecursiveDepth Default maximal recursive
     * depth of downloaded message.
     */
    public BullBoard(final ProtocolBB protocol,
                     final BullBoardBasic bullBoardBasic,
                     final int defaultMaximalWaitTime,
                     final long defaultMaximalByteLength,
                     final int defaultMaximalRecursiveDepth) {
        super("BullBoard", protocol);

        this.bullBoardBasic = bullBoardBasic;
        this.defaultMaximalWaitTime = defaultMaximalWaitTime;
        this.defaultMaximalByteLength = defaultMaximalByteLength;
        this.defaultMaximalRecursiveDepth = defaultMaximalRecursiveDepth;
    }

    /**
     * Sets the total network time to zero.
     */
    public void clearTotalNetworkTime() {
        bullBoardBasic.clearTotalNetworkTime();
    }

    /**
     * Returns the total milliseconds of network activity after the
     * initial successful message. This does not include the time
     * waiting for a party to complete its computations.
     *
     * @return Time used for communication so far.
     */
    public long getTotalNetworkTime() {
        return bullBoardBasic.getTotalNetworkTime();
    }

    /**
     * Returns the total milliseconds of waiting for computations to
     * be performed by other parties.
     *
     * @return Time used for waiting so far.
     */
    public long getTotalWaitingTime() {
        return bullBoardBasic.getTotalWaitingTime();
    }

    /**
     * Unpublishes the contents of this bulletin board.
     */
    public void unpublish() {
        bullBoardBasic.unpublish(parent.getFullName());
    }

    /**
     * Creates an instance associated with the protocol
     * <code>protocol</code>.
     *
     * @param protocol Protocol invoking this one.
     * @param bullBoard Bulletin board of which this instance is a
     * child.
     */
    public BullBoard(final ProtocolBB protocol, final BullBoard bullBoard) {

        // This sid has special meaning for Protocol. Do not change!
        super("BullBoard", protocol);

        this.bullBoardBasic = bullBoard.bullBoardBasic;
        this.defaultMaximalWaitTime = bullBoard.defaultMaximalWaitTime;
        this.defaultMaximalByteLength = bullBoard.defaultMaximalByteLength;
        this.defaultMaximalRecursiveDepth =
            bullBoard.defaultMaximalRecursiveDepth;
    }

    /**
     * Starts the underlying server unless it is already running.
     *
     * @param log Logging context.
     */
    public void start(final Log log) {
        bullBoardBasic.start(log);
    }

    /**
     * Stops the underlying basic server if it is running.
     *
     * @param log Logging context.
     */
    public void stop(final Log log) {
        bullBoardBasic.stop(log);
    }

    /**
     * Makes the labels of this instance globally unique.
     *
     * @param label Basic label.
     * @return Input label prepended with the full name of this
     * instance.
     */
    protected String marshal(final String label) {
        return getFullName() + "/" + label;
    }

    /**
     * Writes the string message on the bulletin board under the given
     * label.
     *
     * @param label Label under which the entry should be stored.
     * @param message Entry to be stored.
     * @param log Log context.
     */
    public void publish(final String label,
                        final ByteTreeBasic message,
                        final Log log) {

        final String fullLabel = marshal(label);
        bullBoardBasic.publish(fullLabel, message, defaultMaximalWaitTime, log);
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters.
     *
     * @param l Index of the party that wrote the message to be read.
     * @param label Label of the message to be read.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    public ByteTreeReader waitFor(final int l,
                                  final String label,
                                  final Log log) {
        return waitFor(l,
                       label,
                       defaultMaximalWaitTime,
                       defaultMaximalByteLength,
                       defaultMaximalRecursiveDepth,
                       log);
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
     *
     * @param l Index of the party that wrote the message to be read.
     * @param label Label of the message to be read.
     * @param maximalWaitTime Maximal amount of time waiting to
     * download a message.
     * @param maximalByteLength Maximal number of bytes in the
     * downloaded message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    public ByteTreeReader waitFor(final int l,
                                  final String label,
                                  final int maximalWaitTime,
                                  final long maximalByteLength,
                                  final int maximalRecursiveDepth,
                                  final Log log) {

        // Store the current time and how much time was used up to
        // this point for network activity.
        final long startTime = System.currentTimeMillis();
        final long totalNetworkTime =
            bullBoardBasic.getTotalNetworkTime();

        final String fullLabel = marshal(label);
        final ByteTreeBasic bt =
            bullBoardBasic.waitFor(l,
                                   fullLabel,
                                   maximalWaitTime,
                                   maximalByteLength,
                                   maximalRecursiveDepth,
                                   log);

        // Compute how much time was spent for network activity in the
        // call above.
        final long networkTimeDelta =
            bullBoardBasic.getTotalNetworkTime() - totalNetworkTime;

        // The amount of waiting in this invocation of this function
        // is the time passed during this invocation minus the time
        // spent for network activity.
        final long waitTime =
            System.currentTimeMillis() - startTime - networkTimeDelta;

        bullBoardBasic.addToTotalWaitingTime(waitTime);

        return bt.getByteTreeReader();
    }

    /**
     * Returns number of received bytes.
     *
     * @return Number of received bytes.
     */
    public long getReceivedBytes() {
        return bullBoardBasic.getReceivedBytes();
    }


    /**
     * Returns the total number of bytes sent.
     *
     * @return Total number of bytes sent.
     */
    public long getSentBytes() {
        return bullBoardBasic.getSentBytes();
    }
}
