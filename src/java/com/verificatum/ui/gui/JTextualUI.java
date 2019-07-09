
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Label;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.tui.TextualUI;


/**
 * Implements a simple graphical user interface for cryptographic
 * protocols that essentially encapsulates the textual interface
 * implemented in {@link com.verificatum.ui.tui.TextualUI}.
 *
 * <p>
 *
 * <b>WARNING! Currently, this class is only meant to be used for
 * debugging purposes. The class {@link JConsole} and the associated
 * classes {@link JConsoleInLinker} and {@link JConsoleOutLinker} do
 * not really implement a console, but only a something simpler and
 * the implemented features are not implemented faithfully.</b>
 *
 * @author Douglas Wikstrom
 */
public final class JTextualUI extends JInternalFrame implements UI {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default number of rows in interaction text area.
     */
    static final int DEFAULT_ROWS_INTERACTION = 20;

    /**
     * Default number of columns in interaction text area.
     */
    static final int DEFAULT_COLS_INTERACTION = 60;

    /**
     * Default number of rows in log text area.
     */
    static final int DEFAULT_ROWS_LOG = 20;

    /**
     * Default number of columns in log text area.
     */
    static final int DEFAULT_COLS_LOG = 80;

    /**
     * The internal textual interface.
     */
    private final TextualUI textualUI;

    /**
     * The console used by this user interface.
     */
    private final JConsole jc;

    /**
     * Linker to allow logging to appear on a separate
     * <code>JTextArea</code>.
     */
    private final JConsoleOutLinker linker; // NOPMD This is used. PMD is wrong.

    /**
     * Creates a user interface for party number <code>i</code>.
     *
     * @param j Index of the party this interface should represent.
     */
    public JTextualUI(final int j) {

        // Set title and ensure resizability
        super("Party" + j, true);
        // Set up the logical part
        final FixedJTextArea interactionArea =
            new FixedJTextArea(DEFAULT_ROWS_INTERACTION,
                               DEFAULT_COLS_INTERACTION);
        interactionArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        jc = new JConsole(interactionArea);

        final JTextArea logArea =
            new JTextArea(DEFAULT_ROWS_LOG, DEFAULT_COLS_LOG);

        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setEditable(false);
        final PipedOutputStream pout = new PipedOutputStream();
        linker = new JConsoleOutLinker(pout, logArea);
        final PrintStream ps = new PrintStream(pout);

        textualUI = new TextualUI(jc);
        textualUI.getLog().addLogStream(ps);

        // Set up the graphical part
        final JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        final JScrollPane interactionJSP =
            new JScrollPane(interactionArea,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JPanel ip = new JPanel(new BorderLayout());
        ip.add(new Label("Interaction Area"), BorderLayout.NORTH);
        ip.add(interactionJSP, BorderLayout.CENTER);
        jSplitPane.setLeftComponent(ip);

        final JScrollPane logJSP =
            new JScrollPane(logArea,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JPanel lp = new JPanel(new BorderLayout());
        lp.add(new Label("Log file"), BorderLayout.NORTH);
        lp.add(logJSP, BorderLayout.CENTER);
        jSplitPane.setRightComponent(lp);

        setContentPane(jSplitPane);

    }

    /**
     * Sets the description string of this interface.
     *
     * @param descriptionString Description of this interface.
     */
    public void setDescriptionString(final String descriptionString) {
        setTitle(descriptionString);
        textualUI.setDescriptionString(descriptionString);
    }

    // The methods below are documented in an implemented interface.

    @Override
    public String getDescrString() {
        return textualUI.getDescrString();
    }

    @Override
    public String getDescrString(final int i) {
        return textualUI.getDescrString(i);
    }

    @Override
    public String stringQuery(final String msgString) {
        return textualUI.stringQuery(msgString);
    }

    @Override
    public boolean dialogQuery(final String msgString) {
        return textualUI.dialogQuery(msgString);
    }

    @Override
    public int intQuery(final String msgString) {
        return textualUI.intQuery(msgString);
    }

    @Override
    public int alternativeQuery(final String[] alternatives,
                                final String descString) {
        return textualUI.alternativeQuery(alternatives, descString);
    }

    @Override
    public Log getLog() {
        return textualUI.getLog();
    }

    @Override
    public void print(final String str) {
        textualUI.print(str);
    }
}
