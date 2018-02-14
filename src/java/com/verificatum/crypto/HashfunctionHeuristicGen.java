
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
 * Generates a human oriented string representation of a
 * <code>HashfunctionHeuristic</code> suitable for initialization
 * files. Using {@link com.verificatum.ui.gen.GeneratorTool} this
 * functionality can be invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public final class HashfunctionHeuristicGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid parameters.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.
            defaultOpt(HashfunctionHeuristic.class.getSimpleName(), 1);

        opt.setUsageComment("(where "
                            + HashfunctionHeuristic.class.getSimpleName()
                            + " = "
                            + HashfunctionHeuristic.class.getName() + ")");

        opt.addParameter("algorithm",
                         "Algorithm (SHA-256, SHA-384, or SHA-512).");

        final String s = "Generates an instance of a heuristically secure "
            + "hashfunction from the SHA-2 family.";

        opt.appendToUsageForm(1, "##algorithm#");

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

        final String algorithm = opt.getStringValue("algorithm");

        if ("SHA-256".equals(algorithm)
            || "SHA-384".equals(algorithm)
            || "SHA-512".equals(algorithm)) {

            final HashfunctionHeuristic hashfunction =
                new HashfunctionHeuristic(algorithm);
            return Marshalizer.marshalToHexHuman(hashfunction,
                                                 opt.getBooleanValue("-v"));

        } else {
            throw new GenException("The given hashfunction (" + algorithm
                                   + ") is not available!");
        }
    }

    @Override
    public String briefDescription() {
        return "Standard heuristically secure hashfunction from the "
            + "SHA-2 family.";
    }
}
