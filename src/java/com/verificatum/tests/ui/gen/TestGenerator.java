
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
