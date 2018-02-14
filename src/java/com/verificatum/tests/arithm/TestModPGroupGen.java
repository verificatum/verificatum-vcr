
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

package com.verificatum.tests.arithm;

import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.ModPGroupElement;
import com.verificatum.arithm.ModPGroupGen;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.tests.ui.gen.TestGenerator;
import com.verificatum.ui.gen.GenException;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Tests {@link ModPGroupGen}.
 *
 * @author Douglas Wikstrom
 */
// FB_ANNOTATION @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED")
public class TestModPGroupGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestModPGroupGen(final TestParameters tp) {
        super(tp, new ModPGroupGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        String groupDescription;
        PGroup pGroup;
        boolean invalid;
        String[] args;

        // Fixed group
        args = new String[2];
        args[0] = "-fixed";
        args[1] = "512";
        groupDescription = generator.gen(rs, args);
        pGroup = Marshalizer.unmarshalHexAux_PGroup(groupDescription, rs, 20);
        assert pGroup.equals(new ModPGroup(512))
            : "Unable to generate and recover fixed group!";

        invalid = false;
        try {
            args[1] = "20";
            groupDescription = generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on lower bound for fixed groups!";

        invalid = false;
        try {
            args[1] = "20000";
            groupDescription = generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on upper bound for fixed groups!";


        // Explicit group
        final ModPGroup mpg = new ModPGroup(512);
        final LargeInteger modulus = mpg.getModulus();
        final LargeInteger g = ((ModPGroupElement) mpg.getg()).toLargeInteger();
        final LargeInteger order =
            modulus.sub(LargeInteger.ONE).divide(LargeInteger.TWO);
        mpg.getEncoding();

        args = new String[4];
        args[0] = "-explic";
        args[1] = modulus.toString();
        args[2] = g.toString();
        args[3] = order.toString();
        groupDescription = generator.gen(rs, args);
        pGroup = Marshalizer.unmarshalHexAux_PGroup(groupDescription, rs, 20);
        assert pGroup.equals(mpg)
            : "Unable to generate and recover explicit group!";

        // Random Group
        args = new String[5];
        args[0] = "-rand";
        args[1] = "-roenc";
        args[2] = "-cert";
        args[3] = "20";
        args[4] = "512";
        groupDescription = generator.gen(rs, args);

    }
}
