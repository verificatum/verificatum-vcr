
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

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.verificatum.eio.ExtIO;

/**
 * Handles HTTP requests by a clients in a simplistic way.
 *
 * @author Douglas Wikstrom
 */
public final class SimpleHTTPHandler implements HttpHandler {

    /**
     * Size of buffer used for streaming.
     */
    static final int BUFFER_SIZE = 4096;

    /**
     * Server for which this is a handler.
     */
    SimpleHTTPServer server;

    /**
     * Handles requests by clients.
     *
     * @param server Server creating this handler.
     */
    public SimpleHTTPHandler(final SimpleHTTPServer server) {
        this.server = server;
    }

    /**
     * Handler of exchanges. If the name of the requested file
     * consists only of digits 0-9, letters a-z and A-Z, and the
     * special symbols "/", "_", and ".", and does not have any
     * subsequences of more than one ".", then it is checked if the
     * file exists in our directory. If so, the file is streamed to
     * the client. Otherwise a failure message is streamed instead.
     *
     * @param exchange Exchange to be handled.
     * @throws IOException If the handler fails due to IO problems.
     */
    @Override
    public void handle(final HttpExchange exchange) throws IOException {

        // We expect this to be "GET".
        final String requestMethod = exchange.getRequestMethod();

        // Name of requested file without leading slash.
        String requestString = exchange.getRequestURI().getPath().substring(1);

        // The name of the requested file must not contain any
        // characters beyond digits, letters, "/", "_", ".", and must
        // not contain any sequence of more than one ".". If it does,
        // then we replace the entire filename by "/" before
        // continuing processing.

        boolean proper = true;

        for (int i = 0; proper && i < requestString.length(); i++) {

            final char c = requestString.charAt(i);

            final boolean digitOrLetter =
                '0' <= c && c <= '9'
                || 'a' <= c && c <= 'z'
                || 'A' <= c && c <= 'Z';

            final boolean underscoreOrSlash =
                c == '_' || c == '/';

            final boolean dotNotDoubleDot =
                c == '.' && (i == 0 || requestString.charAt(i - 1) != '.');

            proper = digitOrLetter || underscoreOrSlash || dotNotDoubleDot;
        }

        if (!proper) {
            requestString = "/";
        }

        // Full path of requested file assuming it resides in our
        // directory.
        final File requestFile = new File(server.directory, requestString);

        // Initialize datastructures for our response.
        final Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "binary/octet-stream");

        OutputStream os = null;
        final BufferedInputStream bis = null;
        FileInputStream fis = null;

        long requestLen = 0;
        long remaining = 0;
        try {

            // Write either requested file if it exists in our
            // directory and is readable, or inform client that the
            // request failed.
            if ("GET".equals(requestMethod) && requestFile.canRead()) {

                requestLen = requestFile.length();

                exchange.sendResponseHeaders(HTTP_OK, requestLen);
                os = exchange.getResponseBody();

                fis = new FileInputStream(requestFile);

                final byte[] buf = new byte[BUFFER_SIZE];
                remaining = requestLen;
                while (remaining > 0) {

                    final int rlen;
                    if (remaining < BUFFER_SIZE) {

                        // This is a safe cast due to the size of
                        // BUFFER_SIZE.
                        rlen = (int) remaining;
                    } else {
                        rlen = BUFFER_SIZE;
                    }

                    final int len = fis.read(buf, 0, rlen);

                    if (len == -1) {
                        break;
                    } else {
                        os.write(buf, 0, len);
                        remaining -= len;
                    }
                }

                if (remaining > 0) {
                    throw new IOException("Could not read complete file, "
                                          + remaining + " bytes remain!");
                }

            } else {

                final String response = "No such file!";
                final byte[] responseBytes = ExtIO.getBytes(response);
                exchange.sendResponseHeaders(HTTP_NOT_FOUND,
                                             responseBytes.length);
                os = exchange.getResponseBody();
                os.write(responseBytes);

            }
        } finally {

            server.addSentBytes(requestLen - remaining);
            ExtIO.strictClose(fis);
            ExtIO.strictClose(bis);
            exchange.close();
        }
    }
}
