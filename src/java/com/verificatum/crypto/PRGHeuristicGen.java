
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

import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;

/**
 * Generates a human oriented string representation of a
 * <code>PRGHeuristic</code> suitable for initialization files. Using
 * {@link com.verificatum.ui.gen.GeneratorTool} this functionality can be
 * invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public final class PRGHeuristicGen implements Generator {

    /**
     * Default statistical distance used when generating a
     * hashfunction.
     */
    static final int DEFAULT_CERT = 100;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(PRGHeuristic.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + PRGHeuristic.class.getSimpleName()
                            + " = " + PRGHeuristic.class.getName() + ")");

        final String s = "Generates an instance of a heuristically secure "
            + "pseudo-random generator. Internally is uses an instance of a "
            + "cryptographically strong hashfunction. A seed is expanded by "
            + "hashing the seed along with an integer counter that is "
            + "incremented for each iteration. The default hashfunction is "
            + "SHA-256.";

        opt.addParameter("hashfunction",
                         "Hashfunction used to expand the seed. This must "
                         + "be an instance of "
                         + "com.verificatum.crypto.Hashfunction. "
                         + "WARNING! Make sure that your hashfunction is "
                         + "suitable to be used as a pseudo-random function.");

        opt.addOption("-cert",
                      "value",
                      "Determines the probability that the underlying "
                      + "fixed-length hashfunction is malformed, i.e., a value "
                      + "of t gives a bound of 2^(-t)." + " Defaults to "
                      + DEFAULT_CERT + ".");

        opt.appendToUsageForm(1, "###hashfunction");

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

        final int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

        try {
            Hashfunction hashfunction;
            if (opt.valueIsGiven("hashfunction")) {

                final String s = opt.getStringValue("hashfunction");
                hashfunction =
                    Marshalizer.unmarshalHexAux_Hashfunction(s,
                                                             randomSource,
                                                             certainty);

            } else {

                hashfunction = new HashfunctionHeuristic("SHA-256");
            }

            final PRGHeuristic prg = new PRGHeuristic(hashfunction);
            return Marshalizer
                .marshalToHexHuman(prg, opt.getBooleanValue("-v"));
        } catch (final EIOException eioe) {
            throw new GenException("Malformed hashfunction!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Pseudo random generator based on a cryptographic "
            + "hashfunction with a counter.";
    }
}
