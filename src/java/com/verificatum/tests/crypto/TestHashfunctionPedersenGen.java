
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
import com.verificatum.arithm.ModPGroupGen;
import com.verificatum.crypto.HashfunctionFixedLength;
import com.verificatum.crypto.HashfunctionPedersenGen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.tests.ui.gen.TestGenerator;


/**
 * Tests {@link HashfunctionPedersenGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestHashfunctionPedersenGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestHashfunctionPedersenGen(final TestParameters tp) {
        super(tp, new HashfunctionPedersenGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        // Generate group string.
        final Generator pGroupGenerator = new ModPGroupGen();
        final String[] params = {"-fixed", "512"};
        final String pGroupString = pGroupGenerator.gen(rs, params);

        final String[] options =
            {"-width", "2", "-statDist", "20", "-cert", "50"};

        final String[] args = new String[3];
        args[2] = pGroupString;

        for (int i = 0; i < options.length / 2; i++) {

            args[0] = options[2 * i];
            args[1] = options[2 * i + 1];

            final String d = generator.gen(rs, args);
            final HashfunctionFixedLength h =
                Marshalizer.unmarshalHexAux_HashfunctionFixedLength(d, rs, 20);

            final String dd = Marshalizer.marshalToHexHuman(h, true);
            final HashfunctionFixedLength hh =
                Marshalizer.unmarshalHexAux_HashfunctionFixedLength(dd, rs, 20);

            assert hh.equals(h) : "Failed to generate/recover hash function!";
        }

        // Bad group name.
        boolean invalid = false;
        args[0] = "xyz";
        try {
            generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad hash function name!";
    }
}
