
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
