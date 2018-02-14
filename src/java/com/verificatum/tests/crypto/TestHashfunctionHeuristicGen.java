
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
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.crypto.HashfunctionHeuristicGen;

/**
 * Tests {@link HashfunctionHeuristicGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestHashfunctionHeuristicGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestHashfunctionHeuristicGen(final TestParameters tp) {
        super(tp, new HashfunctionHeuristicGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        final String[] names = {"SHA-256", "SHA-384", "SHA-512"};

        final String[] args = new String[1];

        for (int i = 0; i < names.length; i++) {
            args[0] = names[i];

            final Hashfunction h = new HashfunctionHeuristic(names[i]);

            final String description = generator.gen(rs, args);

            final Hashfunction hh =
                Marshalizer.unmarshalHexAux_Hashfunction(description, rs, 20);

            assert h.equals(hh) : "Failed to generate/recover hash function!";
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
