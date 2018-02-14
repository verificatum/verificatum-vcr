
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

import java.io.File;

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.protocol.Protocol;
import com.verificatum.protocol.ProtocolError;
import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;


/**
 * This abstract base class represents a bulletin board. Protocols
 * should not use any subclass of this protocol directly. Use instead
 * the class {@link BullBoard} and initialize it with a subclass of
 * this class.
 *
 * <p>
 *
 * If you want to implement your own bulletin board, then:
 *
 * <ul>
 *
 * <li>You must implement a subclass of {@link BullBoardBasicGen}
 * named <code>MyBullBoardBasicGen</code>, where
 * <code>MyBullBoardBasic</code> is the name of your bulletin board,
 * that adds the fields to the protocol info and private info that you
 * need to configure your bulletin board, and optionally, default
 * values for some of these fields. Look at
 * {@link BullBoardBasicHTTPGen} and {@link BullBoardBasicHTTPWGen}
 * for examples.
 *
 * <li>Your own bulletin board, <code>MyBullBoardBasic</code> must
 * inherit this class (i.e., <code>BullBoardBasic</code>) and provide
 * a constructor taking the following parameters in the given order:
 * {@link PrivateInfo}, {@link ProtocolInfo}, {@link UI},
 * <code>boolean[]</code>. Look at {@link BullBoardBasicHTTP} and
 * {@link BullBoardBasicHTTPW} for examples.
 *
 * <p>
 *
 * Upon invocation, the first two info instances will contain all info
 * fields needed by a protocol using your bulletin board, i.e., the
 * fields added by {@link com.verificatum.protocol.ProtocolBBGen}, as
 * well as the fields added by your own subclass of {@link
 * BullBoardBasicGen} needed to configure your bulletin board.
 *
 * <li> {@link #waitFor} must return an empty byte tree if the given
 * party is not active. An empty byte tree can be created using
 * {@link com.verificatum.eio.ByteTree#ByteTree()}.
 *
 * </ul>
 *
 * @author Douglas Wikstrom
 */
public abstract class BullBoardBasic extends Protocol {

    /**
     * Total time spent during network activity.
     */
    protected long totalNetworkTime;

    /**
     * Total time spent waiting for computations to be performed by
     * other parties.
     */
    protected long totalWaitingTime;

    /**
     * Create an instance of the bulletin board.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     */
    protected BullBoardBasic(final PrivateInfo privateInfo,
                             final ProtocolInfo protocolInfo,
                             final UI ui) {
        super(privateInfo, protocolInfo, ui);

        // Create our own sub directory if it does not exist.
        directory = new File(directory, getNameAndSid());

        try {
            ExtIO.mkdirs(directory);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to create directory!", eioe);
        }
    }

    /**
     * Sets the total network time to zero.
     */
    public void clearTotalNetworkTime() {
        synchronized (this) {
            totalNetworkTime = 0;
        }
    }

    /**
     * Update the total network time.
     *
     * @param networkTime Additional network time.
     */
    protected void addToTotalNetworkTime(final long networkTime) {
        synchronized (this) {
            totalNetworkTime = totalNetworkTime + networkTime;
        }
    }

    /**
     * Update the total waiting time.
     *
     * @param waitingTime Additional waiting time.
     */
    protected void addToTotalWaitingTime(final long waitingTime) {
        synchronized (this) {
            totalWaitingTime = totalWaitingTime + waitingTime;
        }
    }

    /**
     * Returns the total milliseconds of network activity after the
     * initial successful message. This does not include the time
     * waiting for a party to complete its computations.
     *
     * @return Time used for communication so far.
     */
    public long getTotalNetworkTime() {
        return totalNetworkTime;
    }

    /**
     * Returns the total milliseconds of waiting for computations to
     * be performed by other parties.
     *
     * @return Time used for waiting so far.
     */
    public long getTotalWaitingTime() {
        return totalWaitingTime;
    }

    /**
     * Starts this bulletin board.
     *
     * @param log Logging context.
     */
    public abstract void start(Log log);

    /**
     * Stops this bulletin board.
     *
     * @param log Logging context.
     */
    public abstract void stop(Log log);

    /**
     * Publishes a message. This includes interaction with other
     * parties.
     *
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to be published.
     * @param maximalWaitTime Maximal amount of time waiting to
     * download a message.
     * @param log Logging context.
     */
    public abstract void publish(String messageLabel,
                                 ByteTreeBasic message,
                                 int maximalWaitTime,
                                 Log log);

    /**
     * Remove everything published under labels with the given prefix.
     *
     * @param messageLabelPrefix Label prefix of messages to be
     * removed.
     */
    public abstract void unpublish(String messageLabelPrefix);

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters.
     *
     * @param l Index of the party that wrote the message to be read.
     * @param messageLabel Name of the file to be read.
     * @param maximalWaitTime Maximal amount of time waiting to
     * download a message.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    public abstract ByteTreeBasic waitFor(int l,
                                          String messageLabel,
                                          int maximalWaitTime,
                                          long maximalByteLength,
                                          int maximalRecursiveDepth,
                                          Log log);

    /**
     * Returns the total number of received bytes.
     *
     * @return Total number of received bytes.
     */
    public abstract long getReceivedBytes();

    /**
     * Returns the total number of bytes sent.
     *
     * @return Total number of bytes sent.
     */
    public abstract long getSentBytes();
}
