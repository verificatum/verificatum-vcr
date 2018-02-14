
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
 * <code>HashfunctionMerkleDamgaard</code> suitable for initialization
 * files. Using {@link com.verificatum.ui.gen.GeneratorTool} this
 * functionality can be invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public final class HashfunctionMerkleDamgaardGen implements Generator {

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

        final String simpleName =
            HashfunctionMerkleDamgaard.class.getSimpleName();

        final Opt opt = GeneratorTool.defaultOpt(simpleName, 1);

        opt.setUsageComment("(where " + simpleName + " = "
                            + HashfunctionMerkleDamgaard.class.getName() + ")");

        opt.addParameter("flHash",
                         "Underlying fixed length hashfunction, an instance "
                         + "of "
                         + HashfunctionFixedLength.class.getName() + ".");
        opt.addOption("-cert",
                      "value",
                      "Determines the probability that the underlying "
                      + "fixed-length hashfunction is malformed, i.e., a value "
                      + "of t gives a bound of 2^(-t)." + " Defaults to "
                      + DEFAULT_CERT + ".");

        final String s = "Generates an instance of the Merkle-Damgaard "
            + "hashfunction based on the given fixed length hashfunction.";

        opt.appendToUsageForm(1, "#-cert#flHash#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in crypto.Generator.java.

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
            final String flhfString = opt.getStringValue("flHash");
            final HashfunctionFixedLength flh =
                Marshalizer.
                unmarshalHexAux_HashfunctionFixedLength(flhfString,
                                                        randomSource,
                                                        certainty);

            final HashfunctionMerkleDamgaard hf =
                new HashfunctionMerkleDamgaard(flh);

            return Marshalizer.marshalToHexHuman(hf, opt.getBooleanValue("-v"));

        } catch (final EIOException eioe) {
            throw new GenException("Generation failed!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "The provably-secure Merkle-Damgaard construction.";
    }
}
