
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
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.crypto.CryptoKeyGen;
import com.verificatum.crypto.CryptoKeyGenNaorYung;
import com.verificatum.crypto.CryptoKeyGenNaorYungGen;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.ui.gen.GenException;


/**
 * Tests {@link CryptoKeyGenNaorYungGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestCryptoKeyGenNaorYungGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestCryptoKeyGenNaorYungGen(final TestParameters tp) {
        super(tp, new CryptoKeyGenNaorYungGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        final PGroup pGroup = ECqPGroupParams.getECqPGroup("P-256");
        final Hashfunction hashfunction = new HashfunctionHeuristic("SHA-256");
        final CryptoKeyGen keyGen =
            new CryptoKeyGenNaorYung(pGroup, hashfunction, 256);

        // Default.
        String[] args = new String[2];
        args[0] = Marshalizer.marshalToHexHuman(pGroup, true);
        args[1] = Marshalizer.marshalToHexHuman(hashfunction, true);
        final String keyGenString2 = generator.gen(rs, args);

        final CryptoKeyGen keyGen2 =
            Marshalizer.unmarshalHexAux_CryptoKeyGen(keyGenString2, rs, 20);
        assert keyGen2.equals(keyGen) : "Failed to generate key generator!";

        // With options.
        final String[] optargs = new String[6];
        optargs[0] = args[0];
        optargs[1] = args[1];

        optargs[2] = "-secpro";
        optargs[3] = "256";

        optargs[4] = "-cert";
        optargs[5] = "30";

        final String keyGenString3 = generator.gen(rs, optargs);
        final CryptoKeyGen keyGen3 =
            Marshalizer.unmarshalHexAux_CryptoKeyGen(keyGenString3, rs, 20);
        assert keyGen3.equals(keyGen) : "Failed to generate key generator!";

        // Failure.
        args = new String[2];
        args[0] = "XXX";
        args[1] = "YYY";
        boolean invalid = false;
        try {
            generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad inputs!";
    }
}
