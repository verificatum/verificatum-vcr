
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
