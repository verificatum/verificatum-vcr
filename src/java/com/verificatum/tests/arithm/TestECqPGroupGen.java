
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
import com.verificatum.arithm.ECqPGroupGen;
import com.verificatum.arithm.ECqPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.EIOException;
import com.verificatum.test.TestParameters;
import com.verificatum.ui.gen.GenException;
import com.verificatum.tests.ui.gen.TestGenerator;


/**
 * Tests {@link ECqPGroupGen}.
 *
 * @author Douglas Wikstrom
 */
public class TestECqPGroupGen extends TestGenerator {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestECqPGroupGen(final TestParameters tp) {
        super(tp, new ECqPGroupGen());
    }

    @Override
    public void gen()
        throws ArithmException, ArithmFormatException,
               GenException, EIOException {
        super.gen();

        String[] args;

        // Generate group from real group name.
        final String curveName = "P-192";
        args = new String[2];
        args[0] = "-name";
        args[1] = curveName;
        final String groupDescription = generator.gen(rs, args);
        final PGroup pGroup =
            Marshalizer.unmarshalHexAux_PGroup(groupDescription, rs, 20);
        assert curveName.equals(((ECqPGroup) pGroup).getCurveName())
            : "Unable to generate and recover group!";

        // Bad group name.
        boolean invalid = false;
        invalid = false;
        args = new String[2];
        args[0] = "-name";
        args[1] = "xyz";
        try {
            generator.gen(rs, args);
        } catch (final GenException ge) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad group name!";
    }
}
