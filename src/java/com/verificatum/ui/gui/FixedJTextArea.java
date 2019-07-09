
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

import javax.swing.JTextArea;

/**
 * Adds the ability to add a selection listener to {@link JTextArea}.
 *
 * @author Douglas Wikstrom
 */
public final class FixedJTextArea extends JTextArea {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Listener of this instance.
     */
    private SelectionListener sl;

    /**
     * Constructs an instance of the given size.
     *
     * @param defaultRows Default number of rows.
     * @param defaultColumns Default number of columns.
     */
    public FixedJTextArea(final int defaultRows, final int defaultColumns) {
        super(defaultRows, defaultColumns);
        this.sl = null;
    }

    /**
     * Adds a selection listener to this instance.
     *
     * @param sl Listener to be associated with this instance.
     */
    public void addSelectionListener(final SelectionListener sl) {
        this.sl = sl;
    }

    // Documented in SelectionListener.java

    @Override
    public void replaceSelection(final String content) {
        if (sl != null) {
            sl.replaceSelection(content);
        }
        super.replaceSelection(content);
    }
}
