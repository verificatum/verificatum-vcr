
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
 * Generates a key pair using a key generator.
 *
 * @author Douglas Wikstrom
 */
public final class SignatureKeyPairGen implements Generator {

    /**
     * Default statistical distance.
     */
    static final int DEFAULT_CERT = 100;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(SignatureKeyPair.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + SignatureKeyPair.class.getSimpleName()
                            + " = " + SignatureKeyPair.class.getName() + ")");

        opt.addParameter("gen", "Signature key pair generator "
                         + "(com.verificatum.crypto.SignatureKeyGen).");
        opt.addOption("-cert",
                      "value",
                      "Determines the probability that the underlying "
                      + "key generator is malformed, i.e., a value "
                      + "of t gives a bound of 2^(-t)." + " Defaults to "
                      + DEFAULT_CERT + ".");

        final String s =
            "Generates a signature key pair using the given key generator.";

        opt.appendToUsageForm(1, "#-cert#gen#");
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

        final String generatorString = opt.getStringValue("gen");
        final int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

        try {
            final SignatureKeyGen generator =
                Marshalizer.unmarshalHexAux_SignatureKeyGen(generatorString,
                                                            randomSource,
                                                            certainty);

            final SignatureKeyPair keyPair = generator.gen(randomSource);

            return Marshalizer.marshalToHexHuman(keyPair,
                                                 opt.getBooleanValue("-v"));
        } catch (final EIOException eioe) {
            throw new GenException("Malformed key pair generator!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Key pair generator.";
    }
}
