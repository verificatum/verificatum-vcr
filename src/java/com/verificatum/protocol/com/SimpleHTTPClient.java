
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.protocol.ProtocolError;
import com.verificatum.ui.Log;
import com.verificatum.util.Pair;
import com.verificatum.util.Timer;


/**
 * Simplistic HTTP client. It can only download files as
 * binary/octet-streams.
 *
 * @author Douglas Wikstrom
 */
public final class SimpleHTTPClient {

    /**
     * Size in bytes of buffer used for streaming.
     */
    public static final int BUFFER_SIZE = 4096;

    /**
     * Received number of bytes.
     */
    private long receivedBytes;


    /**
     * Avoid accidental instantiation.
     */
    public SimpleHTTPClient() {
        receivedBytes = 0;
    }

    /**
     * Receives data from a validated connection.
     *
     * @param connection Source of data.
     * @param contentLength Number of bytes of data.
     * @param os Stream where the fetched data is written.
     * @param timer Timer for receiving data.
     * @param log Logging context.
     * @return Indicates if the data was received correctly.
     * @throws IOException If there the data could not download the
     * data due to reading/writing data locally or network errors.
     */
    private boolean receiveData(final HttpURLConnection connection,
                                final long contentLength,
                                final OutputStream os,
                                final Timer timer,
                                final Log log)
        throws IOException {

        InputStream is = null;

        try {

            is = connection.getInputStream();

            final byte[] buf = new byte[BUFFER_SIZE];
            long remaining = contentLength;
            for (;;) {

                final int rlen;
                if (remaining < BUFFER_SIZE) {

                    // This is a safe cast due to BUFFER_SIZE.
                    rlen = (int) remaining;
                } else {
                    rlen = BUFFER_SIZE;
                }
                final int len = is.read(buf, 0, rlen);

                if (timer.timeIsUp() || len == -1 || remaining == 0) {

                    break;

                } else {

                    os.write(buf, 0, len);
                    remaining -= len;

                }
            }

            // Update the total number of received bytes.
            receivedBytes += (contentLength - remaining);

            // It is possible to send files using the HTTP protocol
            // without any length embedded. If you use an external
            // HTTP-server and your protocol fails at this point, then
            // the likely cause is that your server incorrectly sets
            // the header to 0 or -1.

            if (remaining == 0) {
                return true;
            } else {
                log.info("Expected " + contentLength + " bytes, but "
                         + remaining
                         + " bytes are missing! Does your HTTP "
                         + "server set the content length correctly?");
                return false;
            }
        } finally {
            ExtIO.strictClose(is);
        }
    }

    /**
     * Returns number of received bytes.
     *
     * @return Number of received bytes.
     */
    public long getReceivedBytes() {
        return receivedBytes;
    }

    /**
     * Processes a fetch request.
     *
     * @param connection Source of data.
     * @param os Stream where the fetched data is written.
     * @param timer Timer for receiving data.
     * @param maximalByteLength Maximal number of bytes to be
     * downloaded.
     * @param log Logging context.
     * @return Indicates if the data was received correctly.
     * @throws IOException If there the data could not download the
     * data due to reading/writing data locally or network errors.
     */
    private boolean processRequest(final HttpURLConnection connection,
                                   final OutputStream os,
                                   final Timer timer,
                                   final long maximalByteLength,
                                   final Log log)
        throws IOException {

        final long contentLength = connection.getContentLengthLong();

        if (contentLength < 0) {

            log.info("Unknown content length!");
            return false;

        } else if (contentLength > maximalByteLength) {

            log.info("Requested file is too long! (more than "
                     + maximalByteLength + " bytes)");
            return false;

        } else {

            return receiveData(connection, contentLength, os, timer, log);
        }
    }

    /**
     * Adapts a straightforward timeout.
     *
     * @param readTimeout Straightforward timeout.
     * @return Adapted timeout.
     */
    protected long adaptReadTimeout(final long readTimeout) {

        long myReadTimeout = readTimeout;

        // We want zero to actually mean "zero".
        if (myReadTimeout == 0) {
            myReadTimeout = 1;
        }

        // Then we translate negative values to zero before calling
        // HttpURLConnection with zero which is interpreted as
        // infinity.
        if (myReadTimeout < 0) {
            myReadTimeout = 0;
        }

        return myReadTimeout;
    }

    /**
     * Fetches a remote file and writes it to the given output stream.
     *
     * @param os Stream where the fetched data is written.
     * @param url URL of file to be fetched.
     * @param readTimeout Longest waiting time in milliseconds before
     * assuming that transfer failed.
     * @param maximalByteLength Maximal number of bytes to be
     * downloaded.
     * @param log Logging context
     * @return A pair of boolean and milliseconds, where the former is
     * the status of the download attempt and the second is the number
     * of milliseconds of network activity during the attempt.
     */
    public Pair<Boolean, Long> fetchFile(final OutputStream os,
                                         final URL url,
                                         final long readTimeout,
                                         final long maximalByteLength,
                                         final Log log) {

        // Used to determine the time used to fetch file.
        final long startTime = System.currentTimeMillis();

        boolean result = true;

        final long myReadTimeout = adaptReadTimeout(readTimeout);

        HttpURLConnection connection = null;
        try {

            final Timer timer = new Timer(readTimeout);

            // Connect using URL.

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout((int) myReadTimeout);
            connection.connect();

            if (!timer.timeIsUp()
                && connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                result = processRequest(connection,
                                        os,
                                        timer,
                                        maximalByteLength,
                                        log);
            } else {

                result = false;
            }

        } catch (final MalformedURLException murle) {
            // If the URL is malformed, then this is a bug.
            throw new ProtocolError("Not a valid URL!", murle);
        } catch (final SocketTimeoutException ste) {
            log.info("Socket timed out while waiting for data!");
            result = false;
        } catch (final java.net.ConnectException ce) {
            // We may fail to connect if one a party is temporarily
            // offline. Thus, we do not log this event.
            result = false;
        } catch (final java.net.ProtocolException pe) {
            log.info("Exception in the underlying network stack!");
            log.register(pe);
            result = false;
        } catch (final IOException ioe) {
            log.info("Exception while reading or writing!");
            log.register(ioe);
            result = false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Milliseconds passed during download attempt.
        final long fetchTime = System.currentTimeMillis() - startTime;

        return new Pair<Boolean, Long>(result, fetchTime);
    }

    /**
     * Fetches a remote file and stores it under the same name in the
     * given directory. The reason for any failing to do so is logged.
     *
     * @param rootURL Location of remote file.
     * @param destinationDir Local directory where the file is stored
     * after download.
     * @param fileName Name of file.
     * @param readTimeout Longest waiting time in milliseconds before
     * assuming that transfer failed.
     * @param maximalByteLength Maximal number of bytes to be
     * downloaded.
     * @param log Logging context
     * @return A pair of boolean and milliseconds, where the former is
     * the status of the download attempt and the second is the number
     * of milliseconds of network activity during the attempt.
     */
    public Pair<Boolean, Long> fetchFile(final URL rootURL,
                                         final File destinationDir,
                                         final String fileName,
                                         final long readTimeout,
                                         final long maximalByteLength,
                                         final Log log) {
        FileOutputStream fos = null;
        boolean result = true;
        long fetchTime = 0;

        try {

            final File tmp = new File(fileName);
            final File parent = tmp.getParentFile();
            final File fullDestinationDir =
                new File(destinationDir, parent.toString());

            // Make sure destination directory exists.
            if (!fullDestinationDir.exists()) {
                try {
                    ExtIO.mkdirs(fullDestinationDir);
                } catch (final EIOException eioe) {
                    throw new ProtocolError("Unable to make directories! ("
                                            + fullDestinationDir + ")", eioe);
                }
            }

            // Open temporary destination file.
            final String name = tmp.getName();
            final File tmpDestinationFile =
                new File(fullDestinationDir, "_" + name);
            fos = new FileOutputStream(tmpDestinationFile);

            // Attempt to fetch data.
            final URL url = new URL(rootURL, fileName);

            final Pair<Boolean, Long> fetchResult =
                fetchFile(fos, url, readTimeout, maximalByteLength, log);
            ExtIO.strictClose(fos);

            result = fetchResult.first;
            fetchTime = fetchResult.second;

            // Final destination of data.
            final File destinationFile = new File(fullDestinationDir, name);

            // Delete destination file if it exists.
            if (destinationFile.exists()
                && !ExtIO.delete(destinationFile)) {
                throw new ProtocolError("Unable to delete old file!");
            }

            // Rename temporary file to target file.
            if (!tmpDestinationFile.renameTo(destinationFile)) {
                final String description = "Unable to rename temporary file \""
                    + tmpDestinationFile + " to \"" + destinationFile
                    + "\"!";
                throw new ProtocolError(description);
            }

        } catch (final MalformedURLException murle) {
            throw new ProtocolError("Not a valid URL!", murle);
        } catch (final IOException ioe) {
            log.info("Exception while performing IO!");
            log.register(ioe);
            result = false;
        } finally {
            ExtIO.strictClose(fos);
        }
        return new Pair<Boolean, Long>(result, fetchTime);
    }

    /**
     * Fetches a remote file and returns the content as a byte array.
     *
     * @param rootURL Location of remote file.
     * @param fileName Name of file.
     * @param readTimeout Longest waiting time in milliseconds before
     * assuming that transfer failed.
     * @param maximalByteLength Maximal number of bytes to be
     * downloaded.
     * @param log Logging context
     * @return A pair of byte array and milliseconds, where the former
     * is the downloaded content, or null if it failed, and the second
     * is the number of milliseconds of network activity during the
     * attempt.
     */
    public Pair<byte[], Long> fetchFile(final URL rootURL,
                                        final String fileName,
                                        final int readTimeout,
                                        final long maximalByteLength,
                                        final Log log) {
        final boolean result = false;
        ByteArrayOutputStream baos = null;

        byte[] contents = null;
        long fetchTime = 0;
        try {

            baos = new ByteArrayOutputStream();
            final URL url = new URL(rootURL, fileName);
            final Pair<Boolean, Long> fetchResult =
                fetchFile(baos, url, readTimeout, maximalByteLength, log);
            ExtIO.strictClose(baos);
            fetchTime = fetchResult.second;

            if (result) {
                contents = baos.toByteArray();
            }
        } catch (final MalformedURLException murle) {
            throw new ProtocolError("Not a valid URL!", murle);
        } finally {
            ExtIO.strictClose(baos);
        }
        return new Pair<byte[], Long>(contents, fetchTime);
    }
}
