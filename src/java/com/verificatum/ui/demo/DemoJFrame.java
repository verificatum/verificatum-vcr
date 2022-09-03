
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
public class DemoJFrame {

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
     * Top level pane which can be added to a window.
     */
    protected JScrollPane jScrollPane;

    /**
     * Title bar of top level window.
     */
    protected String titleBar;

    /**
     * Size of window if any is created.
     */
    protected Dimension jFrameSize;

    /**
     * Window if any is created.
     */
    protected JFrame jFrame;

    /**
     * Creates an instance with the given number of simulated user
     * interfaces.
     *
     * @param k Number of user interfaces to simulate.
     * @param titleBar Titlebar of frame.
     */
    public DemoJFrame(final int k, final String titleBar) {
        this.titleBar = titleBar;

        jFrameSize = new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

        final JDesktopPane jDesktopPane = new JDesktopPane();

        uiArray = new JTextualUI[k + 1];

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

            if (xLoc + internalSize.width > jFrameSize.width) {
                xLoc = 0;
                yLoc += internalSize.height;
            }
        }

        final JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setPreferredSize(new Dimension(xTotal, yTotal));
        jPanel.add(jDesktopPane, BorderLayout.CENTER);

        jScrollPane =
            new JScrollPane(jPanel,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
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

    /**
     * Creates a window tied to the desktop and makes it visible. It
     * is unfortunate that the JDK instantiates windows at the desktop
     * level even when they are not made visible. This is a
     * workaround.
     *
     * @param visible Determines if a window is created and visible or
     * not.
     */
    public void setVisible(final boolean visible) {

        if (visible) {

            jFrame = new JFrame("Demonstration: " + titleBar);

            // Use standard window decoration
            jFrame.setDefaultLookAndFeelDecorated(true);

            final int inset = 50;
            final Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();

            jFrame.setBounds(inset,
                             inset,
                             screenSize.width - inset * 2,
                             screenSize.height - inset * 2);

            jFrame.setSize(jFrameSize);

            jFrame.setContentPane(jScrollPane);

            // Make sure we die if user closes the window using the OSs
            // close button.
            jFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(final WindowEvent we) {
                        System.exit(0);
                    }
                });

            jFrame.pack();

            jFrame.setVisible(true);
        }
    }

    /**
     * Disposes of the window if any has been created.
     */
    public void dispose() {
        if (jFrame != null) {
            jFrame.dispose();
        }
    }
}
