
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
                Thread.currentThread().interrupt();
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
