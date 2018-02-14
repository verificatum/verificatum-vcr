
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
