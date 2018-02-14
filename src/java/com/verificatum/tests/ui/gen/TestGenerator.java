
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

package com.verificatum.tests.ui.gen;

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link com.verificatum.ui.gen.Generator}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public abstract class TestGenerator extends TestClass {

    /**
     * Generator.
     */
    protected final Generator generator;

    /**
     * Creates basic testing of generator.
     *
     * @param tp Test parameters.
     * @param generator Generator to be tested.
     */
    public TestGenerator(final TestParameters tp, final Generator generator) {
        super(tp);
        this.generator = generator;
    }

    /**
     * Generator.
     *
     * @throws ArithmException When failing test.
     * @throws ArithmFormatException When failing test.
     * @throws GenException When failing test.
     * @throws EIOException When failing test.
     */
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {

        // Default case.
        final String[] args = new String[1];
        args[0] = "-h";
        generator.gen(rs, args);
    }

    /**
     * Exercise briefDescription().
     *
     * @throws ArithmException When failing test.
     * @throws ArithmFormatException When failing test.
     */
    public void briefDescription()
        throws ArithmException, ArithmFormatException {
        generator.briefDescription();
    }
}
