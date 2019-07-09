
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

package com.verificatum.ui.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.verificatum.ui.UI;
import com.verificatum.ui.gui.JTextualUI;


/**
 * Implements a demonstration user interface in which there is a
 * simulated embedded user interface for each party taking part in the
 * protocol.
 *
 * @author Douglas Wikstrom
 */
public class DemoJFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default width of the main window.
     */
    static final int DEFAULT_WINDOW_WIDTH = 1280;

    /**
     * Default height of the main window.
     */
    static final int DEFAULT_WINDOW_HEIGHT = 768;

    /**
     * Simulated user interfaces of all parties.
     */
    protected JTextualUI[] uiArray;

    /**
     * Creates an instance with the given number of simulated user
     * interfaces.
     *
     * @param k Number of user interfaces to simulate.
     * @param titleBar Titlebar of frame.
     */
    public DemoJFrame(final int k, final String titleBar) {
        super("Demonstration: " + titleBar);

        // Use standard window decoration
        setDefaultLookAndFeelDecorated(true);

        final int inset = 50;
        final Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();

        setBounds(inset,
                  inset,
                  screenSize.width - inset * 2,
                  screenSize.height - inset * 2);

        setSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));

        final JDesktopPane jDesktopPane = new JDesktopPane();

        uiArray = new JTextualUI[k + 1];

        final Dimension size = getSize();

        int xTotal = 0;
        int yTotal = 0;
        int xLoc = 0;
        int yLoc = 0;
        for (int i = 1; i <= k; i++) {

            uiArray[i] = new JTextualUI(i);
            uiArray[i].pack();
            final Dimension internalSize = uiArray[i].getSize();
            uiArray[i].setLocation(xLoc, yLoc);
            xLoc += internalSize.width;
            xTotal = Math.max(xTotal, xLoc);
            yTotal = Math.max(yTotal, yLoc + internalSize.height);
            uiArray[i].setVisible(true);
            jDesktopPane.add(uiArray[i]);

            if (xLoc + internalSize.width > size.width) {
                xLoc = 0;
                yLoc += internalSize.height;
            }
        }

        final JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setPreferredSize(new Dimension(xTotal, yTotal));
        jPanel.add(jDesktopPane, BorderLayout.CENTER);

        final JScrollPane pane =
            new JScrollPane(jPanel,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        setContentPane(pane);

        // Make sure we die if user closes the window using the OSs
        // close button.
        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent we) {
                    System.exit(0);
                }
            });

        pack();
    }

    /**
     * Returns the simulated user interface of the <code>i</code>th
     * party.
     *
     * @param i Index of a party.
     * @return User interface of the given party.
     */
    public UI uiAt(final int i) {
        return uiArray[i];
    }
}
