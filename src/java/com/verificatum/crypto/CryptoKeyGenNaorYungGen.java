
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
