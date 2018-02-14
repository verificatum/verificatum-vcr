
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
import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.protocol.ProtocolError;
import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;


/**
 * This is a bulletin board that is used by a single semi-trusted
 * party, i.e., it is only useful if the mix-net is used with a single
 * party.
 *
 * @author Douglas Wikstrom
 */
public final class BullBoardBasicSingle extends BullBoardBasic {

    /**
     * Milliseconds we wait between attempts to read a file we expect
     * to be published.
     */
    public static final int SLEEP_TIME = 100;

    /**
     * Variable used to lock access to directory.
     */
    private final Object lock;

    /**
     * Create an instance of the bulletin board.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     */
    public BullBoardBasicSingle(final PrivateInfo privateInfo,
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
        lock = new Object();
    }

    /**
     * Starts this bulletin board.
     *
     * @param log Logging context.
     */
    @Override
    public void start(final Log log) {
    }

    /**
     * Stops this bulletin board.
     *
     * @param log Logging context.
     */
    @Override
    public void stop(final Log log) {
    }

    /**
     * Publishes a message.
     *
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to be published.
     * @param maximalWaitTime Maximal amount of time waiting to
     * download a message.
     * @param log Logging context.
     */
    @Override
    public void publish(final String messageLabel,
                        final ByteTreeBasic message,
                        final int maximalWaitTime,
                        final Log log) {

        final File file = new File(directory, messageLabel);
        final File parent = file.getParentFile();

        try {
            ExtIO.mkdirs(parent);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to create directory!", eioe);
        }

        synchronized (lock) {
            message.unsafeWriteTo(file);
        }
    }

    /**
     * Remove everything published under labels with the given prefix.
     *
     * @param messageLabelPrefix Label prefix of messages to be
     * removed.
     */
    @Override
    public void unpublish(final String messageLabelPrefix) {
        synchronized (lock) {

            final File file = new File(directory, messageLabelPrefix);

            if (file.exists()) {
                ExtIO.delete(file);
            }
        }
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters.
     *
     * @param l Index of the party that wrote the message to be read.
     * @param messageLabel Name of the file to be read.
     * @param addedTime Additional milliseconds to wait due to
     * computations performed by the publisher in the application
     * layer. This implementation ignores this value.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    @Override
    public ByteTreeBasic waitFor(final int l,
                                 final String messageLabel,
                                 final int addedTime,
                                 final long maximalByteLength,
                                 final int maximalRecursiveDepth,
                                 final Log log) {

        final File file = new File(directory, messageLabel);

        for (;;) {
            synchronized (lock) {

                if (!ByteTreeF.verifyFormat(file, maximalRecursiveDepth)) {
                    return null;
                }

                if (file.canRead()) {
                    return new ByteTreeF(file);
                }
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (final InterruptedException ie) {
            }
        }
    }

    @Override
    public long getReceivedBytes() {
        return 0;
    }

    @Override
    public long getSentBytes() {
        return 0;
    }
}
