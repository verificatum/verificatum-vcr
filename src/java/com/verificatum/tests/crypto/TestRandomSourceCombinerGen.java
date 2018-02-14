
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
import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomSource;
import com.verificatum.crypto.RandomSourceCombiner;
import com.verificatum.crypto.RandomSourceCombinerGen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.ui.gen.GenException;


/**
 * Tests {@link RandomSourceCombinerGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestRandomSourceCombinerGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestRandomSourceCombinerGen(final TestParameters tp) {
        super(tp, new RandomSourceCombinerGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        final RandomSource atomicSource = new RandomDevice();
        final String atomicSourceString =
            Marshalizer.marshalToHex(atomicSource);
        final RandomSource source =
            new RandomSourceCombiner(atomicSource, atomicSource);

        final String[] args = new String[2];
        args[0] = atomicSourceString;
        args[1] = atomicSourceString;
        final String sourceString = generator.gen(rs, args);
        final RandomSource source2 =
            Marshalizer.unmarshalHex_RandomSource(sourceString);
        assert source2.equals(source)
            : "Unable to generate and recover random source!";
    }
}
