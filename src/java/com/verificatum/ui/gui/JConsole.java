
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
