
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

package com.verificatum.tests.arithm;

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.ModPGroupElement;
import com.verificatum.arithm.ModPGroupGen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.ui.gen.GenException;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link ModPGroupGen}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestModPGroupGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestModPGroupGen(final TestParameters tp) {
        super(tp, new ModPGroupGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        String groupDescription;
        PGroup pGroup;
        boolean invalid;
        String[] args;

        // Fixed group
        args = new String[2];
        args[0] = "-fixed";
        args[1] = "512";
        groupDescription = generator.gen(rs, args);
        pGroup = Marshalizer.unmarshalHexAux_PGroup(groupDescription, rs, 20);
        assert pGroup.equals(new ModPGroup(512))
            : "Unable to generate and recover fixed group!";

        invalid = false;
        try {
            args[1] = "20";
            groupDescription = generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on lower bound for fixed groups!";

        invalid = false;
        try {
            args[1] = "20000";
            groupDescription = generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on upper bound for fixed groups!";


        // Explicit group
        final ModPGroup mpg = new ModPGroup(512);
        final LargeInteger modulus = mpg.getModulus();
        final LargeInteger g = ((ModPGroupElement) mpg.getg()).toLargeInteger();
        final LargeInteger order =
            modulus.sub(LargeInteger.ONE).divide(LargeInteger.TWO);
        mpg.getEncoding();

        args = new String[4];
        args[0] = "-explic";
        args[1] = modulus.toString();
        args[2] = g.toString();
        args[3] = order.toString();
        groupDescription = generator.gen(rs, args);
        pGroup = Marshalizer.unmarshalHexAux_PGroup(groupDescription, rs, 20);
        assert pGroup.equals(mpg)
            : "Unable to generate and recover explicit group!";

        // Random Group
        args = new String[5];
        args[0] = "-rand";
        args[1] = "-roenc";
        args[2] = "-cert";
        args[3] = "20";
        args[4] = "512";
        groupDescription = generator.gen(rs, args);

    }
}
