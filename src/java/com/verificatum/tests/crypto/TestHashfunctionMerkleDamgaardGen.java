
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
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionFixedLength;
import com.verificatum.crypto.HashfunctionMerkleDamgaard;
import com.verificatum.crypto.HashfunctionMerkleDamgaardGen;
import com.verificatum.crypto.HashfunctionPedersen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.tests.ui.gen.TestGenerator;


/**
 * Tests {@link HashfunctionMerkleDamgaardGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestHashfunctionMerkleDamgaardGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestHashfunctionMerkleDamgaardGen(final TestParameters tp) {
        super(tp, new HashfunctionMerkleDamgaardGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        final PGroup pGroup = new ModPGroup(512);
        final PGroupElement h = pGroup.getg().mul(pGroup.getg());
        final HashfunctionFixedLength hash =
            new HashfunctionPedersen(pGroup.getg(), h);
        final String hashString = Marshalizer.marshalToHex(hash);

        final Hashfunction hashfunction =
            new HashfunctionMerkleDamgaard(hash);

        // Default
        final String[] args = new String[3];
        args[0] = "-cert";
        args[1] = "20";
        args[2] = hashString;

        final String hashString2 = generator.gen(rs, args);
        final Hashfunction hashfunction2 =
            Marshalizer.unmarshalHexAux_Hashfunction(hashString2, rs, 20);

        assert hashfunction2.equals(hashfunction)
            : "Failed to fail on bad hash function name!";
    }
}
