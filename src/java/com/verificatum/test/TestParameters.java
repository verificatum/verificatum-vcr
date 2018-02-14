
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
