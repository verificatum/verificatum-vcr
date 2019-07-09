
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
 * Extracts a public key from a key pair.
 *
 * @author Douglas Wikstrom
 */
public class SignatureSKeyGen implements Generator {

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
            GeneratorTool.defaultOpt(SignatureSKey.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + SignatureSKey.class.getSimpleName()
                            + " = " + SignatureSKey.class.getName() + ")");

        opt.addParameter("keypair", "Signature key pair "
                         + "(com.verificatum.crypto.SignatureKeyPair).");
        opt.addOption("-cert", "value",
                      "Determines the probability that the input key pair "
                      + "is malformed, i.e., a value "
                      + "of t gives a bound of 2^(-t)." + " Defaults to "
                      + DEFAULT_CERT + ".");

        final String s = "Extracts a signature secret key from a key pair.";

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
            final SignatureKeyPair keypair = Marshalizer
                .unmarshalHexAux_SignatureKeyPair(keypairString,
                                                  randomSource, certainty);

            return Marshalizer.marshalToHexHuman(keypair.getSKey(),
                                                 opt.getBooleanValue("-v"));
        } catch (final EIOException eioe) {
            throw new GenException("Malformed key pair!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Signature secret key extractor.";
    }
}
