
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
