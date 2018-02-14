
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

import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;

/**
 * Generator of the key generation algorithm of the standard RSA
 * signature scheme based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public final class SignatureKeyGenHeuristicGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt = GeneratorTool.
            defaultOpt(SignatureKeyGenHeuristic.class.getSimpleName(), 1);

        opt.setUsageComment("(where "
                            + SignatureKeyGenHeuristic.class.getSimpleName()
                            + " = "
                            + SignatureKeyGenHeuristic.class.getName() + ")");

        opt.addParameter("bitlength", "Bits in modulus.");

        final String s =
            "Generates an instance of a full domain hash (SHA-256) "
            + "RSA signature key generator for keys of the given key length";

        opt.appendToUsageForm(1, "##bitlength#");
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

        final int bitlength = opt.getIntValue("bitlength");

        final SignatureKeyGenHeuristic keygen =
            new SignatureKeyGenHeuristic(bitlength);

        return Marshalizer.marshalToHexHuman(keygen, opt.getBooleanValue("-v"));
    }

    @Override
    public String briefDescription() {
        return "RSA signature key generator.";
    }
}
