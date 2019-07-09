
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

package com.verificatum.tests.crypto;

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.PRGHeuristicGen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.ui.gen.GenException;


/**
 * Tests {@link PRGHeuristicGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestPRGHeuristicGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestPRGHeuristicGen(final TestParameters tp) {
        super(tp, new PRGHeuristicGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        final Hashfunction hashfunction = new HashfunctionHeuristic("SHA-256");
        final String hashfunctionString =
            Marshalizer.marshalToHexHuman(hashfunction, true);
        final PRG prg = new PRGHeuristic(hashfunction);

        // Default for hashfunction.
        String[] args = new String[1];
        args[0] = hashfunctionString;
        String prgString = generator.gen(rs, args);
        PRG prg2 = Marshalizer.unmarshalHexAux_PRG(prgString, rs, 20);
        assert prg2.equals(prg) : "Failed to generate key generator!";

        // Default.
        args = new String[0];
        prgString = generator.gen(rs, args);
        prg2 = Marshalizer.unmarshalHexAux_PRG(prgString, rs, 20);
        assert prg2.equals(prg) : "Failed to generate key generator!";

        // Fail.
        args = new String[1];
        args[0] = "XXX";
        boolean invalid = false;
        try {
            generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad parameter!";

    }
}
