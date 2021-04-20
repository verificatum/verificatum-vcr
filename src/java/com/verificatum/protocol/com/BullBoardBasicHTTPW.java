
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

import java.net.InetSocketAddress;
import java.net.URL;

import com.verificatum.crypto.Hashfunction;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.protocol.ProtocolError;
import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.info.PartyInfo;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;
import com.verificatum.util.Pair;
import com.verificatum.util.Timer;
import com.verificatum.util.Triple;


/**
 * Abstract base class for distributed bulletin board. It contains
 * classes for downloading messages of bounded size, and downloading
 * and verifying signatures from other parties within bounded time.
 *
 * <p>
 *
 * Subclass this class to implement the logics of a particular
 * distributed bulletin board.
 *
 * @author Douglas Wikstrom
 */
public final class BullBoardBasicHTTPW extends BullBoardBasicHTTP {

    /**
     * Error message used for invalid server addresses.
     */
    public static final String INVALID_ADDRESS = "Invalid hint server address!";

    /**
     * Name of the hint-server tag.
     */
    public static final String HINT = "hint";

    /**
     * Name of listening hint server type tag.
     */
    public static final String HINTL = "hintl";

    /**
     * Default upper bound on signature byte length.
     */
    public static final int DEFAULT_MAXIMAL_SIGNATURE_BYTE_LENGTH =
        1000 * 1024;

    /**
     * Socket addresses of the hint servers of all parties.
     */
    InetSocketAddress[] hints;

    /**
     * Socket address at which the hint server of this party listens,
     * which may be different from the socket address used by other
     * parties, e.g., if this party is behind a NAT.
     */
    InetSocketAddress hintServerAddress;

    /**
     * Hint server of this instance.
     */
    HintServer hintServer;

    /**
     * Upper bound on the byte length of a signature.
     */
    int maximalSignatureByteLength;

    /**
     * Joint hashfunction used to compress messages before signing.
     */
    Hashfunction jointHashfunction;

    /**
     * Creates a bulletin board configured using the values in the
     * info instance.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     */
    public BullBoardBasicHTTPW(final PrivateInfo privateInfo,
                               final ProtocolInfo protocolInfo,
                               final UI ui) {
        super(privateInfo, protocolInfo, ui);

        // We do not use a joint hashfunction for now.
        this.jointHashfunction = null;

        this.maximalSignatureByteLength = DEFAULT_MAXIMAL_SIGNATURE_BYTE_LENGTH;

        this.hints = new InetSocketAddress[k + 1];

        // Addresses of hint servers.
        String hintString = null;
        try {
            for (int i = 1; i <= k; i++) {

                final PartyInfo pi = protocolInfo.get(i);
                hintString = pi.getStringValue(HINT);

                final String[] s = hintString.split(":");
                if (s.length != 2) {
                    throw new ProtocolError(INVALID_ADDRESS + " ("
                                            + hintString + ")");
                }

                final int port = Integer.parseInt(s[1]);
                this.hints[i] = new InetSocketAddress(s[0], port);
            }
        } catch (final NumberFormatException nfe) {
            throw new ProtocolError(INVALID_ADDRESS + " (" + hintString + ")",
                                    nfe);
        }

        // Address of hint server of this instance.
        final String hintl = privateInfo.getStringValue(HINTL);
        final String[] s = hintl.split(":");
        if (s.length != 2) {
            throw new ProtocolError(INVALID_ADDRESS + " (" + hintl + ")");
        }
        try {
            final int port = Integer.parseInt(s[1]);
            this.hintServerAddress = new InetSocketAddress(s[0], port);
        } catch (final NumberFormatException nfe) {
            throw new ProtocolError(INVALID_ADDRESS + " (" + hintl + ")", nfe);
        }
    }

    // Documented in BullBoardBasic.java

    @Override
    public void start(final Log log) {
        if (!running) {

            log.info("Starting hint server.");
            hintServer = new HintServer(hintServerAddress, k);
            hintServer.start();

            super.start(log);
        }
    }

    @Override
    public void stop(final Log log) {
        if (running) {

            log.info("Stopping hint server.");
            hintServer.stop();

            super.stop(log);
        }
    }

    @Override
    protected URL http(final int loc) {
        return http[loc];
    }

    @Override
    protected void signalWrite() {

        // We signal everybody except ourselves.
        //
        // Note that it may happen that some servers that are not
        // downloading anything gets a hint anyway, which then
        // incorrectly triggers an immediate download attempt. To
        // avoid this one would need this method to be application
        // dependent which makes no sense.

        for (int l = 1; l <= k; l++) {
            if (l != j) {
                HintServer.hint(j, hints[l]);
            }
        }
    }

    @Override
    protected void waitForAtMost(final int l, final long waitTime) {

        final Sleeper sleeper = new Sleeper(waitTime);

        hintServer.setListener(l, sleeper);

        synchronized (sleeper) {

            sleeper.start();

            // We must manually check that no hint has already been
            // received. Note that this can not be done before
            // starting the thread, since the hint could come
            // inbetween these lines.
            hintServer.checkHint(l);

            boolean alive = true;
            while (alive) {
                try {
                    sleeper.wait();
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                alive = false;
            }
        }
    }

    /**
     * Publishes the message under the label.
     *
     * @param messageLabel Label under which the message is published.
     * @param message Published message.
     * @param maximalWaitTime Maximal amount of time waiting to
     * download a message.
     * @param log Logging context.
     */
    @Override
    public void publish(final String messageLabel,
                        final ByteTreeBasic message,
                        final int maximalWaitTime,
                        final Log log) {

        ByteTreeBasic actualMessage = message;

        writeMessage(messageLabel, actualMessage, log);

        // If a joint hashfunction is used, then we use it to compress
        // the message before signing.
        byte[] jointDigest = null;
        if (jointHashfunction != null) {

            jointDigest = jointDigestOfMessage(j,
                                               messageLabel,
                                               actualMessage,
                                               jointHashfunction);
            actualMessage = null;
        }
        writeSignature(j, messageLabel, actualMessage, jointDigest, log);

        waitFor(j, messageLabel, maximalWaitTime, 0, 0, log,
                actualMessage, jointDigest);
    }

    @Override
    public ByteTreeBasic waitFor(final int l,
                                 final String messageLabel,
                                 final int maximalWaitTime,
                                 final long maximalByteLength,
                                 final int maximalRecursiveDepth,
                                 final Log log) {
        return waitFor(l,
                       messageLabel,
                       maximalWaitTime,
                       maximalByteLength,
                       maximalRecursiveDepth,
                       log,
                       null,
                       null);
    }

    /**
     * Download signatures of all relevant parties.
     *
     * @param l Index of publisher.
     * @param signatureIndex Index of signature.
     * @param messageLabel Label of published message.
     * @param message Message.
     * @param jointDigest Joint digest of the message.
     * @param timer Timer that determines for how long we try to
     * download signatures.
     * @param log Logging context.
     * @return Index from which to start downloading messages if
     * restarted.
     */
    protected int downloadSignatures(final int l,
                                     final int signatureIndex,
                                     final String messageLabel,
                                     final ByteTreeBasic message,
                                     final byte[] jointDigest,
                                     final Timer timer,
                                     final Log log) {

        int index = signatureIndex;

        // This is false iff a valid signature could not be downloaded
        // from some party.
        boolean verdict = true;

        // Provided that we have successfully downloaded a message and
        // valid signature from the publisher, we try to download the
        // signatures of other parties and sign the message downloaded
        // from the publisher.
        while (0 < index && index <= k && verdict) {

            // Ignore parties that are not active.
            if (getActive(index)) {

                // If it is our turn, we make our signature of the
                // message available on our HTTP server.

                if (index == j) {

                    writeSignature(l,
                                   messageLabel,
                                   message,
                                   jointDigest,
                                   log);

                // If it is not our turn, then we only download a
                // signature, unless it is the publishers turn. We
                // have already downloaded that signature.
                } else if (index != l) {

                    final Pair<Boolean, Long> fetchVerdict =
                        readSignature(index,
                                      l,
                                      messageLabel,
                                      index,
                                      message,
                                      jointDigest,
                                      maximalSignatureByteLength,
                                      timer,
                                      log);

                    verdict = fetchVerdict.first;
                }
            }

            if (verdict) {
                index++;
            }
        }
        return index;
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
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
     * @param message The publisher should input the message to be
     * published here and otherwise it must be null.
     * @param jointDigest Joint digest of the message.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    protected ByteTreeBasic waitFor(final int l,
                                    final String messageLabel,
                                    final int maximalWaitTime,
                                    final long maximalByteLength,
                                    final int maximalRecursiveDepth,
                                    final Log log,
                                    final ByteTreeBasic message,
                                    final byte[] jointDigest) {

        // If the publisher is not active, then we short-circuit the
        // method and return a fixed simple byte tree.
        if (!getActive(l)) {
            return new ByteTree();
        }

        ByteTreeBasic actualMessage = message;

        byte[] jd = jointDigest;

        // Zero indicates that we download a signature from the
        // publisher.
        int signatureIndex = 0;

        Timer timer = new Timer(maximalWaitTime);

        long startTime = 0;

        for (;;) {

            if (signatureIndex == 0) {

                if (l == j) {

                    // If we are the publisher, then we do nothing. We
                    // obviously produced a valid signature of our own
                    // message.

                    if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                    }

                    signatureIndex++;

                } else {

                    // If we are not the publisher, we try to download the
                    // message and a corresponding valid signature of the
                    // publisher on the message directly from the
                    // publisher.

                    final Triple<ByteTreeBasic, byte[], Long> triple =
                        readMessAndSig(l,
                                       l,
                                       messageLabel,
                                       maximalByteLength,
                                       maximalRecursiveDepth,
                                       maximalSignatureByteLength,
                                       timer,
                                       log,
                                       jointHashfunction);
                    actualMessage = triple.first;
                    jd = triple.second;

                    if (startTime == 0) {
                        startTime = System.currentTimeMillis() - triple.third;
                    }

                    // If a message and signature was successfully
                    // downloaded, then we try to download signatures.
                    if (actualMessage != null) {
                        signatureIndex++;
                    }
                }
            }

            signatureIndex = downloadSignatures(l,
                                                signatureIndex,
                                                messageLabel,
                                                actualMessage,
                                                jd,
                                                timer,
                                                log);


            if (signatureIndex > k) {

                // If we have downloaded the message and valid
                // signatures from all parties, then we simply return
                // the message.

                addToTotalNetworkTime(System.currentTimeMillis() - startTime);

                return actualMessage;
            }

            final String q = "The bulletin board failed. "
                + "You may agree with the administrators of the other "
                + "mix-servers to make another attempt to complete the "
                + "request to the bulletin board. Otherwise the "
                + "protocol will halt securely without an output. Would you "
                + "like to try again?";

            if (ui.dialogQuery(q)) {
                timer = new Timer(maximalWaitTime);
            } else {
                throw new Error("No error resolution is implemented yet!");
            }
        }
    }
}
