
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

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Implements a simple console which connects standard input, output
 * and error to a {@link javax.swing.JTextArea}. One would
 * expect that such functionality existed in the standard Java API,
 * but it seems this is not the case.
 *
 * <b>WARNING! Read the documentation of {@link JTextualUI} before
 * use.</b>
 *
 * @author Douglas Wikstrom
 */
public class JConsole extends com.verificatum.ui.Console {

    /**
     * Connects a <code>JTextArea</code> to an input pipe stream.
     */
    private final JConsoleInLinker inLinker; // NOPMD Used in subclasses.

    /**
     * Connects a <code>JTextArea</code> to an output pipe stream.
     */
    private final JConsoleOutLinker outLinker; // NOPMD Used in subclasses.

    /**
     * Connects a <code>JTextArea</code> to an error pipe stream.
     */
    private final JConsoleOutLinker errLinker; // NOPMD Used in subclasses.

    /**
     * Creates a console and connects it to the given
     * <code>JTextArea</code>.
     *
     * @param jTextArea <code>JTextArea</code> to which this instance
     * is connected.
     */
    public JConsole(final FixedJTextArea jTextArea) {
        super(null, null, null);

        // Make sure user can not enter anything directly on the
        // JTextArea.
        jTextArea.setEditable(false);

        // Create pipes.
        final PipedInputStream pin = new PipedInputStream();
        final PipedOutputStream pout = new PipedOutputStream();
        final PipedOutputStream perr = new PipedOutputStream();

        // Link them to the JTextArea.
        inLinker = new JConsoleInLinker(jTextArea, pin);
        outLinker = new JConsoleOutLinker(pout, jTextArea);
        errLinker = new JConsoleOutLinker(perr, jTextArea);

        // Connect the pipes to workable streams.
        in = pin;
        out = new PrintStream(pout);
        err = new PrintStream(perr);

    }
}
