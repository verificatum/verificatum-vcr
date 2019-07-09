
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
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.PRGElGamal;
import com.verificatum.crypto.PRGElGamalGen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.tests.ui.gen.TestGenerator;


/**
 * Tests {@link PRGElGamalGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestPRGElGamalGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestPRGElGamalGen(final TestParameters tp) {
        super(tp, new PRGElGamalGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        PRG prg = new PRGElGamal(SafePrimeTable.safePrime(512),
                                 PRGElGamal.DEFAULT_STATDIST);
        String[] args;

        // Fixed group
        args = new String[2];
        args[0] = "-fixed";
        args[1] = "512";
        String prgString = generator.gen(rs, args);
        PRG prg2 = Marshalizer.unmarshalHexAux_PRG(prgString, rs, 20);
        assert prg2.equals(prg)
            : "Unable to generate and recover fixed group generator!";

        // Options
        prg = new PRGElGamal(SafePrimeTable.safePrime(512), 3, 20);
        args = new String[8];
        args[0] = "-fixed";
        args[1] = "-statDist";
        args[2] = "20";
        args[3] = "-width";
        args[4] = "3";
        args[5] = "-cert";
        args[6] = "30";
        args[7] = "512";
        prgString = generator.gen(rs, args);
        prg2 = Marshalizer.unmarshalHexAux_PRG(prgString, rs, 20);
        assert prg2.equals(prg)
            : "Unable to generate and recover fixed group generator!";

        // Fixed group
        final LargeInteger prime = SafePrimeTable.safePrime(512);
        prg = new PRGElGamal(prime, PRGElGamal.DEFAULT_STATDIST);
        args = new String[2];
        args[0] = "-explic";
        args[1] = prime.toString();
        prgString = generator.gen(rs, args);
        prg2 = Marshalizer.unmarshalHexAux_PRG(prgString, rs, 20);
        assert prg2.equals(prg)
            : "Unable to generate and recover explicit group generator!";
    }
}
