
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

import java.io.File;

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomDeviceGen;
import com.verificatum.crypto.CryptoError;

/**
 * Tests {@link RandomDeviceGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestRandomDeviceGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestRandomDeviceGen(final TestParameters tp) {
        super(tp, new RandomDeviceGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        final String[] args = new String[1];
        args[0] = "/dev/urandom";

        final RandomSource rd = new RandomDevice(new File(args[0]));

        final String description = generator.gen(rs, args);
        final RandomSource rdd =
                Marshalizer.unmarshalHex_RandomSource(description);

        assert rdd.equals(rd) : "Failed to generate/recover random device!";

        // Bad group name.
        boolean invalid = false;
        args[0] = "xyz";
        try {
            generator.gen(rs, args);
        } catch (final CryptoError ce) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad device!";
    }
}
