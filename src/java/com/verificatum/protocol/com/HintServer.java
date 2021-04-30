
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.verificatum.protocol.ProtocolError;


/**
 * Waits for a "hint" from any of the other servers in the form of a
 * tiny UDP-package. When it is received, this server signals an
 * interrupt on its listener thread if it has one, and then
 * unregisters the listener thread.
 *
 * @author Douglas Wikstrom
 */
public final class HintServer implements Runnable {

    /**
     * Maximal number of attempts to listen.
     */
    public static final int MAX_SOCKET_ATTEMPTS = 10;

    /**
     * Number of milliseconds to sleep inbetween attempts to listen.
     */
    public static final int SOCKET_ATTEMPT_SLEEP = 500;

    /**
     * Socket timeout.
     */
    public static final int SOCKET_TIMEOUT = 2000;

    /**
     * Socket for incoming hint packages.
     */
    DatagramSocket socket;

    /**
     * Listener threads that should be interrupted when a hint is
     * received.
     */
    Thread[] listeners;

    /**
     * Flag indicating if this instance is running or not.
     */
    boolean running;

    /**
     * Indicates if a hint was received already.
     */
    boolean[] hintReceived;

    /**
     * Number of parties.
     */
    int k;

    /**
     * A server listening at the given socket address.
     *
     * @param isa Socket address where this server listens when
     * started.
     * @param k Number of parties.
     */
    public HintServer(final InetSocketAddress isa, final int k) {

        this.k = k;

        // It seems we need to do this to avoid race conditions when
        // closing and listening on the same port frequently (which
        // happens when we test protocols, but not during normal
        // execution).
        for (int i = 0; i < MAX_SOCKET_ATTEMPTS; i++) {

            try {

                socket = new DatagramSocket(isa);
                socket.setReuseAddress(true);
                break;

            } catch (final SocketException se) {

                if (i == MAX_SOCKET_ATTEMPTS - 1) {
                    throw new ProtocolError("Invalid socket address! (" + isa
                                            + ")", se);
                } else {
                    try {
                        Thread.sleep(SOCKET_ATTEMPT_SLEEP);
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        listeners = new Thread[k + 1];
        Arrays.fill(listeners, null);

        hintReceived = new boolean[k + 1];
        Arrays.fill(hintReceived, false);

        running = false;
    }

    /**
     * Sends a hint to the remote server at the given socket address.
     *
     * @param j Index of hinting party.
     * @param isa Socket address of remote hint server.
     * @return True or false depending on if a hint was received.
     */
    public static boolean hint(final int j, final InetSocketAddress isa) {

        DatagramSocket socket = null;
        try {

            socket = new DatagramSocket();

            final byte[] buf = new byte[1];
            buf[0] = (byte) j;
            final DatagramPacket packet =
                new DatagramPacket(buf, buf.length, isa);
            socket.send(packet);

        } catch (final IOException ioe) {
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return true;
    }

    /**
     * Registers a thread as the listener of interrupts from this hint
     * server.
     *
     * @param l Party index of listener.
     * @param listener Listener of interrupts.
     */
    public void setListener(final int l, final Thread listener) {

        synchronized (this.listeners) {
            this.listeners[l] = listener;
        }
    }

    /**
     * Checks if the given listener should be interrupted. If so, then
     * it interrupts the listener and unregisters it.
     *
     * @param l Party index of listener.
     */
    public void checkHint(final int l) {

        synchronized (listeners) {
            if (0 < l && l <= k && hintReceived[l] && listeners[l] != null) {
                listeners[l].interrupt();
                listeners[l] = null;
                hintReceived[l] = false;
            }
        }
    }

    /**
     * Start this hint server.
     */
    public void start() {
        if (!running) {
            running = true;
            (new Thread(this)).start();
        }
    }

    /**
     * Stop this hint server.
     */
    public void stop() {
        running = false;
    }

    // Documented in superclass Thread.java.

    @Override
    public void run() {

        final byte[] buf = new byte[1];
        final DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (running) {
            try {

                socket.setSoTimeout(SOCKET_TIMEOUT);
                socket.receive(packet);
                final int l = buf[0];
                if (0 < l && l <= k) {
                    hintReceived[l] = true;
                    checkHint(l);
                }

            } catch (final IOException ioe) {
                // Hints are optimistic, so failures are ignored.
                continue;
            }
        }
        socket.close();
        socket = null;
    }
}
