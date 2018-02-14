
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
