
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

package com.verificatum.crypto;

import java.io.File;

import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;


/**
 * Generates a human oriented string representation of a
 * <code>RandomDevice</code> suitable for initialization files.
 *
 * @author Douglas Wikstrom
 */
public final class RandomDeviceGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(RandomDevice.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + RandomDevice.class.getSimpleName()
                            + " = " + RandomDevice.class.getName() + ")");

        opt.addParameter("path", "Absolute path to a random device.");

        final String s =
            "Generates a wrapper of a random device, i.e., a file "
            + "descriptor from which random bytes can be read."
            + "\n\n"
            + "WARNING! Make sure that the random device you wrap is SECURE "
            + "FOR CRYPTOGRAPHIC USE, and NEVER reuses randomness.";

        opt.appendToUsageForm(1, "##path#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in com.verificatum.crypto.Generator.java.

    @Override
    public String gen(final RandomSource randomSource, final String[] args)
        throws GenException {
        final Opt opt = opt();

        final String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        final RandomDevice rd =
            new RandomDevice(new File(opt.getStringValue("path")));

        // Sanity check: Make sure that we at least can read
        // something.
        rd.getBytes(1);

        return Marshalizer.marshalToHexHuman(rd, opt.getBooleanValue("-v"));
    }

    @Override
    public String briefDescription() {
        return "Random device wrapper.";
    }
}
