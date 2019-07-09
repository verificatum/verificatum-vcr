
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
