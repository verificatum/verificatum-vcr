
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
 * Extracts a public key from a key pair.
 *
 * @author Douglas Wikstrom
 */
public class SignaturePKeyGen implements Generator {

    /**
     * Default statistical distance.
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
            GeneratorTool.defaultOpt(SignaturePKey.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + SignaturePKey.class.getSimpleName()
                            + " = " + SignaturePKey.class.getName() + ")");

        opt.addParameter("keypair", "Signature key pair "
                         + "(com.verificatum.crypto.SignatureKeyPair).");
        opt.addOption("-cert", "value",
                      "Determines the probability that the input key pair "
                      + "is malformed, i.e., a value "
                      + "of t gives a bound of 2^(-t)." + " Defaults to "
                      + DEFAULT_CERT + ".");

        final String s = "Extracts a signature public key from a key pair.";

        opt.appendToUsageForm(1, "#-cert#keypair#");
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

        final String keypairString = opt.getStringValue("keypair");
        final int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

        try {
            final SignatureKeyPair keypair =
                Marshalizer.unmarshalHexAux_SignatureKeyPair(keypairString,
                                                             randomSource,
                                                             certainty);

            return Marshalizer.marshalToHexHuman(keypair.getPKey(),
                                                 opt.getBooleanValue("-v"));
        } catch (final EIOException eioe) {
            throw new GenException("Malformed key pair!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Signature public key extractor.";
    }
}
