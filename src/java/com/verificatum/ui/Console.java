
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
