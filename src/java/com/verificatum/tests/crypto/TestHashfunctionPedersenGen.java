
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
