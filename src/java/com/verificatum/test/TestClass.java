
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

import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ExtIO;


/**
 * Base class for a class that contains testing methods.
 *
 * @author Douglas Wikstrom
 */
public class TestClass {

    /**
     * Maximal running time of each test.
     */
    protected long testTime;

    /**
     * Test parameters.
     */
    protected TestParameters tp;

    /**
     * Source of randomness.
     */
    protected RandomSource rs;

    /**
     * Constructor needed to avoid that this class is instantiated.
     *
     * @param tp Test parameters.
     */
    protected TestClass(final TestParameters tp) {
        this.testTime = tp.milliSeconds;
        this.rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
        this.tp = tp;
    }
}
