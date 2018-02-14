
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

package com.verificatum.tests.arithm;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PFieldElementArray;
import com.verificatum.arithm.PPRing;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PFieldElementArray}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPFieldElementArray extends TestPRingElementArray {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPFieldElementArray(final TestParameters tp)
        throws ArithmFormatException {
        super(new PField(SafePrimeTable.safePrime(512)),
              new PField(SafePrimeTable.safePrime(740)),
              new PPRing(new PField(SafePrimeTable.safePrime(740)),
                         new PField(SafePrimeTable.safePrime(740))),
              tp);
    }
}
