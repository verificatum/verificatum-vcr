
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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpServer;
import com.verificatum.protocol.ProtocolError;

/**
 * A simple server with only minimal understanding of the HTTP
 * protocol. It can only process requests where the file resides in
 * the root directory of the server, and where the filename consists
 * exclusively of the letters A-Z, a-z, 0-9, and the special letters
 * "_" and ".", and contains no sequence of more than one ".".
 * Furthermore, all files are considered to have the content type
 * "binary/octet-stream", regardless of extensions.
 *
 * @author Douglas Wikstrom
 */
public final class SimpleHTTPServer {

    /**
     * Maximal number of attempts to listen.
     */
    public static final int MAX_SOCKET_ATTEMPTS = 10;

    /**
     * Number of milliseconds to sleep inbetween attempts to listen.
     */
    public static final int SOCKET_ATTEMPT_SLEEP = 500;

    /**
     * Address at which this server can be accessed.
     */
    InetSocketAddress socketAddress;

    /**
     * Root directory containing files that may be requested by
     * clients.
     */
    File directory;

    /**
     * Maximal number of concurrent clients.
     */
    int backLog;

    /**
     * Underlying HTTP server.
     */
    HttpServer server;

    /**
     * Lock object for updating sent bytes.
     */
    private final Object sentBytesLock = new Object();

    /**
     * Number of sent bytes.
     */
    long sentBytes;

    /**
     * Creates a server.
     *
     * @param directory Root directory containing files that may be
     * requested by clients.
     * @param hostname Hostname of this server.
     * @param port Port at which this server listens.
     * @param backLog Maximal number of concurrent clients.
     */
    public SimpleHTTPServer(final File directory,
                            final String hostname,
                            final int port,
                            final int backLog) {
        this(directory, new InetSocketAddress(hostname, port), backLog);
    }

    /**
     * Creates a server.
     *
     * @param directory Directory containing files that may be
     * requested by clients.
     * @param url URL of this server.
     * @param backLog Maximal number of concurrent clients.
     */
    public SimpleHTTPServer(final File directory,
                            final URL url,
                            final int backLog) {
        this(directory, new InetSocketAddress(url.getHost(), url.getPort()),
             backLog);
    }

    /**
     * Creates a server.
     *
     * @param directory Directory containing files that may be
     * requested by clients.
     * @param socketAddress Socket address of this server.
     * @param backLog Maximal number of concurrent clients.
     */
    public SimpleHTTPServer(final File directory,
                            final InetSocketAddress socketAddress,
                            final int backLog) {
        this.directory = directory;
        this.socketAddress = socketAddress;
        this.backLog = backLog;
        this.sentBytes = 0;
    }

    /**
     * Adds a number bytes sent by the handler to the total number of
     * bytes sent.
     *
     * @param additionalSentBytes Number of bytes sent by a handler.
     */
    public void addSentBytes(final long additionalSentBytes) {
        synchronized (sentBytesLock) {
            sentBytes += additionalSentBytes;
        }
    }

    /**
     * Reports the number of bytes sent by this server.
     *
     * @return Number of bytes sent by this server.
     */
    public long getSentBytes() {
        return sentBytes;
    }

    /**
     * Starts this server.
     */
    public void start() {

        // It seems we need to do this to avoid race conditions when
        // closing and listening on the same port frequently (which
        // happens when we test protocols, but not during normal
        // execution).
        for (int i = 0; i < MAX_SOCKET_ATTEMPTS; i++) {

            try {

                server = HttpServer.create(socketAddress, backLog);
                server.createContext("/", new SimpleHTTPHandler(this));
                server.start();
                break;

            } catch (final IOException ioe) {

                if (i == MAX_SOCKET_ATTEMPTS - 1) {
                    throw new ProtocolError("Failed to start HTTP server! ("
                                            + socketAddress + ")", ioe);
                } else {
                    try {
                        Thread.sleep(SOCKET_ATTEMPT_SLEEP);
                    } catch (final InterruptedException ie) {
                    }
                }
            }
        }
    }

    /**
     * Stops this server.
     */
    public void stop() {
        server.stop(0);
        server = null;
    }
}
