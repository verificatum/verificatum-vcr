
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
