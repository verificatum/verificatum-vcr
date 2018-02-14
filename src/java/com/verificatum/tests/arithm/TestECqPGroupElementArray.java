
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
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link com.verificatum.arithm.BPGroupElementArrayIM} and
 * {@link com.verificatum.arithm.BPGroupElementArrayF} based on {@link
 * com.verificatum.arithm.ECqPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class TestECqPGroupElementArray extends TestPGroupElementArray {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestECqPGroupElementArray(final TestParameters tp)
        throws ArithmFormatException {
        super(ECqPGroupParams.getECqPGroup("P-256"),
              ECqPGroupParams.getECqPGroup("P-384"),
              new ModPGroup(512),
              tp);
    }
}
