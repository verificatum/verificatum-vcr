
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
import java.net.MalformedURLException;
import java.net.URL;

import com.verificatum.crypto.Hashdigest;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.SignatureKeyPair;
import com.verificatum.crypto.SignaturePKey;
import com.verificatum.crypto.SignatureSKey;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Marshalizer;
import com.verificatum.protocol.Protocol;
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
 * Abstract base class for distributed bulletin board using HTTP to
 * communicate. It contains classes for downloading messages of
 * bounded size, and downloading and verifying signatures from other
 * parties within bounded time.
 *
 * <p>
 *
 * If you intend to implement a distributed bulletin board, then one
 * alternative is to subclass this class.
 *
 * @author Douglas Wikstrom
 */
public abstract class BullBoardBasicHTTP extends BullBoardBasic {

    /**
     * Name of the public key tag.
     */
    public static final String PUB_KEY = "pkey";

    /**
     * Name of private key tag.
     */
    public static final String PRIV_KEY = "skey";

    /**
     * Name of directory of http server tag.
     */
    public static final String HTTPDIR = "httpdir";

    /**
     * Name of http server type tag.
     */
    public static final String HTTP_TYPE = "httptype";

    /**
     * Name of the http-server tag.
     */
    public static final String HTTP = "http";

    /**
     * Name of listening http server type tag.
     */
    public static final String HTTPL = "httpl";

    /**
     * Default number of milliseconds to wait inbetween download
     * attempts.
     */
    public static final int DEFAULT_PAUSE_TIME = 100;

    /**
     * Number of milliseconds we pause inbetween download attempts.
     * Subclasses are expected to minimize this time by using
     * interrupts.
     */
    public int pauseTime;

    /**
     * URLs to the HTTP servers of all parties.
     */
    protected URL[] http;

    /**
     * Directory where our HTTP server is rooted.
     */
    protected File httpdir;

    /**
     * URL at which this party listens, which may be different from
     * the URL used by other parties, e.g., if this party is behind a
     * NAT.
     */
    protected URL httpl;

    /**
     * Public signature keys of all parties.
     */
    protected SignaturePKey[] pkeys;

    /**
     * Private signature key.
     */
    protected SignatureSKey skey;

    /**
     * HTTP server of this instance.
     */
    protected SimpleHTTPServer httpServer;

    /**
     * Client used to fetch data.
     */
    protected SimpleHTTPClient simpleClient;

    /**
     * Maximal number of concurrent connections to the HTTP server.
     */
    protected int backLog;

    /**
     * Indicates if this server is running or not.
     */
    protected boolean running;

    /**
     * Indicates if this bulletin board uses an external HTTP server.
     */
    protected boolean external;

    /**
     * Creates an instance.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     */
    public BullBoardBasicHTTP(final PrivateInfo privateInfo,
                              final ProtocolInfo protocolInfo,
                              final UI ui) {

        super(privateInfo, protocolInfo, ui);

        final int certainty = privateInfo.getIntValue(Protocol.CERTAINTY);

        // Parse the public information of other servers.
        http = new URL[k + 1];
        pkeys = new SignaturePKey[k + 1];

        try {
            for (int i = 1; i <= k; i++) {

                final PartyInfo pi = protocolInfo.get(i);
                http[i] = new URL(pi.getStringValue(HTTP));

                final String pkeyString = pi
                    .getStringValue(BullBoardBasicHTTP.PUB_KEY);

                pkeys[i] =
                    Marshalizer.unmarshalHexAux_SignaturePKey(pkeyString,
                                                              randomSource,
                                                              certainty);
            }
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to use public keys!", eioe);
        } catch (final MalformedURLException murle) {
            throw new ProtocolError("Malformed party info!", murle);
        }

        // Make the directory of our HTTP server.
        httpdir = new File(privateInfo.getStringValue(HTTPDIR));
        try {
            ExtIO.mkdirs(httpdir);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to create http directory!", eioe);
        }

        skey = null;
        try {
            final String keyPairString = privateInfo
                .getStringValue(BullBoardBasicHTTP.PRIV_KEY);
            final SignatureKeyPair keyPair =
                Marshalizer.unmarshalHexAux_SignatureKeyPair(keyPairString,
                                                             randomSource,
                                                             certainty);
            skey = keyPair.getSKey();
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to use secret key!", eioe);
        }

        external = privateInfo.getStringValue(HTTP_TYPE).equals("external");

        // If the server is run behind a firewall with portforwarding,
        // the local hostname may differ from the one used by other
        // parties.
        try {
            httpl = new URL(privateInfo.getStringValue(HTTPL));
        } catch (final MalformedURLException murle) {
            throw new ProtocolError("Malformed party info!", murle);
        }

        // We should probably allow programmer to change this value.
        this.pauseTime = DEFAULT_PAUSE_TIME;

        simpleClient = new SimpleHTTPClient();
    }

    /**
     * Reports the number of bytes received by this bulletin board.
     *
     * @return Number of bytes received by this bulletin board.
     */
    public long getReceivedBytes() {
        return simpleClient.getReceivedBytes();
    }


    /**
     * Reports the number of bytes sent by this bulletin board.
     *
     * @return Number of bytes sent by this bulletin board.
     */
    public long getSentBytes() {
        return httpServer.getSentBytes();
    }

    /**
     * Returns a URL to what is logically the data of the party with
     * the given index. Depending on how this is implemented this
     * party may actually download from the party with the given index
     * or from some other source.
     *
     * @param loc Index of party.
     * @return A URL.
     */
    protected abstract URL http(int loc);

    /**
     * Signal that something was written to its HTTP server by this
     * party. The bulletin boards of other parties may receive the
     * signal and interrupt waiting in {@link #waitForAtMost(int,long)}
     * .
     */
    protected abstract void signalWrite();

    /**
     * Wait for at most the given number of milliseconds or until
     * interrupted. The interruption may be the result of another
     * party calling its {@link #signalWrite()} method.
     *
     * @param l Index of party from which we are waiting for an
     * interruption.
     * @param waitTime Maximal number of milliseconds to wait.
     */
    protected abstract void waitForAtMost(int l, long waitTime);

    // ############### Reads and writes raw data ##################

    /**
     * Reads the given data from the party with index
     * <code>loc</code>. The output is guaranteed to represent a
     * proper byte tree with no spurious bytes at the end.
     *
     * @param loc Index of party that should have put the data on its
     * HTTP server.
     * @param relativeFileName Relative filename of requested data.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param readTimeout Maximal time spent waiting for the data. A
     * negative value is interpreted as infinity.
     * @param log Log context.
     * @return Byte tree representation of the data downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected Pair<ByteTreeBasic, Long>
        readData(final int loc,
                 final String relativeFileName,
                 final long maximalByteLength,
                 final int maximalRecursiveDepth,
                 final long readTimeout,
                 final Log log) {

        // Attempt to fetch the data.
        final Pair<Boolean, Long> fetchResult =
            simpleClient.fetchFile(http(loc),
                                   directory,
                                   relativeFileName,
                                   readTimeout,
                                   maximalByteLength,
                                   log);
        final boolean result = fetchResult.first;
        long readTime = fetchResult.second;

        // Did we download anything?
        if (result) {

            // Verify that the downloaded file represents a valid
            // byte tree that is not too deep.
            final File file = new File(directory, relativeFileName);

            final long startTime = System.currentTimeMillis();

            readTime = readTime + System.currentTimeMillis() - startTime;

            if (ByteTreeF.verifyFormat(file, maximalRecursiveDepth)) {

                // If everything is ok we return data that is now
                // guaranteed to represent a proper byte tree of
                // limited total size and with limited depth.
                return new Pair<ByteTreeBasic, Long>(new ByteTreeF(file),
                                                     readTime);

            } else {
                return new Pair<ByteTreeBasic, Long>(null, readTime);
            }


        } else {

            return new Pair<ByteTreeBasic, Long>(null, readTime);
        }
    }

    /**
     * Spends at most the time given by the timer reading the given
     * data from the party with index <code>loc</code>.
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param relativeFileName Filename of requested information.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param timer Timer that keeps track of how long we may try to
     * download.
     * @param log Log context.
     * @return Byte tree representation of the file downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected Pair<ByteTreeBasic, Long>
        readData(final int loc,
                 final String relativeFileName,
                 final long maximalByteLength,
                 final int maximalRecursiveDepth,
                 final Timer timer,
                 final Log log) {

        // Note that we do not include the time for attempts in our
        // estimate of the milliseconds of network time.

        while (!timer.timeIsUp()) {

            final Pair<ByteTreeBasic, Long> fetchData =
                readData(loc,
                         relativeFileName,
                         maximalByteLength,
                         maximalRecursiveDepth,
                         timer.remainingTime(),
                         log);

            if (fetchData.first == null) {

                long waitTime = timer.remainingTime();
                if (waitTime < 0) {
                    waitTime = pauseTime;
                } else {
                    waitTime = Math.min(pauseTime, waitTime);
                }
                waitForAtMost(loc, waitTime);

            } else {

                return fetchData;
            }
        }
        return new Pair<ByteTreeBasic, Long>((ByteTreeBasic) null, (long) 0);
    }

    /**
     * Puts the data on the HTTP server.
     *
     * @param relativeFileName Filename where data is stored.
     * @param data Data to be stored.
     * @param log Log context.
     * @return Number of bytes in data.
     */
    protected long writeData(final String relativeFileName,
                             final ByteTreeBasic data,
                             final Log log) {

        // Write to root of HTTP server.
        final File file = new File(httpdir, relativeFileName);

        // Make sure directory exists.
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            try {
                ExtIO.mkdirs(parent);
            } catch (final EIOException eioe) {
                throw new ProtocolError("Unable to make directory! ("
                                        + parent + ")",
                                        eioe);
            }
        }

        // Write data atomically. This is thread safe in the sense
        // that distinct instances have distinct directories anyway.
        final String tmpName =
            file.getName() + "_ " + System.currentTimeMillis();
        final File tmpFile = new File(parent, tmpName);

        data.unsafeWriteTo(tmpFile);

        if (!tmpFile.renameTo(file)) {
            throw new ProtocolError("Unable to write file atomically! "
                                    + "(renaming from "
                                    + tmpFile.toString() + " to "
                                    + file.toString() + ")");
        }

        signalWrite();

        return file.length();
    }

    @Override
    public void unpublish(final String messageLabelPrefix) {

        for (int l = 1; l <= k; l++) {
            final File file =
                new File(httpdir, partyPrefix(l, messageLabelPrefix));

            if (file.exists()) {
                ExtIO.delete(file);
            }
        }
    }

    // ############### Reads and writes messages ##################

    /**
     * Puts the data on the HTTP server as published by the given
     * party.
     *
     * @param l Index of original publisher of the data (this may be
     * different from the index of this party).
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to write.
     * @param log Log context.
     * @return Number of bytes in data.
     */
    protected long writeMessage(final int l,
                                final String messageLabel,
                                final ByteTreeBasic message,
                                final Log log) {
        return writeData(partyPrefix(l, messageLabel), message, log);
    }

    /**
     * Puts the data on the HTTP server as published by this party.
     *
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to write.
     * @param log Log context.
     * @return Number of bytes in data.
     */
    protected long writeMessage(final String messageLabel,
                                final ByteTreeBasic message,
                                final Log log) {
        return writeMessage(j, messageLabel, message, log);
    }

    /**
     * Spends at most the time given by the timer reading the given
     * message from the party with index <code>loc</code>.
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param l Index of supposed producer of message.
     * @param messageLabel Message label of requested message.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param timer Timer that keeps track of how long we may try to
     * download.
     * @param log Log context.
     * @return Byte tree representation of the file downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected Pair<ByteTreeBasic, Long>
        readMessage(final int loc,
                    final int l,
                    final String messageLabel,
                    final long maximalByteLength,
                    final int maximalRecursiveDepth,
                    final Timer timer,
                    final Log log) {

        return readData(loc,
                        partyPrefix(l, messageLabel),
                        maximalByteLength,
                        maximalRecursiveDepth,
                        timer,
                        log);
    }

    /**
     * Returns a full message that embeds the index of the signer, the
     * message label, and the original message in an invertable way.
     *
     * @param l Index of original sender.
     * @param messageLabel Message label under which the message is
     * published.
     * @param message Original message.
     * @return Constructed byte tree.
     */
    protected ByteTreeBasic fullMessage(final int l,
                                        final String messageLabel,
                                        final ByteTreeBasic message) {

        // Use index of original sender and the message label as
        // prefix. This is safe due to the invertability of how a
        // ByteTree is encoded as a byte[].

        final byte[] labelBytes =
            ExtIO.getBytes(partyPrefix(l, messageLabel));
        final ByteTree labelByteTree = new ByteTree(labelBytes);

        return new ByteTreeContainer(labelByteTree, message);
    }

    // ############### Computes digests ###########################

    /**
     * Computes a digest of a message.
     *
     * @param l Index of original sender.
     * @param messageLabel Message label under which the message is
     * published.
     * @param message Original message.
     * @param s Index of owner of the hashfunction used to compute
     * digest.
     * @return Digest of message.
     */
    protected byte[] digestOfMessage(final int l,
                                     final String messageLabel,
                                     final ByteTreeBasic message,
                                     final int s) {
        final Hashdigest hd = pkeys[s].getDigest();
        fullMessage(l, messageLabel, message).update(hd);
        return hd.digest();
    }

    /**
     * Computes a joint digest of a message.
     *
     * @param l Index of original sender.
     * @param messageLabel Message label under which the message is
     * published.
     * @param message Original message.
     * @param jointHashfunction Hashfunction used to compute joint
     * digest.
     * @return Joint digest of message.
     */
    protected byte[]
        jointDigestOfMessage(final int l,
                             final String messageLabel,
                             final ByteTreeBasic message,
                             final Hashfunction jointHashfunction) {
        final Hashdigest hd = jointHashfunction.getDigest();
        fullMessage(l, messageLabel, message).update(hd);
        return hd.digest();
    }

    /**
     * Computes a digest of a joint digest.
     *
     * @param jointDigest Digest of which we compute the digest.
     * @param s Index of owner of the hashfunction used to compute
     * digest.
     * @return Digest of joint digest.
     */
    protected byte[] digestOfJointDigest(final byte[] jointDigest,
                                         final int s) {
        final Hashdigest hd = pkeys[s].getDigest();
        hd.update(jointDigest);
        return hd.digest();
    }

    // ############### Reads and writes signatures ############

    /**
     * Spends at most the time given by the timer to download, from
     * Party <code>loc</code>, a valid signature computed by Party
     * <code>s</code> of a message originally published by Party
     * <code>l</code> or a joint digest (depending on the parameters).
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param l Index of supposed producer of message.
     * @param messageLabel Message label of requested message.
     * @param s Index of supposed signer.
     * @param message Message of which we seek a signature. This
     * should be null to use the joint digest instead.
     * @param jointDigest Joint digest of message of which we seek a
     * signature. This should be null if the message is
     * used directly.
     * @param maximalSignatureByteLength Maximal number of bytes in a
     * signature.
     * @param timer Timer indicating how much time we can spend on
     * downloading a signature.
     * @param log Log context.
     * @return <code>true</code> or <code>false</code> depending on if
     * a valid signature could be downloaded or not.
     */
    protected Pair<Boolean, Long>
        readSignature(final int loc,
                      final int l,
                      final String messageLabel,
                      final int s,
                      final ByteTreeBasic message,
                      final byte[] jointDigest,
                      final int maximalSignatureByteLength,
                      final Timer timer,
                      final Log log) {

        // Download signature.
        final Pair<ByteTreeBasic, Long> fetchSignature =
            readData(loc,
                     sigPostfix(partyPrefix(l, messageLabel), s),
                     maximalSignatureByteLength,
                     0, // This implies that the signature must be a leaf.
                     timer,
                     log);

        final ByteTreeBasic signature = fetchSignature.first;
        final long readTime = fetchSignature.second;

        // Did we download a candidate signature?
        if (signature == null) {

            log.info("Unable to download signature!");
            return new Pair<Boolean, Long>(false, readTime);

        } else {

            try {

                // Compute digest.
                byte[] digest;
                if (message == null) {
                    digest = digestOfJointDigest(jointDigest, s);
                } else {
                    digest = digestOfMessage(l, messageLabel, message, s);
                }

                // Is the candidate signature valid? (Reading like
                // this is safe since the signature is a leaf of
                // limited size.)
                final byte[] signatureBytes =
                    signature.getByteTreeReader().read();
                final boolean res =
                    pkeys[s].verifyDigest(signatureBytes, digest);
                if (!res) {
                    log.info("Invalid signature!");
                }
                return new Pair<Boolean, Long>(res, readTime);

            } catch (final EIOException eioe) {
                log.info("Unable to extract signature from ByteTree!");
                log.register(eioe);

                return new Pair<Boolean, Long>(false, readTime);
            }
        }
    }

    /**
     * Publishes a signature of the full message derived from the
     * index of the publisher, the message label, and the message or
     * joint digest (depending on the parameters).
     *
     * @param l Index of publisher of the message.
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to be signed. This should be null if a
     * joint digest is meant to be signed.
     * @param jointDigest Digest to be signed. This should be null if
     * a message is meant to be signed.
     * @param log Log context.
     */
    protected void writeSignature(final int l,
                                  final String messageLabel,
                                  final ByteTreeBasic message,
                                  final byte[] jointDigest,
                                  final Log log) {

        // Compute digest using our key.
        byte[] digest;
        if (jointDigest == null) {
            digest = digestOfMessage(l, messageLabel, message, j);
        } else {
            digest = digestOfJointDigest(jointDigest, j);
        }

        // Sign digest.
        final byte[] signatureBytes = skey.signDigest(randomSource, digest);
        final ByteTree signature = new ByteTree(signatureBytes);

        // Write signature.
        writeData(sigPostfix(partyPrefix(l, messageLabel), j), signature, log);
    }

    /**
     * Spends at most the time given by the timer to download, from
     * Party <code>loc</code>, a message originally published by Party
     * <code>l</code> and a valid signature of the publisher. If
     * <code>jointHashfunction</code> is not null, then
     * <code>jointDigest</code> is assumed to be of size
     * <code>jointHashfunction</code>.
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param l Index of supposed producer of message.
     * @param messageLabel Filename of requested message.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param maximalSignatureByteLength Maximal number of bytes in a
     * signature.
     * @param timer Timer indicating how much time we can spend on
     * downloading a signature.
     * @param log Log context.
     * @param jointHashfunction Joint hashfunction. If this is not
     * null then digest is computed using this hashfunction
     * and the downloaded signature is a signature of the
     * digest.
     * @return Byte tree representation of the file downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected Triple<ByteTreeBasic, byte[], Long>
        readMessAndSig(final int loc,
                       final int l,
                       final String messageLabel,
                       final long maximalByteLength,
                       final int maximalRecursiveDepth,
                       final int maximalSignatureByteLength,
                       final Timer timer,
                       final Log log,
                       final Hashfunction jointHashfunction) {

        ByteTreeBasic message = null;
        byte[] jointDigest = null;

        // This initialization is not needed, but the compiler does
        // not understand this.
        long startTime = 0;

        boolean firstAttempt = true;

        do {

            // Try to download the message.
            final Pair<ByteTreeBasic, Long> fetchMessage =
                readMessage(loc,
                            l,
                            messageLabel,
                            maximalByteLength,
                            maximalRecursiveDepth,
                            timer,
                            log);

            message = fetchMessage.first;
            final long readTime = fetchMessage.second;

            // We start our clock after the first successful attempt
            // to download a message and then retroactively subtract
            // the time it took to download it. This gives us the
            // starting time of the first download of the message as a
            // starting point for remaining measurements.
            if (firstAttempt) {
                startTime = System.currentTimeMillis() - readTime;
                firstAttempt = false;
            }

            // Download and verify signature.
            if (message != null) {

                // If there is a joint hashfunction we use it first to
                // compress the message.
                if (jointHashfunction != null) {
                    jointDigest = jointDigestOfMessage(l,
                                                       messageLabel,
                                                       message,
                                                       jointHashfunction);
                }

                // Read signature.
                final Pair<Boolean, Long> fetchSignature =
                    readSignature(loc,
                                  l,
                                  messageLabel,
                                  l,
                                  message,
                                  jointDigest,
                                  maximalSignatureByteLength,
                                  timer,
                                  log);

                // If the signature was not valid, then we attempt to
                // download a new message and signature.
                if (!fetchSignature.first) {
                    message = null;
                }
            }

        } while (message == null && !timer.timeIsUp());

        // We only count successful attempts as network time. Failed
        // attempts is categorized as waiting.
        long messageTime = 0;
        if (message == null) {
            messageTime = System.currentTimeMillis() - startTime;
        }

        return new Triple<ByteTreeBasic, byte[], Long>(message,
                                                       jointDigest,
                                                       messageTime);
    }

    // Implemented in terms of the above.

    /**
     * Starts this server.
     *
     * @param log Logging context.
     */
    @Override
    public void start(final Log log) {
        if (!running) {

            if (external) {

                log.info("There is no need to start the external http server.");

            } else {

                log.info("Starting http server.");
                httpServer = new SimpleHTTPServer(httpdir, httpl, backLog);
                httpServer.start();

            }
            running = true;
        }
    }

    /**
     * Stops this server.
     *
     * @param log Logging context.
     */
    @Override
    public void stop(final Log log) {
        if (running) {

            if (external) {

                log.info("There is no need to stop the external server.");

            } else {

                log.info("Stopping http server.");
                httpServer.stop();

            }
            running = false;
        }
    }

    /**
     * Creates a new signature-label with a postfix from the indicated
     * party that represents the signature of the message published
     * under the given label.
     *
     * @param label Original label.
     * @param i Index of signing party.
     * @return Label representing the signature.
     */
    protected String sigPostfix(final String label, final int i) {
        return label + ".sig." + i;
    }

    /**
     * Adds a party-specific prefix to a filename.
     *
     * @param l Index of party.
     * @param label Label to be prefixed.
     * @return Prefixed filename.
     */
    protected String partyPrefix(final int l, final String label) {
        return Integer.toString(l) + "/" + label;
    }
}
