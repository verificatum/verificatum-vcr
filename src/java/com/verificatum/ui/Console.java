
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

package com.verificatum.ui;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Uniform container of basic textual input and output streams that
 * are found on consoles.
 *
 * @author Douglas Wikstrom
 */
public class Console {

    /**
     * Standard input stream.
     */
    public InputStream in;

    /**
     * Standard output stream.
     */
    public PrintStream out;

    /**
     * Standard error stream.
     */
    public PrintStream err;

    /**
     * This prevents initialization.
     *
     * @param in Standard input.
     * @param out Standard output.
     * @param err Standard error.
     */
    protected Console(final InputStream in, final PrintStream out,
                      final PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }
}
