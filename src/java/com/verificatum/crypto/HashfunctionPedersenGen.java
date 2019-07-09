
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
 * Generates a human oriented string representation of a
 * <code>HashfunctionPedersen</code> suitable for initialization
 * files. Using {@link com.verificatum.ui.gen.GeneratorTool} this
 * functionality can be invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionPedersenGen implements Generator {

    /**
     * Default number of generators used in the hashfunction.
     */
    static final int DEFAULT_WIDTH = 2;

    /**
     * Default statistical distance used when generating a
     * hashfunction.
     */
    static final int DEFAULT_STATDIST = 100;

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
    protected final Opt opt() {
        final Opt opt =
            GeneratorTool.
            defaultOpt(HashfunctionPedersen.class.getSimpleName(), 1);

        opt.setUsageComment("(where "
                            + HashfunctionPedersen.class.getSimpleName() + " = "
                            + HashfunctionPedersen.class.getName() + ")");

        opt.addParameter("pGroup", "Underlying prime order group, an "
                         + "instance of " + PGroup.class.getName() + ".");

        opt.addOption("-width", "width",
                      "Number of generators used. Defaults to "
                      + DEFAULT_WIDTH + ".");
        opt.addOption("-statDist", "bits", "Statistical error parameter. "
                      + "Defaults to " + DEFAULT_STATDIST + ".");
        opt.addOption("-cert", "value",
                      "Certainty that the group is correct, i.e., a value "
                      + "of t gives a probability of an"
                      + "incorrect group of at most 2^(-t). "
                      + "Defaults to " + DEFAULT_CERT + ".");

        final String s =
            "Generates an instance of the Pedersen hashfunction over "
            + "the given group.";

        opt.appendToUsageForm(1, "#-width,-statDist,-cert#pGroup#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in com.verificatum.crypto.Generator.java.

    @Override
    public final String gen(final RandomSource randomSource,
                            final String[] args)
        throws GenException {
        GeneratorTool.verify(randomSource);

        final Opt opt = opt();

        final String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        try {

            final int width = opt.getIntValue("-width", DEFAULT_WIDTH);
            if (width > HashfunctionPedersen.MAX_WIDTH) {
                throw new GenException("Too large width, maximum is "
                                       + HashfunctionPedersen.MAX_WIDTH + "!");
            }

            final int statDist = opt.getIntValue("-statDist", DEFAULT_STATDIST);
            final int cert = opt.getIntValue("-cert", DEFAULT_CERT);

            final String pGroupString = opt.getStringValue("pGroup");
            final PGroup pGroup =
                Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                   randomSource,
                                                   cert);

            final HashfunctionPedersen phf =
                new HashfunctionPedersen(pGroup, width, randomSource, statDist);

            return Marshalizer.marshalToHexHuman(phf,
                                                 opt.getBooleanValue("-v"));
        } catch (final EIOException eioe) {
            throw new GenException("Unable to parse group description!", eioe);
        }
    }

    @Override
    public final String briefDescription() {
        return "Pedersen's hashfunction.";
    }
}
