
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

import com.verificatum.arithm.PGroup;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;

/**
 * Generates a human oriented string representation of a {@link
 * CryptoKeyGenNaorYung} suitable for initialization files.
 *
 * @author Douglas Wikstrom
 */
public final class CryptoKeyGenNaorYungGen implements Generator {

    /**
     * Default certainty used to check correctness of group.
     */
    static final int CERTAINTY = 100;

    /**
     * Default bit length of challenges in Fiat-Shamir proof.
     */
    static final int SECPRO = 256;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(CryptoKeyGenNaorYung.class.getSimpleName(),
                                     1);

        opt.setUsageComment("(where "
                            + CryptoKeyGenNaorYung.class.getSimpleName() + " = "
                            + CryptoKeyGenNaorYung.class.getName() + ")");

        opt.addParameter("pGroup",
                         "Group over which the keys are generated, "
                         + "instance of com.verificatum.arithm.PGroup.");
        opt.addParameter("roh",
                         "Collision-resistant hashfunction, instance of "
                         + "com.verificatum.crypto.Hashfunction.");

        opt.addOption("-cert", "value",
                      "Certainty in primality testing, "
                      + "i.e., a value of t gives a probability of an "
                      + "incorrect group of at most 2^(-t).");

        opt.addOption("-secpro", "value",
                      "Bit length of challenges in Fiat-Shamir proof. This "
                      + " defaults to " + SECPRO + ".");

        final String s = "Outputs a key generator of Naor-Yung keys of the "
            + "given group and with the given hashfunction.";

        opt.appendToUsageForm(1, "#-cert,-secpro#pGroup,roh#");

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

        try {

            int certainty = CERTAINTY;
            if (opt.valueIsGiven("-cert")) {
                certainty = opt.getIntValue("-cert");
            }

            final String pGroupString = opt.getStringValue("pGroup");

            final PGroup pGroup =
                Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                   randomSource,
                                                   certainty);

            final String rohString = opt.getStringValue("roh");
            final Hashfunction roh =
                Marshalizer.unmarshalHexAux_Hashfunction(rohString,
                                                         randomSource,
                                                         certainty);

            int secpro = SECPRO;
            if (opt.valueIsGiven("-secpro")) {
                secpro = opt.getIntValue("-secpro");
            }

            final CryptoKeyGen keyGen =
                new CryptoKeyGenNaorYung(pGroup, roh, secpro);

            return Marshalizer.marshalToHexHuman(keyGen,
                                                 opt.getBooleanValue("-v"));

        } catch (final EIOException eioe) {
            throw new GenException("Unable to create key generator! ("
                                   + eioe.getMessage() + ")", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Constructor of Naor-Yung key generator.";
    }
}
