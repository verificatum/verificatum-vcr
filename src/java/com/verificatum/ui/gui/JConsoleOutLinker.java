
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

package com.verificatum.ui.gui;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.JTextArea;

import com.verificatum.ui.UIError;


/**
 * Links a <code>PipedOutputStream</code> to write to a
 * <code>JTextArea</code>, i.e., it allows us to write to a text area
 * by means of a stream.
 *
 * <b>WARNING! Read the documentation of {@link JTextualUI} before
 * use.</b>
 *
 * @author Douglas Wikstrom
 */
public final class JConsoleOutLinker implements Runnable {

    /**
     * Internal buffer.
     */
    static final int BUFFER_SIZE = 1024;

    /**
     * A pipe to allow writing to the text area.
     */
    private PipedInputStream pin;

    /**
     * Text area to which we write.
     */
    private final JTextArea jTextArea;

    /**
     * A thread that keeps writing from the stream onto the text area.
     */
    private final Thread thisThread;

    /**
     * Creates an instance.
     *
     * @param pout Source of inputs
     * @param jTextArea Where the inputs are written.
     */
    public JConsoleOutLinker(final PipedOutputStream pout,
                             final JTextArea jTextArea) {
        try {
            this.pin = new PipedInputStream(pout);
        } catch (final IOException ioe) {
            throw new UIError("Unable to create input stream!", ioe);
        }
        this.jTextArea = jTextArea;

        thisThread = new Thread(this);
        thisThread.setDaemon(true);
        thisThread.start();
    }

    // Documented in super class or interface.

    @Override
    public void run() {

        final byte[] buffer = new byte[BUFFER_SIZE];

        for (;;) {
            try {
                synchronized (this) {
                    this.wait(100);
                }
            } catch (final InterruptedException ie) {
            }

            int len = 0;
            try {
                final int noBytes = pin.available();

                if (noBytes > 0) {
                    len =
                        pin.read(buffer, 0, Math.min(noBytes, BUFFER_SIZE));
                    if (len > 0) {
                        jTextArea.append(new String(buffer, 0, len));
                        jTextArea
                            .setCaretPosition(jTextArea.getText().length());
                    }
                }
            } catch (final IOException ioe) {
                throw new UIError("Unable to read from input stream! "
                                  + ioe.getMessage(),
                                  ioe);
            }
        }
    }
}
