
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

package com.verificatum.test;

import java.io.PrintStream;

/**
 * Container class for global test parameters.
 *
 * @author Douglas Wikstrom
 */
public class TestParameters {

    /**
     * Destination for additional outputs during the test.
     */
    public PrintStream ps;

    /**
     * Seed used for main pseudo-random generator. This is used for
     * debugging.
     */
    public String prgseed;

    /**
     * Maximal execution time for time consuming tests.
     */
    public long milliSeconds;

    /**
     * Create test parameters.
     *
     * @param prgseed Seed used in tests.
     * the test).
     * @param milliSeconds For how long the test proceeds.
     * @param ps Destination for written output of test.
     */
    public TestParameters(final String prgseed,
                          final long milliSeconds,
                          final PrintStream ps) {
        this.prgseed = prgseed;
        this.milliSeconds = milliSeconds;
        this.ps = ps;
    }
}
