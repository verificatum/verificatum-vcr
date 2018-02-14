
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
