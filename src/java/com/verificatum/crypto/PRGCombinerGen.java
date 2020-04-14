
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
 * <code>PRGCombiner</code> suitable for initialization files.
 *
 * @author Douglas Wikstrom
 */
public final class PRGCombinerGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt = GeneratorTool
            .defaultOpt(PRGCombiner.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + PRGCombiner.class.getSimpleName()
                            + " = " + PRGCombiner.class.getName() + ")");

        opt.addParameter("src", "Pseudo-random generator or random source, "
                         + "an instance of "
                         + "com.verificatum.crypto.RandomSource.");
        opt.addParameter("srcs",
                         "Zero or more additional sources, instances of "
                         + "com.verificatum.crypto.RandomSource.");

        final String s = "Generates a pseudo-random generator combiner that "
            + "simply xors the outputs of the random sources it encapsulates.";

        opt.appendToUsageForm(1, "##src,+srcs#");

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
            final String[] multi = opt.getMultiParameters();

            final RandomSource[] randomSources =
                new RandomSource[1 + multi.length];
            String randomSourceString = opt.getStringValue("src");
            randomSources[0] = Marshalizer
                .unmarshalHex_RandomSource(randomSourceString);

            for (int i = 0; i < multi.length; i++) {
                randomSourceString = multi[i];
                randomSources[1 + i] = Marshalizer
                    .unmarshalHex_RandomSource(randomSourceString);
            }

            final PRGCombiner combiner = new PRGCombiner(randomSources);

            return Marshalizer.marshalToHexHuman(combiner,
                                                 opt.getBooleanValue("-v"));

        } catch (final EIOException eioe) {
            throw new GenException("Generation failed!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Pseudo-random generator combiner.";
    }
}
