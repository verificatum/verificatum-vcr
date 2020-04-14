
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

package com.verificatum.tests.arithm;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.ECqPGroup;
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link ECqPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class TestECqPGroup extends TestPGroup {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestECqPGroup(final TestParameters tp)
        throws ArithmFormatException {
        super(ECqPGroupParams.getECqPGroup("P-256"),
              ECqPGroupParams.getECqPGroup("P-384"),
              new ModPGroup(512),
              tp);
    }

    @Override
    protected PGroup[] encodingPGroups()
        throws ArithmFormatException {

        final PGroup[] pGroups = new PGroup[4];
        pGroups[0] = ECqPGroupParams.getECqPGroup("P-192");
        pGroups[1] = ECqPGroupParams.getECqPGroup("P-256");
        pGroups[2] = ECqPGroupParams.getECqPGroup("P-384");
        pGroups[3] = ECqPGroupParams.getECqPGroup("P-521");

        return pGroups;
    }

    @Override
    protected PGroup newInstance(final ByteTreeReader btr,
                                 final RandomSource rs)
        throws ArithmFormatException {
        return ECqPGroup.newInstance(btr, rs, 20);
    }
}
