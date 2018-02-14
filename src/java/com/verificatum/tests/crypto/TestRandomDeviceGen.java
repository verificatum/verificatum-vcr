
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
