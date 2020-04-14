
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
